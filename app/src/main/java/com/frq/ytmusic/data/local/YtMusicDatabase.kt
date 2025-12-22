package com.frq.ytmusic.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.frq.ytmusic.data.local.dao.DownloadedSongDao
import com.frq.ytmusic.data.local.dao.FavoriteSongDao
import com.frq.ytmusic.data.local.dao.PlaylistDao
import com.frq.ytmusic.data.local.entity.DownloadedSongEntity
import com.frq.ytmusic.data.local.entity.FavoriteSongEntity
import com.frq.ytmusic.data.local.entity.PlaylistEntity
import com.frq.ytmusic.data.local.entity.PlaylistSongCrossRef

/**
 * Room database for YT Music app.
 */
@Database(
    entities = [
        FavoriteSongEntity::class,
        DownloadedSongEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class
    ],
    version = 3,
    exportSchema = false
)
abstract class YtMusicDatabase : RoomDatabase() {
    abstract fun favoriteSongDao(): FavoriteSongDao
    abstract fun downloadedSongDao(): DownloadedSongDao
    abstract fun playlistDao(): PlaylistDao
}

