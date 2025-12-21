package com.frq.ytmusic.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.frq.ytmusic.presentation.player.MiniPlayer
import com.frq.ytmusic.presentation.player.PlayerViewModel
import com.frq.ytmusic.presentation.search.SearchScreen

/**
 * Main screen composable that combines Search and MiniPlayer.
 */
@Composable
fun MainScreen(
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    Scaffold(
        bottomBar = {
            MiniPlayer(viewModel = playerViewModel)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchScreen(
                onSongClick = { song ->
                    playerViewModel.playSong(song)
                }
            )
        }
    }
}
