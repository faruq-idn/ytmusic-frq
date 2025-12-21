package com.frq.ytmusic.data.repository

import com.frq.ytmusic.data.local.dao.FavoriteSongDao
import com.frq.ytmusic.data.local.mapper.toFavoriteSongEntity
import com.frq.ytmusic.data.local.mapper.toSong
import com.frq.ytmusic.domain.model.Song
import com.frq.ytmusic.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of FavoritesRepository using Room.
 */
class FavoritesRepositoryImpl @Inject constructor(
    private val favoriteSongDao: FavoriteSongDao
) : FavoritesRepository {

    override fun getAllFavorites(): Flow<List<Song>> {
        return favoriteSongDao.getAllFavorites().map { entities ->
            entities.map { it.toSong() }
        }
    }

    override fun isFavorite(videoId: String): Flow<Boolean> {
        return favoriteSongDao.isFavorite(videoId)
    }

    override suspend fun addFavorite(song: Song) {
        favoriteSongDao.addFavorite(song.toFavoriteSongEntity())
    }

    override suspend fun removeFavorite(videoId: String) {
        favoriteSongDao.removeFavorite(videoId)
    }

    override suspend fun toggleFavorite(song: Song) {
        val isFav = favoriteSongDao.isFavorite(song.videoId).first()
        if (isFav) {
            removeFavorite(song.videoId)
        } else {
            addFavorite(song)
        }
    }
}
