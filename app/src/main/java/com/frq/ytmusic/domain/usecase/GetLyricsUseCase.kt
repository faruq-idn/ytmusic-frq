package com.frq.ytmusic.domain.usecase

import com.frq.ytmusic.domain.model.Lyrics
import com.frq.ytmusic.domain.repository.SongRepository
import javax.inject.Inject

/**
 * Use case to get lyrics for a song.
 */
class GetLyricsUseCase @Inject constructor(
    private val repository: SongRepository
) {
    /**
     * Get lyrics for a specific video ID.
     */
    suspend operator fun invoke(videoId: String): Result<Lyrics?> {
        return repository.getMetadata(videoId).map { it.lyrics }
    }
}
