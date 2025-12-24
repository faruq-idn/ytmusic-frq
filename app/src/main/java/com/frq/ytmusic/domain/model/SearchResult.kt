package com.frq.ytmusic.domain.model

/**
 * Combined search result containing songs, playlists, albums, and artists.
 */
data class SearchResult(
    val songs: List<Song>,
    val playlists: List<YtmPlaylist>,
    val albums: List<YtmAlbum> = emptyList(),
    val artists: List<YtmArtist> = emptyList()
)
