package com.frq.ytmusic.presentation.player

import com.frq.ytmusic.domain.model.Song

/**
 * State representing the current player status.
 */
data class PlayerState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val error: String? = null
) {
    val hasMedia: Boolean
        get() = currentSong != null
    
    val progress: Float
        get() = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f
}
