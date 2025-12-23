package com.frq.ytmusic.domain.model

/**
 * Domain model for YouTube Music album detail.
 */
data class YtmAlbumDetail(
    val browseId: String,
    val title: String,
    val artist: String?,
    val thumbnailUrl: String,
    val year: String?,
    val trackCount: Int,
    val duration: String?,
    val songs: List<Song>
)
