package com.frq.ytmusic.domain.repository

import com.frq.ytmusic.domain.model.Playlist
import com.frq.ytmusic.domain.model.PlaylistWithSongs
import com.frq.ytmusic.domain.model.Song
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for playlist operations.
 */
interface PlaylistRepository {
    
    // Playlist CRUD
    suspend fun createPlaylist(name: String, description: String? = null): Long
    suspend fun updatePlaylist(playlist: Playlist)
    suspend fun deletePlaylist(playlistId: Long)
    fun getAllPlaylists(): Flow<List<Playlist>>
    fun getPlaylistById(playlistId: Long): Flow<Playlist?>
    suspend fun getPlaylistByIdSync(playlistId: Long): Playlist?
    
    // Song operations
    suspend fun addSongToPlaylist(playlistId: Long, song: Song)
    suspend fun removeSongFromPlaylist(playlistId: Long, videoId: String)
    fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>>
    fun getPlaylistWithSongs(playlistId: Long): Flow<PlaylistWithSongs?>
    suspend fun isSongInPlaylist(playlistId: Long, videoId: String): Boolean
    
    // Reordering
    suspend fun reorderSongs(playlistId: Long, fromIndex: Int, toIndex: Int)
}
