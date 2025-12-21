package com.frq.ytmusic.presentation.search

import com.frq.ytmusic.domain.model.Song

/**
 * UI state for the search screen.
 */
data class SearchUiState(
    val query: String = "",
    val songs: List<Song> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEmpty: Boolean = false
) {
    val showEmptyState: Boolean
        get() = isEmpty && !isLoading && error == null && query.isNotBlank()
    
    val showResults: Boolean
        get() = songs.isNotEmpty() && !isLoading
    
    val showInitialState: Boolean
        get() = query.isBlank() && songs.isEmpty() && !isLoading
}
