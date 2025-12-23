package com.frq.ytmusic.domain.model

/**
 * Combined search result containing songs, playlists, and albums.
 */
data class SearchResult(
    val songs: List<Song>,
    val playlists: List<YtmPlaylist>,
    val albums: List<YtmAlbum> = emptyList()
)

