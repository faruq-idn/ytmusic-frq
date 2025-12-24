package com.frq.ytmusic.presentation.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.frq.ytmusic.presentation.common.components.PlayingIndicator
import com.frq.ytmusic.presentation.player.components.MiniPlayerControls
import com.frq.ytmusic.presentation.player.components.PlaybackControls
import com.frq.ytmusic.presentation.player.components.PlayerBottomTabs
import com.frq.ytmusic.presentation.player.components.PlayerHeader
import com.frq.ytmusic.presentation.player.components.PlayerSeekBar
import com.frq.ytmusic.presentation.player.components.PlayerSongInfo
import com.frq.ytmusic.presentation.player.components.SleepTimerDialog

/**
 * Morphing player that transitions between mini and full player states.
 * Progress: 0 = collapsed (mini), 1 = expanded (full)
 */
@Composable
fun MorphingPlayer(
    playerViewModel: PlayerViewModel,
    isFavorite: Boolean,
    progress: Float,
    activeTab: String?,
    onTabClick: (String) -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    onArtistClick: (artistId: String?, artistName: String) -> Unit = { _, _ -> }
) {
    val playerState by playerViewModel.playerState.collectAsState()
    val song = playerState.currentSong ?: return
    
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    
    val configuration = LocalConfiguration.current
    val screenHeight = remember(configuration) { configuration.screenHeightDp.dp }
    val screenWidth = remember(configuration) { configuration.screenWidthDp.dp }
    
    // Static dimensions - calculated once
    val miniPlayerHeight = 64.dp
    val maxArtworkSize = remember(screenWidth) { screenWidth - 48.dp }
    
    // Interpolate dimensions - direct calculation for smooth animation
    val artworkSize = lerp(48.dp, maxArtworkSize, progress)
    val artworkCorner = lerp(4.dp, 12.dp, progress)
    val artworkPaddingStart = lerp(8.dp, 24.dp, progress)
    val artworkPaddingTop = lerp(8.dp, 120.dp, progress)
    
    // Gradient brush - needs to update with progress for visual effect
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val gradientAlpha = 0.8f * progress
    val brush = Brush.verticalGradient(
        colors = listOf(
            primaryColor.copy(alpha = gradientAlpha),
            surfaceColor,
            surfaceColor
        )
    )
    
    val density = LocalDensity.current
    val draggableHeight = remember(screenHeight, density) {
        with(density) { (screenHeight - miniPlayerHeight).toPx() }
    }

    
    Box(
        modifier = Modifier
            .fillMaxSize() // Always fill max size, we slide it via offset in MainScreen
            .background(MaterialTheme.colorScheme.surface)
            .background(brush)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { onDragStart() },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() },
                    onVerticalDrag = { _, dragAmount -> 
                        val progressDelta = -dragAmount / draggableHeight
                        onDrag(progressDelta) 
                    }
                )
            }
            .clickable(
                enabled = progress < 0.5f,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onExpand() }
    ) {
        // Mini player progress bar
        if (progress < 0.5f) {
            LinearProgressIndicator(
                progress = { playerState.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.TopCenter)
                    .alpha(1f - progress * 2f),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.Transparent,
            )
        }
        
        // Morphing Artwork
        Box(
            modifier = Modifier
                .padding(start = artworkPaddingStart, top = artworkPaddingTop)
                .size(artworkSize)
                .clip(RoundedCornerShape(artworkCorner))
                .background(Color.DarkGray)
                .zIndex(10f),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = song.thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Playing Indicator (mini player)
            if (progress < 0.5f && playerState.isPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .alpha(1f - progress * 2f),
                    contentAlignment = Alignment.Center
                ) {
                    PlayingIndicator(maxHeight = 16.dp)
                }
            }
        }
        
        // Mini player controls
        if (progress < 0.5f) {
            val playPause = remember(playerViewModel) { { playerViewModel.togglePlayPause() } }
            val playPrev = remember(playerViewModel) { { playerViewModel.playPrevious() } }
            val playNext = remember(playerViewModel) { { playerViewModel.playNext() } }
            
            MiniPlayerControls(
                title = song.title,
                artist = song.artist,
                isPlaying = playerState.isPlaying,
                alpha = 1f - progress * 2f,
                onPrevious = playPrev,
                onPlayPause = playPause,
                onNext = playNext
            )
        }
        
        // Full player content
        if (progress > 0.3f) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(horizontal = 24.dp)
                    .alpha((progress - 0.3f) / 0.7f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PlayerHeader(
                    isSleepTimerActive = playerState.sleepTimerMinutes != null,
                    onCollapse = onCollapse,
                    onSleepTimerClick = { showSleepTimerDialog = true }
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val toggleFav = remember(playerViewModel) { { playerViewModel.toggleFavorite() } }
                    val toggleDown = remember(playerViewModel) { { playerViewModel.toggleDownload() } }
                    
                    PlayerSongInfo(
                        title = song.title,
                        artist = song.artist,
                        isFavorite = isFavorite,
                        downloadState = playerState.downloadState,
                        onFavoriteClick = toggleFav,
                        onDownloadClick = toggleDown,
                        onArtistClick = { onArtistClick(song.artistId, song.artist) }
                    )
                    
                    val seekTo = remember(playerViewModel) { { pos: Float -> playerViewModel.seekTo(pos) } }
                    
                    PlayerSeekBar(
                        progress = playerState.progress,
                        currentPosition = playerState.currentPosition,
                        duration = playerState.duration,
                        onSeek = seekTo
                    )
                    
                    val playPause = remember(playerViewModel) { { playerViewModel.togglePlayPause() } }
                    val playPrev = remember(playerViewModel) { { playerViewModel.playPrevious() } }
                    val playNext = remember(playerViewModel) { { playerViewModel.playNext() } }
                    val toggleShuffle = remember(playerViewModel) { { playerViewModel.toggleShuffle() } }
                    val toggleRepeat = remember(playerViewModel) { { playerViewModel.toggleRepeat() } }
                    
                    PlaybackControls(
                        isPlaying = playerState.isPlaying,
                        isLoading = playerState.isLoading,
                        isShuffleEnabled = playerState.isShuffleEnabled,
                        repeatMode = playerState.repeatMode,
                        onPlayPause = playPause,
                        onPrevious = playPrev,
                        onNext = playNext,
                        onShuffle = toggleShuffle,
                        onRepeat = toggleRepeat
                    )
                    
                    PlayerBottomTabs(
                        activeTab = activeTab,
                        onTabClick = onTabClick
                    )
                }
            }
        }
    }
    
    // Sleep Timer Dialog
    if (showSleepTimerDialog) {
        SleepTimerDialog(
            currentTimerMinutes = playerState.sleepTimerMinutes,
            remainingTimeText = playerViewModel.getSleepTimerRemainingText(),
            onSelectTimer = { minutes ->
                playerViewModel.setSleepTimer(minutes)
                showSleepTimerDialog = false
            },
            onCancelTimer = {
                playerViewModel.cancelSleepTimer()
                showSleepTimerDialog = false
            },
            onDismiss = { showSleepTimerDialog = false }
        )
    }
}
