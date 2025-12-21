package com.frq.ytmusic.di

import android.content.Context
import androidx.room.Room
import com.frq.ytmusic.data.local.YtMusicDatabase
import com.frq.ytmusic.data.local.dao.DownloadedSongDao
import com.frq.ytmusic.data.local.dao.FavoriteSongDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for database dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): YtMusicDatabase {
        return Room.databaseBuilder(
            context,
            YtMusicDatabase::class.java,
            "ytmusic_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideFavoriteSongDao(database: YtMusicDatabase): FavoriteSongDao {
        return database.favoriteSongDao()
    }

    @Provides
    @Singleton
    fun provideDownloadedSongDao(database: YtMusicDatabase): DownloadedSongDao {
        return database.downloadedSongDao()
    }
}
