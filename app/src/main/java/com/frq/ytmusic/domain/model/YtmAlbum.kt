package com.frq.ytmusic.domain.model

/**
 * Domain model for YouTube Music album from search results.
 */
data class YtmAlbum(
    val browseId: String,
    val title: String,
    val artist: String?,
    val thumbnailUrl: String,
    val year: String?,
    val isExplicit: Boolean = false
)
