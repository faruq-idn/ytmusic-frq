package com.frq.ytmusic.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.frq.ytmusic.data.local.entity.DownloadedSongEntity
import com.frq.ytmusic.domain.model.Song
import com.frq.ytmusic.presentation.downloads.DownloadsScreen
import com.frq.ytmusic.presentation.library.LibraryScreen
import com.frq.ytmusic.presentation.search.SearchScreen

/**
 * Navigation graph for the app.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    onSongClick: (List<Song>, Int) -> Unit,
    onDownloadPlay: (DownloadedSongEntity) -> Unit,
    activeSongId: String? = null,
    isPlaying: Boolean = false
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Search.route
    ) {
        composable(Screen.Search.route) {
            SearchScreen(
                onSongClick = onSongClick,
                activeSongId = activeSongId,
                isPlaying = isPlaying
            )
        }
        
        composable(Screen.Library.route) {
            LibraryScreen(onSongClick = onSongClick)
        }

        composable(Screen.Downloads.route) {
            DownloadsScreen(
                onNavigateUp = { navController.navigateUp() },
                onPlaySong = onDownloadPlay,
                activeSongId = activeSongId,
                isPlaying = isPlaying
            )
        }
    }
}

