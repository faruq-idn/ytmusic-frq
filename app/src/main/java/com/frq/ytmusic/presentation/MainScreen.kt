package com.frq.ytmusic.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.frq.ytmusic.presentation.common.components.PlayingIndicator
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.frq.ytmusic.presentation.navigation.BottomNavBar
import com.frq.ytmusic.presentation.navigation.NavGraph
import com.frq.ytmusic.presentation.player.PlayerViewModel
import java.util.Locale

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
                        dragProgress = 0f
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
                        dragProgress = 0f
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
                UnifiedMorphingPlayer(
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
                    onExpand = {
                        isDragging = false
                        targetProgress = 1f
                    },
                    onCollapse = {
                        isDragging = false
                        targetProgress = 0f
                    }
                )
            }
        }
        
        // Fullscreen Tab Overlays (Queue, Lyrics, Related)
        androidx.compose.animation.AnimatedVisibility(
            visible = activeTab != null,
            enter = androidx.compose.animation.slideInVertically { it },
            exit = androidx.compose.animation.slideOutVertically { it },
            modifier = Modifier.fillMaxSize().zIndex(50f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .systemBarsPadding()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // "Mini Player" Header
                    if (song != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp) // Slightly taller for artwork
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Artwork
                            AsyncImage(
                                model = song.thumbnailUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.DarkGray)
                            )
                            
                            // Song Info
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = when(activeTab) {
                                        "queue" -> "Antrean"
                                        "lyrics" -> "Lirik"
                                        "related" -> "Terkait"
                                        else -> ""
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            
                            // Play/Pause Control in Header
                            IconButton(onClick = { playerViewModel.togglePlayPause() }) {
                                Icon(
                                    imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = Color.White
                                )
                            }
                            
                            // Close Button (Minimize Overlay)
                            IconButton(onClick = { activeTab = null }) {
                                Icon(Icons.Default.KeyboardArrowDown, "Close", tint = Color.White)
                            }
                        }
                        
                        Divider(color = Color.White.copy(alpha = 0.1f))
                    }
                    
                    // Content Area
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Konten ${activeTab}...",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UnifiedMorphingPlayer(
    playerViewModel: PlayerViewModel,
    isFavorite: Boolean,
    progress: Float, // 0 = mini, 1 = full
    activeTab: String?,
    onTabClick: (String) -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onExpand: () -> Unit,
    onCollapse: () -> Unit
) {
    val playerState by playerViewModel.playerState.collectAsState()
    val song = playerState.currentSong ?: return
    
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    
    // Interpolate dimensions
    val miniPlayerHeight = 64.dp
    val playerHeight = lerp(miniPlayerHeight, screenHeight, progress)
    
    val artworkSize = lerp(48.dp, 280.dp, progress)
    val artworkCorner = lerp(4.dp, 12.dp, progress)
    val artworkPaddingStart = lerp(8.dp, (screenWidth - 280.dp) / 2, progress)
    val artworkPaddingTop = lerp(8.dp, 100.dp, progress)
    
    // Background gradient - stronger when expanded
    val gradientAlpha = 0.5f * progress
    val brush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = gradientAlpha),
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surface
        )
    )
    
    // Dynamic Drag Sensitivity to make it feel 1:1
    val draggableHeight = with(LocalDensity.current) { (screenHeight - miniPlayerHeight).toPx() }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(playerHeight)
            .background(MaterialTheme.colorScheme.surface)
            .background(brush)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { onDragStart() },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() },
                    onVerticalDrag = { _, dragAmount -> 
                        // Drag amount in pixels. Negative = Up.
                        // Progress = -delta / totalHeight
                        val progressDelta = -dragAmount / draggableHeight
                        onDrag(progressDelta) 
                    }
                )
            }
            // Remove ripple effect
            .clickable(
                enabled = progress < 0.5f,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onExpand() }
            // Apply padding to content instead of container to avoid jumps
    ) {
        // Mini player progress bar (visible when collapsed)
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
        
        // Morphing Artwork Container
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
            
            // Playing Indicator (visible only when collapsed and playing)
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
        
        // Mini player controls (visible when collapsed)
        if (progress < 0.5f) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 68.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                    .alpha(1f - progress * 2f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                IconButton(onClick = { playerViewModel.playPrevious() }) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous")
                }
                IconButton(onClick = { playerViewModel.togglePlayPause() }) {
                    Icon(
                        imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (playerState.isPlaying) "Pause" else "Play"
                    )
                }
                IconButton(onClick = { playerViewModel.playNext() }) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next")
                }
            }
        }
        
        // Full player content (visible when expanded)
        if (progress > 0.3f) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding() // Safe insets
                    .padding(horizontal = 24.dp)
                    .alpha((progress - 0.3f) / 0.7f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onCollapse) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Minimize",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                    }
                }
                
                // Flexible spacer to push content down, respecting Artwork area
                // Artwork lands at ~100dp top + 280dp size. 
                // We use weight to push the controls to the bottom half.
                Spacer(modifier = Modifier.weight(1f))
                
                // Content Container
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Song Info & Main Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                modifier = Modifier.basicMarquee()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = song.artist,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        // Like/Dislike & Download area
                        Row(verticalAlignment = Alignment.CenterVertically) {
                             com.frq.ytmusic.presentation.common.DownloadButton(
                                downloadState = playerState.downloadState,
                                onClick = { playerViewModel.toggleDownload() }
                            )
                             
                             IconButton(onClick = { playerViewModel.toggleFavorite() }) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Filled.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isFavorite) Color.White else Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                    
                    // Seekbar
                    Column(modifier = Modifier.fillMaxWidth()) {
                        var sliderPosition by remember { mutableFloatStateOf(0f) }
                        var isDragging by remember { mutableStateOf(false) }
                        
                        Slider(
                            value = if (isDragging) sliderPosition else playerState.progress,
                            onValueChange = { isDragging = true; sliderPosition = it },
                            onValueChangeFinished = { playerViewModel.seekTo(sliderPosition); isDragging = false },
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = Color.White,
                                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.height(16.dp)
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                formatTime(playerState.currentPosition),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                formatTime(playerState.duration),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                    
                    // Playback Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { playerViewModel.toggleShuffle() }) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = "Shuffle",
                                tint = if (playerState.isShuffleEnabled) Color.White else Color.White.copy(alpha = 0.4f)
                            )
                        }
                        
                        IconButton(
                            onClick = { playerViewModel.playPrevious() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Default.SkipPrevious,
                                contentDescription = "Previous",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        
                        // Play/Pause Circle
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable { playerViewModel.togglePlayPause() },
                            contentAlignment = Alignment.Center
                        ) {
                            if (playerState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    color = Color.Black
                                )
                            } else {
                                Icon(
                                    imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                                    tint = Color.Black,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                        
                        IconButton(
                            onClick = { playerViewModel.playNext() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Default.SkipNext,
                                contentDescription = "Next",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        
                        IconButton(onClick = { playerViewModel.toggleRepeat() }) {
                            Icon(
                                imageVector = when (playerState.repeatMode) {
                                    Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne
                                    else -> Icons.Default.Repeat
                                },
                                contentDescription = "Repeat",
                                tint = when (playerState.repeatMode) {
                                    Player.REPEAT_MODE_OFF -> Color.White.copy(alpha = 0.4f)
                                    else -> Color.White
                                }
                            )
                        }
                    }
                    
                    // Bottom Tabs (Lyrics, Up Next, etc)
                    val tabInteractionSource = remember { MutableInteractionSource() }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text(
                            text = "BERIKUTNYA",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if(activeTab == "queue") Color.White else Color.White.copy(alpha = 0.7f),
                            modifier = Modifier
                                .padding(8.dp)
                                .clickable(
                                    interactionSource = tabInteractionSource,
                                    indication = null
                                ) { onTabClick("queue") }
                        )
                        Text(
                            text = "LIRIK",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if(activeTab == "lyrics") Color.White else Color.White.copy(alpha = 0.7f),
                            modifier = Modifier
                                .padding(8.dp)
                                .clickable(
                                    interactionSource = tabInteractionSource,
                                    indication = null
                                ) { onTabClick("lyrics") }
                        )
                        Text(
                            text = "TERKAIT",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if(activeTab == "related") Color.White else Color.White.copy(alpha = 0.7f),
                            modifier = Modifier
                                .padding(8.dp)
                                .clickable(
                                    interactionSource = tabInteractionSource,
                                    indication = null
                                ) { onTabClick("related") }
                        )
                    }
                }
            }
        }
    }
}


private fun formatTime(ms: Long): String {
    val minutes = (ms / 1000) / 60
    val seconds = (ms / 1000) % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}
