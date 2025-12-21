package com.frq.ytmusic.data.repository

import com.frq.ytmusic.data.local.DownloadManager
import com.frq.ytmusic.data.local.DownloadState
import com.frq.ytmusic.data.local.entity.DownloadedSongEntity
import com.frq.ytmusic.domain.model.Song
import com.frq.ytmusic.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of DownloadRepository using DownloadManager.
 */
@Singleton
class DownloadRepositoryImpl @Inject constructor(
    private val downloadManager: DownloadManager
) : DownloadRepository {

    override val downloadState: StateFlow<DownloadState>
        get() = downloadManager.downloadState

    override suspend fun downloadSong(song: Song): Result<DownloadedSongEntity> {
        return downloadManager.downloadSong(song)
    }

    override suspend fun deleteSong(videoId: String): Result<Unit> {
        return downloadManager.deleteSong(videoId)
    }

    override fun getAllDownloads(): Flow<List<DownloadedSongEntity>> {
        return downloadManager.getAllDownloads()
    }

    override fun isDownloaded(videoId: String): Flow<Boolean> {
        return downloadManager.isDownloaded(videoId)
    }

    override suspend fun getFilePath(videoId: String): String? {
        return downloadManager.getFilePath(videoId)
    }

    override fun resetState() {
        downloadManager.resetState()
    }
}
