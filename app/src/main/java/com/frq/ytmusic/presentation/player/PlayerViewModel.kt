package com.frq.ytmusic.presentation.player

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import com.frq.ytmusic.domain.model.QueueManager
import com.frq.ytmusic.domain.model.Song
import com.frq.ytmusic.domain.usecase.GetLyricsUseCase
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
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val getLyricsUseCase: GetLyricsUseCase,
    private val downloadSongUseCase: com.frq.ytmusic.domain.usecase.DownloadSongUseCase,
    private val deleteDownloadUseCase: com.frq.ytmusic.domain.usecase.DeleteDownloadUseCase,
    private val isDownloadedUseCase: com.frq.ytmusic.domain.usecase.IsDownloadedUseCase,
    private val downloadRepository: com.frq.ytmusic.domain.repository.DownloadRepository
) : ViewModel() {

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val queueManager = QueueManager()
    private var currentPlayerListener: Player.Listener? = null
    private var favoriteObserverJob: kotlinx.coroutines.Job? = null
    private var downloadObserverJob: kotlinx.coroutines.Job? = null
    private var sleepTimerJob: kotlinx.coroutines.Job? = null

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
                        
                        // Check if sleep timer is set to "end of song"
                        if (_playerState.value.sleepTimerMinutes == -1) {
                            // Don't play next, just clear timer
                            _playerState.update { 
                                it.copy(
                                    sleepTimerMinutes = null,
                                    sleepTimerEndTime = 0L
                                ) 
                            }
                        } else {
                            // Auto play next handled by MediaSession service or QueueManager
                            playNext()
                        }
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
     * Skip reload if same song is already playing/loaded.
     */
    fun playSong(song: Song) {
        val currentSong = _playerState.value.currentSong
        if (currentSong?.videoId == song.videoId) {
            // Same song - just ensure it's playing, don't reload
            serviceConnection.controller.value?.let { controller ->
                if (!controller.isPlaying) {
                    controller.play()
                }
            }
            return
        }
        
        queueManager.setQueue(listOf(song), 0)
        updateQueueState()
        playCurrentSong()
    }

    /**
     * Play song from search results.
     * Only queues the selected song, then auto-fills with related songs.
     * This provides a better listening experience than playing all search results.
     */
    fun playSongFromList(songs: List<Song>, index: Int) {
        val targetSong = songs.getOrNull(index) ?: return
        val currentSong = _playerState.value.currentSong
        
        if (currentSong?.videoId == targetSong.videoId) {
            // Same song - just ensure it's playing, don't reload
            serviceConnection.controller.value?.let { controller ->
                if (!controller.isPlaying) {
                    controller.play()
                }
            }
            return
        }
        
        // Only queue the selected song, related songs will be loaded automatically
        queueManager.setQueue(listOf(targetSong), 0)
        updateQueueState()
        playCurrentSong()
        
        // Auto-fill queue with related songs in background
        loadRelatedSongsToQueue(targetSong.videoId)
    }

    /**
     * Load related songs and add to queue (not for display, for playback).
     */
    private fun loadRelatedSongsToQueue(videoId: String) {
        viewModelScope.launch {
            getRelatedSongsUseCase(videoId, 15)
                .onSuccess { songs ->
                    if (songs.isNotEmpty()) {
                        songs.forEach { queueManager.addToQueue(it) }
                        updateQueueState()
                    }
                }
        }
    }

    /**
     * Add song to queue.
     */
    fun addToQueue(song: Song) {
        queueManager.addToQueue(song)
        updateQueueState()
    }

    /**
     * Play a song from local file (for downloaded songs - instant playback).
     * Skip reload if same song is already playing/loaded.
     */
    fun playLocalFile(song: Song, filePath: String) {
        val currentSong = _playerState.value.currentSong
        if (currentSong?.videoId == song.videoId) {
            // Same song - just ensure it's playing, don't reload
            serviceConnection.controller.value?.let { controller ->
                if (!controller.isPlaying) {
                    controller.play()
                }
            }
            return
        }
        
        queueManager.setQueue(listOf(song), 0)
        updateQueueState()
        
        // Observe states for new song
        observeFavoriteStatus(song.videoId)
        loadLyrics(song.videoId)
        observeDownloadStatus(song.videoId)
        
        viewModelScope.launch {
            _playerState.update { 
                it.copy(
                    currentSong = song,
                    isLoading = true,
                    error = null
                ) 
            }
            
            val controller = serviceConnection.controller.value
            if (controller != null) {
                val mediaItem = MediaItem.Builder()
                    .setUri(filePath)
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
     * Load related songs for display in Related tab.
     */
    private fun loadRelatedSongsForDisplay(videoId: String) {
        viewModelScope.launch {
            _playerState.update { it.copy(isRelatedLoading = true, relatedSongs = emptyList()) }
            getRelatedSongsUseCase(videoId, 20)
                .onSuccess { songs ->
                    _playerState.update { it.copy(relatedSongs = songs, isRelatedLoading = false) }
                }
                .onFailure {
                    _playerState.update { it.copy(isRelatedLoading = false) }
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

    /**
     * Play song from queue at specific index.
     */
    fun playFromQueue(index: Int) {
        if (index in queueManager.queue.indices && index != queueManager.currentIndex) {
            queueManager.skipTo(index)
            updateQueueState()
            playCurrentSong()
        }
    }

    private fun playCurrentSong() {
        val song = queueManager.currentSong ?: return
        
        // Observe favorite status for new song
        observeFavoriteStatus(song.videoId)
        loadLyrics(song.videoId)
        loadRelatedSongsForDisplay(song.videoId)
        observeDownloadStatus(song.videoId)
        
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

    private fun observeDownloadStatus(videoId: String) {
        downloadObserverJob?.cancel()
        downloadObserverJob = viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                downloadRepository.downloadState,
                isDownloadedUseCase(videoId)
            ) { globalState, isDownloaded ->
                val currentState = globalState
                when {
                    currentState is com.frq.ytmusic.data.local.DownloadState.Downloading && currentState.videoId == videoId -> currentState
                    currentState is com.frq.ytmusic.data.local.DownloadState.Error && currentState.videoId == videoId -> currentState
                    currentState is com.frq.ytmusic.data.local.DownloadState.Completed && currentState.videoId == videoId -> currentState
                    isDownloaded -> com.frq.ytmusic.data.local.DownloadState.Completed(videoId)
                    else -> com.frq.ytmusic.data.local.DownloadState.Idle
                }
            }.collect { state ->
                _playerState.update { it.copy(downloadState = state) }
            }
        }
    }

    fun toggleFavorite() {
        val song = _playerState.value.currentSong ?: return
        viewModelScope.launch {
            toggleFavoriteUseCase(song)
        }
    }

    fun toggleDownload() {
        val song = _playerState.value.currentSong ?: return
        val currentState = _playerState.value.downloadState
        
        viewModelScope.launch {
            if (currentState is com.frq.ytmusic.data.local.DownloadState.Completed) {
                deleteDownloadUseCase(song.videoId)
            } else if (currentState is com.frq.ytmusic.data.local.DownloadState.Idle || currentState is com.frq.ytmusic.data.local.DownloadState.Error) {
                downloadSongUseCase(song)
            }
            // Downloading state clicks are ignored or could be cancel
        }
    }

    fun toggleLyricsVisibility() {
        _playerState.update { it.copy(isLyricsVisible = !it.isLyricsVisible) }
    }

    fun seekTo(positionMs: Long) {
        serviceConnection.controller.value?.let { controller ->
            controller.seekTo(positionMs)
        }
    }

    private fun loadLyrics(videoId: String) {
        viewModelScope.launch {
            _playerState.update { it.copy(isLyricsLoading = true, lyrics = null) }
            getLyricsUseCase(videoId)
                .onSuccess { lyrics ->
                    _playerState.update { it.copy(lyrics = lyrics, isLyricsLoading = false) }
                }
                .onFailure {
                    _playerState.update { it.copy(isLyricsLoading = false) }
                }
        }
    }

    // ===== Sleep Timer Functions =====
    
    /**
     * Set sleep timer.
     * @param minutes Duration in minutes. -1 = end of current song.
     */
    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        
        if (minutes == -1) {
            // End of song mode - will be handled in STATE_ENDED
            _playerState.update { 
                it.copy(
                    sleepTimerMinutes = -1,
                    sleepTimerEndTime = 0L
                ) 
            }
        } else {
            val endTime = System.currentTimeMillis() + (minutes * 60 * 1000L)
            _playerState.update { 
                it.copy(
                    sleepTimerMinutes = minutes,
                    sleepTimerEndTime = endTime
                ) 
            }
            
            sleepTimerJob = viewModelScope.launch {
                delay(minutes * 60 * 1000L)
                // Timer expired - pause playback
                serviceConnection.controller.value?.pause()
                _playerState.update { 
                    it.copy(
                        sleepTimerMinutes = null,
                        sleepTimerEndTime = 0L
                    ) 
                }
            }
        }
    }
    
    /**
     * Cancel active sleep timer.
     */
    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        _playerState.update { 
            it.copy(
                sleepTimerMinutes = null,
                sleepTimerEndTime = 0L
            ) 
        }
    }
    
    /**
     * Get remaining time text for display.
     */
    fun getSleepTimerRemainingText(): String? {
        val state = _playerState.value
        return when {
            state.sleepTimerMinutes == null -> null
            state.sleepTimerMinutes == -1 -> "Akhir lagu"
            else -> {
                val remaining = state.sleepTimerEndTime - System.currentTimeMillis()
                if (remaining > 0) {
                    val minutes = (remaining / 60000).toInt()
                    val seconds = ((remaining % 60000) / 1000).toInt()
                    "${minutes}m ${seconds}s tersisa"
                } else null
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        favoriteObserverJob?.cancel()
        downloadObserverJob?.cancel()
        sleepTimerJob?.cancel()
        serviceConnection.controller.value?.let { controller ->
            currentPlayerListener?.let { controller.removeListener(it) }
        }
    }
}
