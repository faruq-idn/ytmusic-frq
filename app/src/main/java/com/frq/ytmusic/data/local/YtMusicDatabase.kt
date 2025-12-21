package com.frq.ytmusic.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.frq.ytmusic.data.local.dao.FavoriteSongDao
import com.frq.ytmusic.data.local.entity.FavoriteSongEntity

/**
 * Room database for YT Music app.
 */
@Database(
    entities = [FavoriteSongEntity::class],
    version = 1,
    exportSchema = false
)
abstract class YtMusicDatabase : RoomDatabase() {
    abstract fun favoriteSongDao(): FavoriteSongDao
}
