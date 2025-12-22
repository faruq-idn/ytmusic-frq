package com.frq.ytmusic.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.frq.ytmusic.data.local.entity.PlaylistEntity
import com.frq.ytmusic.data.local.entity.PlaylistSongCrossRef
import kotlinx.coroutines.flow.Flow

/**
 * DAO for playlist operations.
 */
@Dao
interface PlaylistDao {
    
    // ==================== Playlist CRUD ====================
    
    @Insert
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long
    
    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)
    
    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)
    
    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylistById(playlistId: Long)
    
    @Query("SELECT * FROM playlists ORDER BY updatedAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>
    
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    suspend fun getPlaylistById(playlistId: Long): PlaylistEntity?
    
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    fun getPlaylistByIdFlow(playlistId: Long): Flow<PlaylistEntity?>
    
    // ==================== Song Operations ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSongToPlaylist(crossRef: PlaylistSongCrossRef)
    
    @Delete
    suspend fun removeSongFromPlaylist(crossRef: PlaylistSongCrossRef)
    
    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND videoId = :videoId")
    suspend fun removeSongFromPlaylistById(playlistId: Long, videoId: String)
    
    @Query("SELECT * FROM playlist_songs WHERE playlistId = :playlistId ORDER BY position ASC")
    fun getSongsInPlaylist(playlistId: Long): Flow<List<PlaylistSongCrossRef>>
    
    @Query("SELECT COUNT(*) FROM playlist_songs WHERE playlistId = :playlistId")
    fun getSongCount(playlistId: Long): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun getSongCountSync(playlistId: Long): Int
    
    @Query("SELECT MAX(position) FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun getMaxPosition(playlistId: Long): Int?
    
    @Query("SELECT EXISTS(SELECT 1 FROM playlist_songs WHERE playlistId = :playlistId AND videoId = :videoId)")
    suspend fun isSongInPlaylist(playlistId: Long, videoId: String): Boolean
    
    // ==================== Reordering ====================
    
    @Query("UPDATE playlist_songs SET position = :newPosition WHERE playlistId = :playlistId AND videoId = :videoId")
    suspend fun updateSongPosition(playlistId: Long, videoId: String, newPosition: Int)
    
    @Transaction
    suspend fun reorderSongs(playlistId: Long, songs: List<PlaylistSongCrossRef>) {
        songs.forEachIndexed { index, song ->
            updateSongPosition(playlistId, song.videoId, index)
        }
    }
    
    // ==================== Update Playlist Timestamp ====================
    
    @Query("UPDATE playlists SET updatedAt = :timestamp WHERE id = :playlistId")
    suspend fun updatePlaylistTimestamp(playlistId: Long, timestamp: Long = System.currentTimeMillis())
}
