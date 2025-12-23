package com.frq.ytmusic.domain.model

/**
 * Represents a playlist from YouTube Music search results.
 * Different from local Playlist which is user-created.
 */
data class YtmPlaylist(
    val playlistId: String,
    val title: String,
    val description: String? = null,
    val thumbnailUrl: String,
    val songCount: Int? = null,
    val author: String? = null
)
