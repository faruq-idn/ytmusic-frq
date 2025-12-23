package com.frq.ytmusic.presentation.ytmplaylist

import com.frq.ytmusic.domain.model.Song
import com.frq.ytmusic.domain.model.YtmPlaylistDetail

/**
 * UI state for YTM playlist detail screen.
 */
data class YtmPlaylistUiState(
    val playlist: YtmPlaylistDetail? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isDownloading: Boolean = false,
    val downloadProgress: Int = 0, // Number of songs downloaded
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val localPlaylistId: Long? = null
)
