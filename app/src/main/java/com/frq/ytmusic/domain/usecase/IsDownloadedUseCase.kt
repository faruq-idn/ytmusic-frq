package com.frq.ytmusic.domain.usecase

import com.frq.ytmusic.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to check if a song is downloaded.
 */
class IsDownloadedUseCase @Inject constructor(
    private val repository: DownloadRepository
) {
    operator fun invoke(videoId: String): Flow<Boolean> {
        return repository.isDownloaded(videoId)
    }
}
