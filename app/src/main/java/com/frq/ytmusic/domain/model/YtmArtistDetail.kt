package com.frq.ytmusic.domain.model

/**
 * Domain model for YouTube Music artist detail.
 */
data class YtmArtistDetail(
    val browseId: String,
    val name: String,
    val thumbnailUrl: String,
    val description: String?,
    val subscribers: String?,
    val songs: List<Song>,
    val albums: List<YtmAlbum>
)
