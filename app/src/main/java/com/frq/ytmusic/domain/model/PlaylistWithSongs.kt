package com.frq.ytmusic.domain.model

/**
 * Domain model representing a playlist with its songs.
 */
data class PlaylistWithSongs(
    val playlist: Playlist,
    val songs: List<Song>
)
