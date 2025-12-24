package com.frq.ytmusic.domain.model

/**
 * Domain model for YouTube Music artist.
 */
data class YtmArtist(
    val browseId: String,
    val name: String,
    val thumbnailUrl: String,
    val subscribers: String?
)
