package com.frq.ytmusic.presentation.player

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import com.frq.ytmusic.domain.model.Song
import com.frq.ytmusic.domain.usecase.GetStreamUrlUseCase
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
    private val getStreamUrlUseCase: GetStreamUrlUseCase
) : ViewModel() {

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var currentPlayerListener: Player.Listener? = null

    init {
        // Connect to service
        serviceConnection.connect()
        
        // Observe controller
        viewModelScope.launch {
            serviceConnection.controller.collect { controller ->
                controller?.let { setupPlayerListener(it) }
            }
        }
        
        // Start position updates
        startPositionUpdates()
    }

    private fun setupPlayerListener(player: Player) {
        // Remove previous listener
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
                                duration = player.duration
                            ) 
                        }
                    }
                    Player.STATE_ENDED -> {
                        _playerState.update { it.copy(isPlaying = false) }
                    }
                    Player.STATE_IDLE -> {
                        _playerState.update { it.copy(isLoading = false) }
                    }
                }
            }

            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                // Update song info from metadata if available
            }
        }
        
        player.addListener(currentPlayerListener!!)
    }

    /**
     * Play a song.
     */
    fun playSong(song: Song) {
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
                            it.copy(
                                isLoading = false,
                                error = "Player not ready"
                            ) 
                        }
                    }
                }
                .onFailure { exception ->
                    _playerState.update { 
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to play"
                        ) 
                    }
                }
        }
    }

    /**
     * Toggle play/pause.
     */
    fun togglePlayPause() {
        serviceConnection.controller.value?.let { controller ->
            if (controller.isPlaying) {
                controller.pause()
            } else {
                controller.play()
            }
        }
    }

    /**
     * Seek to position (0-1 range).
     */
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

    override fun onCleared() {
        super.onCleared()
        serviceConnection.controller.value?.let { controller ->
            currentPlayerListener?.let { controller.removeListener(it) }
        }
        // Don't disconnect - keep service running
    }
}
