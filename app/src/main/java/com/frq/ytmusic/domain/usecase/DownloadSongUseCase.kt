package com.frq.ytmusic.domain.usecase

import com.frq.ytmusic.data.local.entity.DownloadedSongEntity
import com.frq.ytmusic.domain.model.Song
import com.frq.ytmusic.domain.repository.DownloadRepository
import javax.inject.Inject

/**
 * Use case to download a song.
 */
class DownloadSongUseCase @Inject constructor(
    private val repository: DownloadRepository
) {
    suspend operator fun invoke(song: Song): Result<DownloadedSongEntity> {
        return repository.downloadSong(song)
    }
}
