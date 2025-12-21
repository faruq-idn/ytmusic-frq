package com.frq.ytmusic.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.frq.ytmusic.data.local.entity.FavoriteSongEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for favorite songs operations.
 */
@Dao
interface FavoriteSongDao {

    @Query("SELECT * FROM favorite_songs ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteSongEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_songs WHERE videoId = :videoId)")
    fun isFavorite(videoId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(song: FavoriteSongEntity)

    @Query("DELETE FROM favorite_songs WHERE videoId = :videoId")
    suspend fun removeFavorite(videoId: String)

    @Query("SELECT COUNT(*) FROM favorite_songs")
    fun getFavoritesCount(): Flow<Int>
}
