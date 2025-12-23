package com.frq.ytmusic.presentation.ytmplaylist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.frq.ytmusic.domain.model.Song
import com.frq.ytmusic.presentation.common.components.SongItem

@Composable
fun YtmPlaylistDetailScreen(
    onBack: () -> Unit,
    onSongClick: (List<Song>, Int) -> Unit,
    viewModel: YtmPlaylistViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberLazyListState()
    
    // Show title in header when scrolled
    val showHeaderTitle by remember {
        derivedStateOf {
            scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 500
        }
    }
    
    // Background alpha for header bar
    val headerBgAlpha by remember {
        derivedStateOf {
            if (scrollState.firstVisibleItemIndex > 0) 1f
            else (scrollState.firstVisibleItemScrollOffset / 400f).coerceIn(0f, 1f)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        Color.Black,
                        Color.Black
                    ),
                    startY = 0f,
                    endY = 800f
                )
            )
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }
            
            uiState.error != null -> {
                Text(
                    text = uiState.error ?: "Error loading playlist",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            uiState.playlist != null -> {
                val playlist = uiState.playlist!!
                
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 56.dp, bottom = 100.dp)
                ) {
                    // Header Section
                    item {
                        PlaylistHeader(
                            playlist = playlist,
                            uiState = uiState,
                            onSave = { viewModel.savePlaylist() },
                            onDownload = { viewModel.downloadAll() },
                            onPlay = { 
                                if (playlist.songs.isNotEmpty()) {
                                    onSongClick(playlist.songs, 0)
                                }
                            }
                        )
                    }
                    
                    // Songs List
                    itemsIndexed(playlist.songs) { index, song ->
                        SongItem(
                            song = song,
                            onClick = { onSongClick(playlist.songs, index) },
                            isPlaying = false
                        )
                    }
                }
            }
        }
        
        // Header bar overlay - same style as LikedSongsScreen
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = headerBgAlpha))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack, 
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            
            // Title (animated)
            AnimatedVisibility(
                visible = showHeaderTitle,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = uiState.playlist?.title ?: "",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
            
            // Spacer when title not visible
            if (!showHeaderTitle) {
                Spacer(modifier = Modifier.weight(1f))
            }
            
            // Menu button
            IconButton(onClick = { /* TODO: Menu */ }) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Menu",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun PlaylistHeader(
    playlist: com.frq.ytmusic.domain.model.YtmPlaylistDetail,
    uiState: YtmPlaylistUiState,
    onSave: () -> Unit,
    onDownload: () -> Unit,
    onPlay: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Thumbnail
        AsyncImage(
            model = playlist.thumbnailUrl,
            contentDescription = playlist.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.DarkGray)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Title
        Text(
            text = playlist.title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Author
        Text(
            text = playlist.author ?: "Unknown",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Stats
        Text(
            text = "${playlist.songCount} songs",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Save Button with State
            ActionButton(
                icon = if (uiState.isSaved) Icons.Default.Check else Icons.AutoMirrored.Filled.PlaylistAdd,
                label = if (uiState.isSaved) "Saved" else "Save",
                isLoading = uiState.isSaving,
                isActive = uiState.isSaved,
                onClick = onSave
            )
            
            // Download
            ActionButton(
                icon = if (uiState.isDownloading) Icons.Default.Download else Icons.Default.Download, // Could change icon if downloaded
                label = "Download",
                isActive = uiState.isDownloading,
                onClick = onDownload
            )
            
            // Share
            ActionButton(
                icon = Icons.Default.Share,
                label = "Share",
                onClick = { /* TODO */ }
            )
            
            // More
            ActionButton(
                icon = Icons.Default.MoreVert,
                label = "More",
                onClick = { /* TODO */ }
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Playback Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Shuffle Button
            Button(
                onClick = { /* TODO: Shuffle */ },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.1f), // Glassy look
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp) // Capsule shape
            ) {
                Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Acak", fontWeight = FontWeight.SemiBold)
            }
            
            // Play Button
            Button(
                onClick = onPlay,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(24.dp) // Capsule shape
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Putar", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isLoading: Boolean = false,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        androidx.compose.material3.Surface(
            onClick = onClick,
            shape = androidx.compose.foundation.shape.CircleShape,
            color = if (isActive) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.1f),
            contentColor = if (isActive) MaterialTheme.colorScheme.onPrimary else Color.White,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = if (isActive) MaterialTheme.colorScheme.onPrimary else Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        icon, 
                        contentDescription = label,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label, 
            style = MaterialTheme.typography.labelSmall, 
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 11.sp
        )
    }
}
