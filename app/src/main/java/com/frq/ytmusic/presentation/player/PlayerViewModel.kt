package com.frq.ytmusic.presentation.player

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import com.frq.ytmusic.domain.model.QueueManager
import com.frq.ytmusic.domain.model.Song
import com.frq.ytmusic.domain.usecase.GetRelatedSongsUseCase
import com.frq.ytmusic.domain.usecase.GetStreamUrlUseCase
import com.frq.ytmusic.domain.usecase.IsFavoriteUseCase
import com.frq.ytmusic.domain.usecase.ToggleFavoriteUseCase
import com.frq.ytmusic.service.MusicServiceConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing music playback via background service.
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val serviceConnection: MusicServiceConnection,
    private val getStreamUrlUseCase: GetStreamUrlUseCase,
    private val getRelatedSongsUseCase: GetRelatedSongsUseCase,
    private val isFavoriteUseCase: IsFavoriteUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val queueManager = QueueManager()
    private var currentPlayerListener: Player.Listener? = null
    private var favoriteObserverJob: kotlinx.coroutines.Job? = null

    init {
        serviceConnection.connect()
        
        viewModelScope.launch {
            serviceConnection.controller.collect { controller ->
                controller?.let { setupPlayerListener(it) }
            }
        }
        
        startPositionUpdates()
    }

    private fun setupPlayerListener(player: Player) {
        currentPlayerListener?.let { player.removeListener(it) }
        
        currentPlayerListener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playerState.update { it.copy(isPlaying = isPlaying) }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        _playerState.update { it.copy(isLoading = true) }
                    }
                    Player.STATE_READY -> {
                         _playerState.update { 
                            it.copy(
                                isLoading = false,
                                duration = player.duration,
                                isShuffleEnabled = player.shuffleModeEnabled,
                                repeatMode = player.repeatMode
                            ) 
                        }
                    }
                    Player.STATE_ENDED -> {
                        _playerState.update { it.copy(isPlaying = false) }
                        // Auto play next handled by MediaSession service or QueueManager
                        playNext()
                    }
                    Player.STATE_IDLE -> {
                        _playerState.update { it.copy(isLoading = false) }
                    }
                }
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                _playerState.update { it.copy(repeatMode = repeatMode) }
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _playerState.update { it.copy(isShuffleEnabled = shuffleModeEnabled) }
            }
        }
        
        player.addListener(currentPlayerListener!!)
    }

    /**
     * Play a single song (clears queue).
     */
    fun playSong(song: Song) {
        queueManager.setQueue(listOf(song), 0)
        updateQueueState()
        playCurrentSong()
    }

    /**
     * Play song from search results (sets queue to all results).
     */
    fun playSongFromList(songs: List<Song>, index: Int) {
        queueManager.setQueue(songs, index)
        updateQueueState()
        playCurrentSong()
    }

    /**
     * Add song to queue.
     */
    fun addToQueue(song: Song) {
        queueManager.addToQueue(song)
        updateQueueState()
    }

    /**
     * Play next song. Auto-loads related songs if queue is empty.
     */
    fun playNext() {
        val nextSong = queueManager.next()
        if (nextSong != null) {
            updateQueueState()
            playCurrentSong()
        } else {
            // Queue ended, load related songs
            loadRelatedSongs()
        }
    }

    /**
     * Load related songs based on current song.
     */
    private fun loadRelatedSongs() {
        val currentSong = _playerState.value.currentSong ?: return
        
        viewModelScope.launch {
            getRelatedSongsUseCase(currentSong.videoId, 10)
                .onSuccess { songs ->
                    if (songs.isNotEmpty()) {
                        songs.forEach { queueManager.addToQueue(it) }
                        updateQueueState()
                        // Auto-play next (which is now the first related song)
                        queueManager.next()?.let {
                            updateQueueState()
                            playCurrentSong()
                        }
                    }
                }
        }
    }

    /**
     * Play previous song.
     */
    fun playPrevious() {
        val prevSong = queueManager.previous()
        if (prevSong != null) {
            updateQueueState()
            playCurrentSong()
        }
    }

    private fun playCurrentSong() {
        val song = queueManager.currentSong ?: return
        
        // Observe favorite status for new song
        observeFavoriteStatus(song.videoId)
        
        viewModelScope.launch {
            _playerState.update { 
                it.copy(
                    currentSong = song,
                    isLoading = true,
                    error = null
                ) 
            }

            getStreamUrlUseCase(song.videoId)
                .onSuccess { streamUrl ->
                    val controller = serviceConnection.controller.value
                    if (controller != null) {
                        val mediaItem = MediaItem.Builder()
                            .setUri(streamUrl)
                            .setMediaMetadata(
                                MediaMetadata.Builder()
                                    .setTitle(song.title)
                                    .setArtist(song.artist)
                                    .setArtworkUri(song.thumbnailUrl?.let { android.net.Uri.parse(it) })
                                    .build()
                            )
                            .build()
                        
                        controller.setMediaItem(mediaItem)
                        controller.prepare()
                        controller.play()
                    } else {
                        _playerState.update { 
                            it.copy(isLoading = false, error = "Player not ready") 
                        }
                    }
                }
                .onFailure { exception ->
                    _playerState.update { 
                        it.copy(isLoading = false, error = exception.message ?: "Failed to play") 
                    }
                }
        }
    }

    private fun updateQueueState() {
        _playerState.update {
            it.copy(
                queue = queueManager.queue,
                currentIndex = queueManager.currentIndex
            )
        }
    }

    fun togglePlayPause() {
        serviceConnection.controller.value?.let { controller ->
            if (controller.isPlaying) controller.pause() else controller.play()
        }
    }

    fun toggleShuffle() {
        serviceConnection.controller.value?.let { controller ->
            val newMode = !controller.shuffleModeEnabled
            controller.shuffleModeEnabled = newMode
            _playerState.update { it.copy(isShuffleEnabled = newMode) }
        }
    }

    fun toggleRepeat() {
        serviceConnection.controller.value?.let { controller ->
            // OFF(0) -> ONE(1) -> ALL(2) -> OFF(0)
            val newMode = when (controller.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
                Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
                else -> Player.REPEAT_MODE_OFF
            }
            controller.repeatMode = newMode
            _playerState.update { it.copy(repeatMode = newMode) }
        }
    }

    fun seekTo(progress: Float) {
        serviceConnection.controller.value?.let { controller ->
            val position = (progress * controller.duration).toLong()
            controller.seekTo(position)
        }
    }

    private fun startPositionUpdates() {
        viewModelScope.launch {
            while (true) {
                serviceConnection.controller.value?.let { controller ->
                    if (controller.isPlaying) {
                        _playerState.update { 
                            it.copy(currentPosition = controller.currentPosition) 
                        }
                    }
                }
                delay(500)
            }
        }
    }

    private fun observeFavoriteStatus(videoId: String) {
        favoriteObserverJob?.cancel()
        favoriteObserverJob = viewModelScope.launch {
            isFavoriteUseCase(videoId).collect { isFav ->
                _isFavorite.value = isFav
            }
        }
    }

    fun toggleFavorite() {
        val song = _playerState.value.currentSong ?: return
        viewModelScope.launch {
            toggleFavoriteUseCase(song)
        }
    }

    override fun onCleared() {
        super.onCleared()
        favoriteObserverJob?.cancel()
        serviceConnection.controller.value?.let { controller ->
            currentPlayerListener?.let { controller.removeListener(it) }
        }
    }
}
