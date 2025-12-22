package com.frq.ytmusic.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.frq.ytmusic.presentation.navigation.BottomNavBar
import com.frq.ytmusic.presentation.navigation.NavGraph
import com.frq.ytmusic.presentation.player.MorphingPlayer
import com.frq.ytmusic.presentation.player.PlayerViewModel
import com.frq.ytmusic.presentation.player.components.TabOverlayContent


@Composable
fun MainScreen(
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val playerState by playerViewModel.playerState.collectAsState()
    val isFavorite by playerViewModel.isFavorite.collectAsState()
    val song = playerState.currentSong
    
    // 0 = collapsed (mini), 1 = expanded (full)
    var targetProgress by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }
    var lastDragDelta by remember { mutableFloatStateOf(0f) }
    var activeTab by remember { mutableStateOf<String?>(null) }
    
    // Only animate when NOT dragging - instant response during drag
    val animatedProgress by animateFloatAsState(
        targetValue = if (isDragging) dragProgress else targetProgress,
        animationSpec = if (isDragging) tween(0) else tween(200),
        label = "player_progress"
    )
    
    // Use direct progress during drag for instant response
    val currentProgress = if (isDragging) dragProgress else animatedProgress

    // Handle back press when player is expanded
    BackHandler(enabled = targetProgress > 0.5f) {
        targetProgress = 0f
    }

    // Also handle back when tab overlay is open
    BackHandler(enabled = activeTab != null) {
        activeTab = null
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                // Keep BottomNavBar always in layout to prevent jumps, just fade it
                BottomNavBar(
                    navController = navController,
                    modifier = Modifier.alpha(1f - animatedProgress * 3f)
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // NavGraph Content
                NavGraph(
                    navController = navController,
                    onSongClick = { songs, index ->
                        playerViewModel.playSongFromList(songs, index)
                        targetProgress = 1f
                    },
                    onDownloadPlay = { downloadedSong ->
                        val songModel = com.frq.ytmusic.domain.model.Song(
                            videoId = downloadedSong.videoId,
                            title = downloadedSong.title,
                            artist = downloadedSong.artist,
                            album = null,
                            durationText = downloadedSong.duration,
                            thumbnailUrl = downloadedSong.thumbnailUrl
                        )
                        // Play from local file for instant playback
                        playerViewModel.playLocalFile(songModel, downloadedSong.filePath)
                        targetProgress = 1f
                    },
                    activeSongId = song?.videoId,
                    isPlaying = playerState.isPlaying
                )
            }
        }
        
        // Unified Morphing Player - positioned as Overlay
        if (song != null) {
            val systemNavHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            val navBarHeight = 80.dp + systemNavHeight
            val playerBottomPadding = lerp(navBarHeight, 0.dp, animatedProgress)
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = playerBottomPadding),
                contentAlignment = Alignment.BottomCenter
            ) {
                MorphingPlayer(
                    playerViewModel = playerViewModel,
                    isFavorite = isFavorite,
                    progress = currentProgress,
                    activeTab = activeTab,
                    onTabClick = { tab -> activeTab = tab },
                    onDragStart = {
                        isDragging = true
                        dragProgress = currentProgress
                    },
                    onDrag = { delta ->
                        // Add progressDelta directly
                        dragProgress = (dragProgress + delta).coerceIn(0f, 1f)
                        // Track last delta for direction
                        lastDragDelta = delta
                    },
                    onDragEnd = {
                        isDragging = false
                        // Direction-aware snap logic
                        // If pushing UP (delta > 0) and past 15% -> Expand
                        // If pushing DOWN (delta < 0) and past 15% (from top) -> Collapse
                        val isExpanding = lastDragDelta > 0
                        val movedMin = 0.15f
                        
                        targetProgress = when {
                            isExpanding && dragProgress > movedMin -> 1f
                            !isExpanding && dragProgress < (1f - movedMin) -> 0f
                            else -> if (dragProgress > 0.5f) 1f else 0f
                        }
                    },
                    onExpand = { targetProgress = 1f },
                    onCollapse = { targetProgress = 0f }
                )
            }
        }
        
        // Fullscreen Tab Overlays (Queue, Lyrics, Related)
        val currentTab = activeTab
        androidx.compose.animation.AnimatedVisibility(
            visible = currentTab != null && song != null,
            enter = androidx.compose.animation.slideInVertically { it },
            exit = androidx.compose.animation.slideOutVertically { it },
            modifier = Modifier.fillMaxSize().zIndex(50f)
        ) {
            if (currentTab != null && song != null) {
                TabOverlayContent(
                    activeTab = currentTab,
                    songTitle = song.title,
                    songThumbnailUrl = song.thumbnailUrl,
                    isPlaying = playerState.isPlaying,
                    onPlayPause = { playerViewModel.togglePlayPause() },
                    onClose = { activeTab = null },
                    // Queue
                    queue = playerState.queue,
                    currentIndex = playerState.currentIndex,
                    onPlayFromQueue = { index -> playerViewModel.playFromQueue(index) },
                    // Lyrics
                    lyrics = playerState.lyrics,
                    isLyricsLoading = playerState.isLyricsLoading,
                    currentPosition = playerState.currentPosition,
                    onSeekToLine = { positionMs -> playerViewModel.seekTo(positionMs) },
                    // Related
                    relatedSongs = playerState.relatedSongs,
                    isRelatedLoading = playerState.isRelatedLoading,
                    onPlayRelated = { song -> playerViewModel.playSong(song) }
                )
            }
        }
    }
}
