package com.frq.ytmusic.domain.usecase

import com.frq.ytmusic.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for checking if a song is favorite.
 */
class IsFavoriteUseCase @Inject constructor(
    private val repository: FavoritesRepository
) {
    operator fun invoke(videoId: String): Flow<Boolean> = repository.isFavorite(videoId)
}
