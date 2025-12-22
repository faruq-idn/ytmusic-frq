package com.frq.ytmusic.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.frq.ytmusic.data.local.entity.DownloadedSongEntity
import com.frq.ytmusic.domain.model.Song
import com.frq.ytmusic.presentation.downloads.DownloadsScreen
import com.frq.ytmusic.presentation.collection.CollectionScreen
import com.frq.ytmusic.presentation.collection.LikedSongsScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.frq.ytmusic.presentation.search.SearchScreen
import com.frq.ytmusic.presentation.playlist.PlaylistDetailScreen

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
        
        composable(Screen.Collection.route) {
            CollectionScreen(
                onDownloadsClick = { navController.navigate(Screen.Downloads.route) },
                onLikedSongsClick = { navController.navigate(Screen.LikedSongs.route) },
                onPlaylistClick = { playlistId ->
                    navController.navigate(Screen.PlaylistDetail(playlistId).route)
                },
                hasMiniPlayer = activeSongId != null
            )
        }

        composable(Screen.Downloads.route) {
            DownloadsScreen(
                onNavigateUp = { navController.navigateUp() },
                onPlaySong = onDownloadPlay,
                activeSongId = activeSongId,
                isPlaying = isPlaying
            )
        }
        
        composable(Screen.LikedSongs.route) {
            LikedSongsScreen(
                onNavigateUp = { navController.navigateUp() },
                onSongClick = onSongClick,
                activeSongId = activeSongId,
                isPlaying = isPlaying
            )
        }
        
        composable(
            route = Screen.PlaylistDetail.route,
            arguments = listOf(
                navArgument("playlistId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: return@composable
            
            PlaylistDetailScreen(
                playlistId = playlistId,
                onNavigateUp = { navController.navigateUp() },
                onSongClick = onSongClick,
                activeSongId = activeSongId,
                isPlaying = isPlaying
            )
        }
    }
}

