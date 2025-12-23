package com.frq.ytmusic.domain.usecase

import com.frq.ytmusic.domain.model.SearchResult
import com.frq.ytmusic.domain.repository.SongRepository
import javax.inject.Inject

/**
 * Use case for unified search (songs + playlists).
 */
class SearchAllUseCase @Inject constructor(
    private val repository: SongRepository
) {
    /**
     * Search for songs and playlists matching the query.
     * 
     * @param query Search query (min 2 characters)
     * @param limit Maximum number of song results
     * @return Result containing SearchResult with songs and playlists
     */
    suspend operator fun invoke(query: String, limit: Int = 20): Result<SearchResult> {
        // Validate query
        if (query.isBlank() || query.length < 2) {
            return Result.success(SearchResult(emptyList(), emptyList()))
        }
        
        return repository.searchAll(query.trim(), limit)
    }
}
