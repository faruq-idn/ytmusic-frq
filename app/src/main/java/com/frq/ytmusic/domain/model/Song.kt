package com.frq.ytmusic.domain.model

/**
 * Domain model for a Song.
 * Clean representation used by the UI layer.
 */
data class Song(
    val videoId: String,
    val title: String,
    val artist: String,
    val artistId: String? = null,  // browseId for direct artist navigation
    val album: String? = null,
    val durationText: String? = null,
    val thumbnailUrl: String? = null
)
