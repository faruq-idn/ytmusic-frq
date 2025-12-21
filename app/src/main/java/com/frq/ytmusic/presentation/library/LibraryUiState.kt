package com.frq.ytmusic.presentation.library

import com.frq.ytmusic.domain.model.Song

/**
 * UI state for Library screen.
 */
data class LibraryUiState(
    val favorites: List<Song> = emptyList(),
    val isLoading: Boolean = false
) {
    val isEmpty: Boolean get() = favorites.isEmpty() && !isLoading
}
