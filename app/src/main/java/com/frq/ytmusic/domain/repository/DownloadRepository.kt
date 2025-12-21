package com.frq.ytmusic.domain.repository

import com.frq.ytmusic.data.local.DownloadState
import com.frq.ytmusic.data.local.entity.DownloadedSongEntity
import com.frq.ytmusic.domain.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository for download operations.
 */
interface DownloadRepository {
    val downloadState: StateFlow<DownloadState>
    
    suspend fun downloadSong(song: Song): Result<DownloadedSongEntity>
    suspend fun deleteSong(videoId: String): Result<Unit>
    fun getAllDownloads(): Flow<List<DownloadedSongEntity>>
    fun isDownloaded(videoId: String): Flow<Boolean>
    suspend fun getFilePath(videoId: String): String?
    fun resetState()
}
