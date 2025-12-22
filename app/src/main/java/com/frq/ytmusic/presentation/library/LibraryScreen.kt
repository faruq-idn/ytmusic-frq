package com.frq.ytmusic.presentation.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.frq.ytmusic.domain.model.Song
import com.frq.ytmusic.presentation.common.components.SongItem
import com.frq.ytmusic.presentation.playlist.PlaylistsScreen
import com.frq.ytmusic.presentation.playlist.PlaylistViewModel
import com.frq.ytmusic.presentation.playlist.components.AddToPlaylistDialog
import com.frq.ytmusic.presentation.playlist.components.CreatePlaylistDialog
import androidx.compose.runtime.mutableStateOf

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel(),
    playlistViewModel: PlaylistViewModel = hiltViewModel(),
    onSongClick: (List<Song>, Int) -> Unit,
    onPlaylistClick: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    
    // Dialog States
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var activeSongToAdd by remember { mutableStateOf<Song?>(null) }
    
    val tabs = listOf("Favorites", "Playlists")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> {
                // Favorites Tab
                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        uiState.isLoading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        uiState.isEmpty -> {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .padding(bottom = 16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = "No favorites yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Tap ❤️ on songs to add them here",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }

                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp) // Bottom padding for player
                            ) {
                                itemsIndexed(uiState.favorites) { index, song ->
                                    SongItem(
                                        song = song,
                                        onClick = { onSongClick(uiState.favorites, index) },
                                        onAddToPlaylist = {
                                            activeSongToAdd = song
                                            showAddToPlaylistDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // Playlists Tab
                PlaylistsScreen(
                    onPlaylistClick = onPlaylistClick
                )
            }
        }
    }

    if (showAddToPlaylistDialog && activeSongToAdd != null) {
        val playlistUiState by playlistViewModel.uiState.collectAsState()
        
        AddToPlaylistDialog(
            playlists = playlistUiState.playlists,
            onDismiss = { showAddToPlaylistDialog = false },
            onPlaylistSelect = { playlist ->
                playlistViewModel.addSongToPlaylist(playlist.id, activeSongToAdd!!)
                showAddToPlaylistDialog = false
            },
            onCreateNew = {
                showAddToPlaylistDialog = false
                showCreatePlaylistDialog = true
            }
        )
    }

    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            onCreate = { name, description ->
                playlistViewModel.createPlaylist(name, description)
                // Optionally reopen add dialog or just let user find it. 
                // For better UX, we might want to automatically add the song to the new playlist.
                // But for simplicity, we'll just close for now.
                showCreatePlaylistDialog = false
            }
        )
    }
}
