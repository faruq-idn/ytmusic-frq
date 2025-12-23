package com.frq.ytmusic.data.repository

import com.frq.ytmusic.data.remote.api.YtMusicApi
import com.frq.ytmusic.data.remote.mapper.toAlbumDomainList
import com.frq.ytmusic.data.remote.mapper.toDomain
import com.frq.ytmusic.data.remote.mapper.toDomainList
import com.frq.ytmusic.data.remote.mapper.toPlaylistDomainList
import com.frq.ytmusic.domain.model.SearchResult
import com.frq.ytmusic.domain.model.Song
import com.frq.ytmusic.domain.model.YtmAlbumDetail
import com.frq.ytmusic.domain.model.YtmPlaylistDetail
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

    override suspend fun searchAll(query: String, limit: Int): Result<SearchResult> {
        return try {
            val response = api.searchSongs(query, limit)
            if (response.success && response.data != null) {
                Result.success(
                    SearchResult(
                        songs = response.data.songs.toDomainList(),
                        playlists = response.data.playlists.toPlaylistDomainList(),
                        albums = response.data.albums.toAlbumDomainList()
                    )
                )
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

    override suspend fun getPlaylist(playlistId: String): Result<YtmPlaylistDetail> {
        return try {
            val response = api.getPlaylist(playlistId)
            if (response.success && response.data != null) {
                Result.success(response.data.toDomain())
            } else {
                Result.failure(Exception(response.error ?: "Failed to get playlist"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSuggestions(query: String): Result<List<String>> {
        return try {
            val response = api.getSuggestions(query)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.success(emptyList()) // Return empty list on failure
            }
        } catch (e: Exception) {
            Result.success(emptyList()) // Suggestions failing shouldn't crash
        }
    }

    override suspend fun getAlbum(browseId: String): Result<YtmAlbumDetail> {
        return try {
            val response = api.getAlbum(browseId)
            if (response.success && response.data != null) {
                Result.success(response.data.toDomain())
            } else {
                Result.failure(Exception(response.error ?: "Failed to get album"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

