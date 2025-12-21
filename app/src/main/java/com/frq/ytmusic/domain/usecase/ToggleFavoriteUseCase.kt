package com.frq.ytmusic.domain.usecase

import com.frq.ytmusic.domain.model.Song
import com.frq.ytmusic.domain.repository.FavoritesRepository
import javax.inject.Inject

/**
 * Use case for toggling favorite status.
 */
class ToggleFavoriteUseCase @Inject constructor(
    private val repository: FavoritesRepository
) {
    suspend operator fun invoke(song: Song) = repository.toggleFavorite(song)
}
