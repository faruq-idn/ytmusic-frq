package com.frq.ytmusic.data.local

import android.content.Context
import com.frq.ytmusic.data.local.dao.DownloadedSongDao
import com.frq.ytmusic.data.local.entity.DownloadedSongEntity
import com.frq.ytmusic.data.remote.api.YtMusicApi
import com.frq.ytmusic.domain.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Download state for tracking progress.
 */
sealed class DownloadState {
    object Idle : DownloadState()
    data class Downloading(val videoId: String, val progress: Float) : DownloadState()
    data class Completed(val videoId: String) : DownloadState()
    data class Error(val videoId: String, val message: String) : DownloadState()
}

/**
 * Manages song downloads with progress tracking.
 */
@Singleton
class DownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: YtMusicApi,
    private val downloadedSongDao: DownloadedSongDao
) {
    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()
    
    private val downloadsDir: File by lazy {
        File(context.filesDir, "downloads").apply { mkdirs() }
    }
    
    /**
     * Download a song and save to local storage.
     */
    suspend fun downloadSong(song: Song): Result<DownloadedSongEntity> = withContext(Dispatchers.IO) {
        try {
            _downloadState.value = DownloadState.Downloading(song.videoId, 0f)
            
            // Check if already downloaded
            if (downloadedSongDao.isDownloadedSync(song.videoId)) {
                val existing = downloadedSongDao.getDownloadById(song.videoId)
                if (existing != null && File(existing.filePath).exists()) {
                    _downloadState.value = DownloadState.Completed(song.videoId)
                    return@withContext Result.success(existing)
                }
            }
            
            // Download from API
            val response = api.downloadAudio(song.videoId)
            val contentLength = response.contentLength()
            
            // Save to file
            val file = File(downloadsDir, "${song.videoId}.m4a")
            val inputStream = response.byteStream()
            val outputStream = FileOutputStream(file)
            
            var bytesWritten = 0L
            val buffer = ByteArray(8192)
            var bytes: Int
            
            while (inputStream.read(buffer).also { bytes = it } != -1) {
                outputStream.write(buffer, 0, bytes)
                bytesWritten += bytes
                
                // Update progress
                if (contentLength > 0) {
                    val progress = bytesWritten.toFloat() / contentLength.toFloat()
                    _downloadState.value = DownloadState.Downloading(song.videoId, progress)
                }
            }
            
            outputStream.close()
            inputStream.close()
            
            // Save to database
            val entity = DownloadedSongEntity(
                videoId = song.videoId,
                title = song.title,
                artist = song.artist,
                thumbnailUrl = song.thumbnailUrl,
                duration = song.durationText,
                filePath = file.absolutePath,
                fileSize = file.length()
            )
            
            downloadedSongDao.insert(entity)
            _downloadState.value = DownloadState.Completed(song.videoId)
            
            Result.success(entity)
        } catch (e: Exception) {
            _downloadState.value = DownloadState.Error(song.videoId, e.message ?: "Download failed")
            Result.failure(e)
        }
    }
    
    /**
     * Delete a downloaded song.
     */
    suspend fun deleteSong(videoId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val download = downloadedSongDao.getDownloadById(videoId)
            download?.let {
                File(it.filePath).delete()
                downloadedSongDao.delete(it)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all downloaded songs.
     */
    fun getAllDownloads(): Flow<List<DownloadedSongEntity>> {
        return downloadedSongDao.getAllDownloads()
    }
    
    /**
     * Check if a song is downloaded.
     */
    fun isDownloaded(videoId: String): Flow<Boolean> {
        return downloadedSongDao.isDownloaded(videoId)
    }
    
    /**
     * Get file path for a downloaded song.
     */
    suspend fun getFilePath(videoId: String): String? {
        return downloadedSongDao.getDownloadById(videoId)?.filePath
    }
    
    /**
     * Reset download state to idle.
     */
    fun resetState() {
        _downloadState.value = DownloadState.Idle
    }
}
