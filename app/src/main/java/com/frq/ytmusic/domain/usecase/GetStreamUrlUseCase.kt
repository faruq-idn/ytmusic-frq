package com.frq.ytmusic.domain.usecase

import com.frq.ytmusic.domain.repository.SongRepository
import javax.inject.Inject

/**
 * Use case for getting stream URL for a song.
 */
class GetStreamUrlUseCase @Inject constructor(
    private val repository: SongRepository
) {
    /**
     * Get stream URL for the given video ID.
     * 
     * @param videoId YouTube video ID
     * @return Result containing stream URL or error
     */
    suspend operator fun invoke(videoId: String): Result<String> {
        if (videoId.isBlank()) {
            return Result.failure(IllegalArgumentException("Video ID cannot be empty"))
        }
        return repository.getStreamUrl(videoId)
    }
}
