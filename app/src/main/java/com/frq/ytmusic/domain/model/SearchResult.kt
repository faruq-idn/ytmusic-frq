package com.frq.ytmusic.domain.model

/**
 * Combined search result containing songs and playlists.
 */
data class SearchResult(
    val songs: List<Song>,
    val playlists: List<YtmPlaylist>
)
