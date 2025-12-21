package com.frq.ytmusic.di

import android.content.Context
import com.frq.ytmusic.service.MusicServiceConnection
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for service dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Provides
    @Singleton
    fun provideMusicServiceConnection(
        @ApplicationContext context: Context
    ): MusicServiceConnection {
        return MusicServiceConnection(context)
    }
}
