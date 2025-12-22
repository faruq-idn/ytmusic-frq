package com.frq.ytmusic.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Cross-reference entity for many-to-many relationship between Playlist and Songs.
 * Stores song metadata directly to avoid joining with external sources.
 */
@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "videoId"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("playlistId"), Index("videoId")]
)
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val videoId: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String?,
    val duration: String?,
    val position: Int,
    val addedAt: Long = System.currentTimeMillis()
)
