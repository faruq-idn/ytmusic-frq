package com.frq.ytmusic.presentation.search

import com.frq.ytmusic.domain.model.Song
import com.frq.ytmusic.domain.model.YtmAlbum
import com.frq.ytmusic.domain.model.YtmPlaylist

/**
 * UI state for the search screen.
 */
data class SearchUiState(
    val query: String = "",
    val songs: List<Song> = emptyList(),
    val playlists: List<YtmPlaylist> = emptyList(),
    val albums: List<YtmAlbum> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEmpty: Boolean = false
) {
    val showEmptyState: Boolean
        get() = isEmpty && !isLoading && error == null && query.isNotBlank()
    
    val showResults: Boolean
        get() = (songs.isNotEmpty() || playlists.isNotEmpty() || albums.isNotEmpty()) && !isLoading
    
    val showSuggestions: Boolean
        get() = suggestions.isNotEmpty() && songs.isEmpty() && playlists.isEmpty() && albums.isEmpty() && query.isNotBlank() && !isLoading
    
    val showInitialState: Boolean
        get() = query.isBlank() && songs.isEmpty() && playlists.isEmpty() && albums.isEmpty() && !isLoading
}

