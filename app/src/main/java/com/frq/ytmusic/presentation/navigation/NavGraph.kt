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
import com.frq.ytmusic.presentation.ytmplaylist.YtmPlaylistDetailScreen
import com.frq.ytmusic.presentation.ytmalbum.YtmAlbumDetailScreen
import com.frq.ytmusic.presentation.ytmartist.YtmArtistDetailScreen

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
                onPlaylistClick = { playlistId ->
                    navController.navigate(Screen.YtmPlaylistDetail(playlistId).route)
                },
                onAlbumClick = { browseId ->
                    navController.navigate(Screen.AlbumDetail(browseId).route)
                },
                onArtistClick = { browseId ->
                    navController.navigate(Screen.ArtistDetail(browseId).route)
                },
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
        
        composable(
            route = Screen.YtmPlaylistDetail.route,
            arguments = listOf(
                navArgument("playlistId") { type = NavType.StringType }
            )
        ) {
            YtmPlaylistDetailScreen(
                onBack = { navController.navigateUp() },
                onSongClick = onSongClick
            )
        }
        
        composable(
            route = Screen.AlbumDetail.route,
            arguments = listOf(
                navArgument("browseId") { type = NavType.StringType }
            )
        ) {
            YtmAlbumDetailScreen(
                onBack = { navController.navigateUp() },
                onSongClick = onSongClick
            )
        }
        
        composable(
            route = Screen.ArtistDetail.route,
            arguments = listOf(
                navArgument("browseId") { type = NavType.StringType }
            )
        ) {
            YtmArtistDetailScreen(
                onBack = { navController.navigateUp() },
                onSongClick = onSongClick,
                onAlbumClick = { browseId ->
                    navController.navigate(Screen.AlbumDetail(browseId).route)
                }
            )
        }
        
        composable(
            route = Screen.ArtistByName.route,
            arguments = listOf(
                navArgument("artistName") { type = NavType.StringType }
            )
        ) {
            YtmArtistDetailScreen(
                onBack = { navController.navigateUp() },
                onSongClick = onSongClick,
                onAlbumClick = { browseId ->
                    navController.navigate(Screen.AlbumDetail(browseId).route)
                },
                isByName = true
            )
        }
    }
}
