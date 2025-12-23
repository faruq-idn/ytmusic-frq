package com.frq.ytmusic.domain.model

/**
 * Detailed playlist with songs from YouTube Music.
 */
data class YtmPlaylistDetail(
    val playlistId: String,
    val title: String,
    val description: String? = null,
    val thumbnailUrl: String,
    val author: String? = null,
    val songCount: Int,
    val songs: List<Song>
)
