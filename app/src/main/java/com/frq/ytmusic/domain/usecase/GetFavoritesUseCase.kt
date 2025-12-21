package com.frq.ytmusic.domain.usecase

import com.frq.ytmusic.domain.model.Song
import com.frq.ytmusic.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting all favorite songs.
 */
class GetFavoritesUseCase @Inject constructor(
    private val repository: FavoritesRepository
) {
    operator fun invoke(): Flow<List<Song>> = repository.getAllFavorites()
}
