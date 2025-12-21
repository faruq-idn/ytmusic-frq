package com.frq.ytmusic.domain.usecase

import com.frq.ytmusic.domain.repository.DownloadRepository
import javax.inject.Inject

/**
 * Use case to delete a downloaded song.
 */
class DeleteDownloadUseCase @Inject constructor(
    private val repository: DownloadRepository
) {
    suspend operator fun invoke(videoId: String): Result<Unit> {
        return repository.deleteSong(videoId)
    }
}
