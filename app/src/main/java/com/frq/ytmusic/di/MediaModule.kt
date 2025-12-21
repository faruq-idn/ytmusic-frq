package com.frq.ytmusic.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for media playback dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object MediaModule {

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideAudioAttributes(): AudioAttributes {
        return AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()
    }

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideExoPlayer(
        @ApplicationContext context: Context,
        audioAttributes: AudioAttributes
    ): ExoPlayer {
        return ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true) // Handle audio focus
            .setHandleAudioBecomingNoisy(true) // Pause when headphones unplugged
            .build()
    }
}
