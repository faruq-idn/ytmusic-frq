package com.frq.ytmusic.data.repository

import com.frq.ytmusic.data.remote.api.YtMusicApi
import com.frq.ytmusic.data.remote.mapper.toDomain
import com.frq.ytmusic.data.remote.mapper.toDomainList
import com.frq.ytmusic.domain.model.Song
import com.frq.ytmusic.domain.repository.SongMetadata
import com.frq.ytmusic.domain.repository.SongRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of SongRepository using remote API.
 */
@Singleton
class SongRepositoryImpl @Inject constructor(
    private val api: YtMusicApi
) : SongRepository {

    override suspend fun searchSongs(query: String, limit: Int): Result<List<Song>> {
        return try {
            val response = api.searchSongs(query, limit)
            if (response.success && response.data != null) {
                Result.success(response.data.songs.toDomainList())
            } else {
                Result.failure(Exception(response.error ?: "Search failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getStreamUrl(videoId: String): Result<String> {
        return try {
            val response = api.getStreamUrl(videoId)
            if (response.success && response.data != null) {
                Result.success(response.data.streamUrl)
            } else {
                Result.failure(Exception(response.error ?: "Failed to get stream URL"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMetadata(videoId: String): Result<SongMetadata> {
        return try {
            val response = api.getMetadata(videoId)
            if (response.success && response.data != null) {
                Result.success(response.data.toDomain())
            } else {
                Result.failure(Exception(response.error ?: "Failed to get metadata"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRelatedSongs(videoId: String, limit: Int): Result<List<Song>> {
        return try {
            val response = api.getRelatedSongs(videoId, limit)
            if (response.success && response.data != null) {
                Result.success(response.data.songs.toDomainList())
            } else {
                Result.failure(Exception(response.error ?: "Failed to get related songs"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
