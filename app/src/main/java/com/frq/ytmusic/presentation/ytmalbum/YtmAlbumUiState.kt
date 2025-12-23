package com.frq.ytmusic.presentation.ytmalbum

import com.frq.ytmusic.domain.model.Song
import com.frq.ytmusic.domain.model.YtmAlbumDetail

/**
 * UI State for album detail screen.
 */
data class YtmAlbumUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val album: YtmAlbumDetail? = null,
    val songs: List<Song> = emptyList()
)
