package com.frq.ytmusic.presentation.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Sealed class representing app screens for navigation.
 */
sealed class Screen(val route: String) {
    data object Search : Screen("search")
    data object Collection : Screen("collection")
    data object Downloads : Screen("downloads")
    data object LikedSongs : Screen("liked_songs")
    data class PlaylistDetail(val playlistId: Long) : Screen("playlist/$playlistId") {
        companion object {
            const val route = "playlist/{playlistId}"
        }
    }
    data class YtmPlaylistDetail(val playlistId: String) : Screen("ytm_playlist/$playlistId") {
        companion object {
            const val route = "ytm_playlist/{playlistId}"
        }
    }
    data class AlbumDetail(val browseId: String) : Screen("album/$browseId") {
        companion object {
            const val route = "album/{browseId}"
        }
    }
    data class ArtistDetail(val browseId: String) : Screen("artist/$browseId") {
        companion object {
            const val route = "artist/{browseId}"
        }
    }
    data class ArtistByName(val artistName: String) : Screen(
        "artist_by_name/${URLEncoder.encode(artistName, StandardCharsets.UTF_8.toString())}"
    ) {
        companion object {
            const val route = "artist_by_name/{artistName}"
        }
    }
}
