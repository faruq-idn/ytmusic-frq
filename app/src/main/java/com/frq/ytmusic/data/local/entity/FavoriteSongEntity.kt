package com.frq.ytmusic.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for favorite songs.
 */
@Entity(tableName = "favorite_songs")
data class FavoriteSongEntity(
    @PrimaryKey
    val videoId: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String?,
    val duration: String?,
    val addedAt: Long = System.currentTimeMillis()
)
