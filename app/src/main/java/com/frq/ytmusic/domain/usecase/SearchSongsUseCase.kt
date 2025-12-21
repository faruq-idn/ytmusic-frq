package com.frq.ytmusic.domain.usecase

import com.frq.ytmusic.domain.model.Song
import com.frq.ytmusic.domain.repository.SongRepository
import javax.inject.Inject

/**
 * Use case for searching songs.
 * Encapsulates the business logic for search.
 */
class SearchSongsUseCase @Inject constructor(
    private val repository: SongRepository
) {
    /**
     * Search for songs matching the query.
     * 
     * @param query Search query (min 2 characters)
     * @param limit Maximum number of results
     * @return Result containing list of songs or error
     */
    suspend operator fun invoke(query: String, limit: Int = 20): Result<List<Song>> {
        // Validate query
        if (query.isBlank() || query.length < 2) {
            return Result.success(emptyList())
        }
        
        return repository.searchSongs(query.trim(), limit)
    }
}
