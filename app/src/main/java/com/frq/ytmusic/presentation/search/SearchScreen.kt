package com.frq.ytmusic.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.frq.ytmusic.domain.model.Song
import com.frq.ytmusic.presentation.common.components.ShimmerSongList
import com.frq.ytmusic.presentation.common.components.SongItem
import com.frq.ytmusic.presentation.playlist.PlaylistViewModel
import com.frq.ytmusic.presentation.playlist.components.AddToPlaylistDialog
import com.frq.ytmusic.presentation.playlist.components.CreatePlaylistDialog
import com.frq.ytmusic.presentation.search.components.PlaylistItem
import com.frq.ytmusic.presentation.search.components.AlbumItem
import com.frq.ytmusic.presentation.search.components.ArtistItem
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.onFocusChanged

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    playlistViewModel: PlaylistViewModel = hiltViewModel(),
    onSongClick: (List<Song>, Int) -> Unit,
    onPlaylistClick: (String) -> Unit = {},
    onAlbumClick: (String) -> Unit = {},
    onArtistClick: (String) -> Unit = {},
    activeSongId: String? = null,
    isPlaying: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()

    // Remembered callbacks to avoid recomposition
    val hideKeyboard = remember(keyboardController, focusManager) {
        {
            keyboardController?.hide()
            focusManager.clearFocus()
        }
    }

    // Dialog States
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var activeSongToAdd by remember { mutableStateOf<Song?>(null) }
    
    // Expand states for sections
    var playlistsExpanded by remember { mutableStateOf(false) }
    var albumsExpanded by remember { mutableStateOf(false) }
    var artistsExpanded by remember { mutableStateOf(false) }


    // Hide keyboard when scrolling
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .collect { isScrolling ->
                if (isScrolling) {
                    hideKeyboard()
                }
            }
    }

    Scaffold(
        topBar = {
            SearchBar(
                query = uiState.query,
                onQueryChange = viewModel::onQueryChange,
                onClearClick = viewModel::clearSearch,
                onFocused = viewModel::onSearchBarFocused,
                onSearchAction = {
                    viewModel.search()
                    hideKeyboard()
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        hideKeyboard()
                    })
                }
        ) {
            when {
                // Loading State
                uiState.isLoading -> {
                    ShimmerSongList(modifier = Modifier.padding(top = 8.dp))
                }

                // Error State
                uiState.error != null -> {
                    Text(
                        text = uiState.error ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Empty State
                uiState.showEmptyState -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No songs found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Initial State
                uiState.showInitialState -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Start searching for music",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Suggestions State
                uiState.showSuggestions -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(uiState.suggestions) { suggestion ->
                            SuggestionItem(
                                suggestion = suggestion,
                                onClick = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                    viewModel.selectSuggestion(suggestion)
                                }
                            )
                        }
                    }
                }

                // Success State (Results)
                uiState.showResults -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        // Top 3 Songs Section (at the very top)
                        if (uiState.songs.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Lagu Teratas",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                            val topSongs = uiState.songs.take(3)
                            itemsIndexed(
                                items = topSongs,
                                key = { index, song -> "top_${song.videoId}" }
                            ) { index, song ->
                                SongItem(
                                    song = song,
                                    onClick = { 
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                        onSongClick(uiState.songs, index) 
                                    },
                                    isPlaying = activeSongId == song.videoId && isPlaying,
                                    onAddToPlaylist = {
                                        activeSongToAdd = song
                                        showAddToPlaylistDialog = true
                                    }
                                )
                            }
                        }
                        
                        // Playlists Section
                        if (uiState.playlists.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Playlists",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                            val playlistsToShow = if (playlistsExpanded) uiState.playlists else uiState.playlists.take(3)
                            items(
                                items = playlistsToShow,
                                key = { "playlist_${it.playlistId}" }
                            ) { playlist ->
                                PlaylistItem(
                                    playlist = playlist,
                                    onClick = {
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                        onPlaylistClick(playlist.playlistId)
                                    }
                                )
                            }
                            // Show More / Less button
                            if (uiState.playlists.size > 3) {
                                item {
                                    TextButton(
                                        onClick = { playlistsExpanded = !playlistsExpanded },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp)
                                    ) {
                                        Text(
                                            text = if (playlistsExpanded) "Tampilkan Sedikit" else "Lihat Semua (${uiState.playlists.size})",
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Albums Section
                        if (uiState.albums.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Albums",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                            val albumsToShow = if (albumsExpanded) uiState.albums else uiState.albums.take(3)
                            items(
                                items = albumsToShow,
                                key = { "album_${it.browseId}" }
                            ) { album ->
                                AlbumItem(
                                    album = album,
                                    onClick = {
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                        onAlbumClick(album.browseId)
                                    }
                                )
                            }
                            // Show More / Less button
                            if (uiState.albums.size > 3) {
                                item {
                                    TextButton(
                                        onClick = { albumsExpanded = !albumsExpanded },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp)
                                    ) {
                                        Text(
                                            text = if (albumsExpanded) "Tampilkan Sedikit" else "Lihat Semua (${uiState.albums.size})",
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Artists Section (simple, separate - max 3)
                        if (uiState.artists.isNotEmpty()) {
                            item(key = "artists_header") {
                                Text(
                                    text = "Artis",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                            items(
                                items = uiState.artists.take(3),  // Limit to max 3
                                key = { "artist_${it.browseId}" }
                            ) { artist ->
                                ArtistItem(
                                    artist = artist,
                                    onClick = {
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                        onArtistClick(artist.browseId)
                                    }
                                )
                            }
                        }
                        
                        // Remaining Songs Section
                        if (uiState.songs.size > 3) {
                            item(key = "remaining_songs_header") {
                                Text(
                                    text = "Lagu Lainnya",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                            
                            val remainingSongs = uiState.songs.drop(3)
                            itemsIndexed(
                                items = remainingSongs,
                                key = { _, song -> "song_${song.videoId}" }
                            ) { localIndex, song ->
                                val fullIndex = localIndex + 3
                                SongItem(
                                    song = song,
                                    onClick = { 
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                        onSongClick(uiState.songs, fullIndex) 
                                    },
                                    isPlaying = activeSongId == song.videoId && isPlaying,
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
                showCreatePlaylistDialog = false
            }
        )
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit,
    onFocused: () -> Unit,
    onSearchAction: () -> Unit
) {
    var hasFocusedOnce by remember { mutableStateOf(false) }
    
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .onFocusChanged { focusState ->
                if (focusState.isFocused && hasFocusedOnce) {
                    // Only trigger on re-focus, not initial focus
                    onFocused()
                }
                if (focusState.isFocused) {
                    hasFocusedOnce = true
                }
            },
        placeholder = { Text("Search songs...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search")
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClearClick) {
                    Icon(Icons.Default.Close, contentDescription = "Clear")
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearchAction() }),
        shape = RoundedCornerShape(24.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        )
    )
}

@Composable
fun SuggestionItem(
    suggestion: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = suggestion,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
