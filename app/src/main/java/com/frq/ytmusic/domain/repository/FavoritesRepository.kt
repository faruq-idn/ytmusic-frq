package com.frq.ytmusic.domain.repository

import com.frq.ytmusic.domain.model.Song
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for favorite songs operations.
 */
interface FavoritesRepository {
    
    fun getAllFavorites(): Flow<List<Song>>
    
    fun isFavorite(videoId: String): Flow<Boolean>
    
    suspend fun addFavorite(song: Song)
    
    suspend fun removeFavorite(videoId: String)
    
    suspend fun toggleFavorite(song: Song)
}
