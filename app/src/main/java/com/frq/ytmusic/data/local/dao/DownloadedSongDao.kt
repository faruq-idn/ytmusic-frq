package com.frq.ytmusic.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.frq.ytmusic.data.local.entity.DownloadedSongEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for downloaded songs.
 */
@Dao
interface DownloadedSongDao {
    
    @Query("SELECT * FROM downloaded_songs ORDER BY downloadedAt DESC")
    fun getAllDownloads(): Flow<List<DownloadedSongEntity>>
    
    @Query("SELECT * FROM downloaded_songs WHERE videoId = :videoId")
    suspend fun getDownloadById(videoId: String): DownloadedSongEntity?
    
    @Query("SELECT EXISTS(SELECT 1 FROM downloaded_songs WHERE videoId = :videoId)")
    fun isDownloaded(videoId: String): Flow<Boolean>
    
    @Query("SELECT EXISTS(SELECT 1 FROM downloaded_songs WHERE videoId = :videoId)")
    suspend fun isDownloadedSync(videoId: String): Boolean
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(song: DownloadedSongEntity)
    
    @Delete
    suspend fun delete(song: DownloadedSongEntity)
    
    @Query("DELETE FROM downloaded_songs WHERE videoId = :videoId")
    suspend fun deleteById(videoId: String)
    
    @Query("SELECT SUM(fileSize) FROM downloaded_songs")
    fun getTotalDownloadSize(): Flow<Long?>
    
    @Query("SELECT COUNT(*) FROM downloaded_songs")
    fun getDownloadCount(): Flow<Int>
}
