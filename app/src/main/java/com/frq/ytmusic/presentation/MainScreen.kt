package com.frq.ytmusic.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.frq.ytmusic.presentation.navigation.BottomNavBar
import com.frq.ytmusic.presentation.navigation.NavGraph
import com.frq.ytmusic.presentation.player.MiniPlayer
import com.frq.ytmusic.presentation.player.PlayerScreen
import com.frq.ytmusic.presentation.player.PlayerViewModel

@Composable
fun MainScreen(
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    var isPlayerExpanded by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            if (!isPlayerExpanded) {
                // Bottom Navigation + MiniPlayer Stack
                androidx.compose.foundation.layout.Column {
                    MiniPlayer(
                        viewModel = playerViewModel,
                        onExpand = { isPlayerExpanded = true }
                    )
                    BottomNavBar(navController = navController)
                }
            }
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
                    isPlayerExpanded = true
                },
                onDownloadPlay = { downloadedSong ->
                    val song = com.frq.ytmusic.domain.model.Song(
                        videoId = downloadedSong.videoId,
                        title = downloadedSong.title,
                        artist = downloadedSong.artist,
                        album = null,
                        durationText = downloadedSong.duration,
                        thumbnailUrl = downloadedSong.thumbnailUrl
                    )
                    playerViewModel.playSong(song)
                    isPlayerExpanded = true
                }
            )
            
            // Full Player Screen Overlay
            AnimatedVisibility(
                visible = isPlayerExpanded,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.fillMaxSize()
            ) {
                PlayerScreen(
                    viewModel = playerViewModel,
                    onCollapse = { isPlayerExpanded = false }
                )
            }
        }
    }
}
