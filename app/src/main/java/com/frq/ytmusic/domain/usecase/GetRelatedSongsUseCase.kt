package com.frq.ytmusic.domain.usecase

import com.frq.ytmusic.domain.model.Song
import com.frq.ytmusic.domain.repository.SongRepository
import javax.inject.Inject

/**
 * Use case for getting related songs.
 */
class GetRelatedSongsUseCase @Inject constructor(
    private val repository: SongRepository
) {
    suspend operator fun invoke(videoId: String, limit: Int = 10): Result<List<Song>> {
        if (videoId.length != 11) {
            return Result.failure(IllegalArgumentException("Invalid video ID"))
        }
        return repository.getRelatedSongs(videoId, limit)
    }
}
