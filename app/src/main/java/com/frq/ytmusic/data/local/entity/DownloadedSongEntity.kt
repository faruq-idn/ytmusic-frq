package com.frq.ytmusic.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for downloaded songs.
 */
@Entity(tableName = "downloaded_songs")
data class DownloadedSongEntity(
    @PrimaryKey
    val videoId: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String?,
    val duration: String?,
    val filePath: String,
    val fileSize: Long,
    val downloadedAt: Long = System.currentTimeMillis()
)
