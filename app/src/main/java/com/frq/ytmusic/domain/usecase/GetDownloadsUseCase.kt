package com.frq.ytmusic.domain.usecase

import com.frq.ytmusic.data.local.entity.DownloadedSongEntity
import com.frq.ytmusic.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to get all downloaded songs.
 */
class GetDownloadsUseCase @Inject constructor(
    private val repository: DownloadRepository
) {
    operator fun invoke(): Flow<List<DownloadedSongEntity>> {
        return repository.getAllDownloads()
    }
}
