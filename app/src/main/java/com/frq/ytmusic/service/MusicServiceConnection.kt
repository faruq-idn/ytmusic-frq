package com.frq.ytmusic.service

import android.content.ComponentName
import android.content.Context
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages connection to MusicPlaybackService's MediaSession.
 * Provides MediaController for playback control.
 */
@Singleton
class MusicServiceConnection @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    
    private val _controller = MutableStateFlow<MediaController?>(null)
    val controller: StateFlow<MediaController?> = _controller.asStateFlow()

    private val sessionToken = SessionToken(
        context,
        ComponentName(context, MusicPlaybackService::class.java)
    )

    /**
     * Connect to the MediaSession service.
     */
    fun connect() {
        if (controllerFuture != null) return
        
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener(
            {
                try {
                    _controller.value = controllerFuture?.get()
                } catch (e: Exception) {
                    _controller.value = null
                }
            },
            MoreExecutors.directExecutor()
        )
    }

    /**
     * Disconnect from the MediaSession service.
     */
    fun disconnect() {
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
        controllerFuture = null
        _controller.value = null
    }
}
