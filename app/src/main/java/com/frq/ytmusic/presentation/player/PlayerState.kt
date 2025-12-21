package com.frq.ytmusic.presentation.player

import com.frq.ytmusic.domain.model.Lyrics
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
    val error: String? = null,
    val queue: List<Song> = emptyList(),
    val currentIndex: Int = -1,
    val isShuffleEnabled: Boolean = false,
    val repeatMode: Int = 0, // 0: OFF, 1: ONE, 2: ALL
    val lyrics: Lyrics? = null,
    val isLyricsVisible: Boolean = false,
    val isLyricsLoading: Boolean = false,
    val isFavorite: Boolean = false,
    val downloadState: com.frq.ytmusic.data.local.DownloadState = com.frq.ytmusic.data.local.DownloadState.Idle
) {
    val hasMedia: Boolean
        get() = currentSong != null
    
    val progress: Float
        get() = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f
    
    val hasNext: Boolean
        get() = currentIndex < queue.size - 1
    
    val hasPrevious: Boolean
        get() = currentIndex > 0
}
