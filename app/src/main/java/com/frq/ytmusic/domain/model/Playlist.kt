package com.frq.ytmusic.domain.model

/**
 * Domain model for a playlist.
 */
data class Playlist(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val songCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
