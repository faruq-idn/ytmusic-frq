package com.frq.ytmusic.presentation.collection

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.frq.ytmusic.presentation.collection.components.CollectionItem
import com.frq.ytmusic.presentation.downloads.DownloadsViewModel
import com.frq.ytmusic.presentation.library.LibraryViewModel
import com.frq.ytmusic.presentation.playlist.PlaylistViewModel
import com.frq.ytmusic.presentation.playlist.components.CreatePlaylistDialog

@Composable
fun CollectionScreen(
    downloadsViewModel: DownloadsViewModel = hiltViewModel(),
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    playlistViewModel: PlaylistViewModel = hiltViewModel(),
    onDownloadsClick: () -> Unit,
    onLikedSongsClick: () -> Unit,
    onPlaylistClick: (Long) -> Unit,
    hasMiniPlayer: Boolean = false
) {
    val downloadsState by downloadsViewModel.uiState.collectAsState()
    val libraryState by libraryViewModel.uiState.collectAsState()
    val playlistState by playlistViewModel.uiState.collectAsState()
    
    var showCreateDialog by remember { mutableStateOf(false) }
    
    val fabBottomPadding = if (hasMiniPlayer) 72.dp else 0.dp

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(bottom = fabBottomPadding)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Playlist")
            }
        }
    ) { _ ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 0.dp)
        ) {
            // Downloads
            item {
                CollectionItem(
                    title = "Downloads",
                    songCount = downloadsState.downloads.size,
                    thumbnailUrls = downloadsState.downloads.take(4).map { it.thumbnailUrl },
                    onClick = onDownloadsClick
                )
            }
            
            // Liked Songs
            item {
                CollectionItem(
                    title = "Liked Songs",
                    songCount = libraryState.favorites.size,
                    thumbnailUrls = libraryState.favorites.take(4).map { it.thumbnailUrl },
                    onClick = onLikedSongsClick
                )
            }
            
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Text(
                    text = "Playlists",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            // User Playlists
            items(playlistState.playlists) { playlist ->
                CollectionItem(
                    title = playlist.name,
                    songCount = playlist.songCount,
                    thumbnailUrls = listOfNotNull(playlist.thumbnailUrl), // Single thumbnail for now
                    onClick = { onPlaylistClick(playlist.id) }
                )
            }
        }
    }
    
    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, description ->
                playlistViewModel.createPlaylist(name, description)
                showCreateDialog = false
            }
        )
    }
}
