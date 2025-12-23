package com.frq.ytmusic.domain.repository

import com.frq.ytmusic.domain.model.Lyrics
import com.frq.ytmusic.domain.model.SearchResult
import com.frq.ytmusic.domain.model.Song
import com.frq.ytmusic.domain.model.YtmAlbumDetail
import com.frq.ytmusic.domain.model.YtmPlaylistDetail

/**
 * Repository interface for song data operations.
 * Defines the contract for data access.
 */
interface SongRepository {
    
    /**
     * Search for songs by query.
     */
    suspend fun searchSongs(query: String, limit: Int = 20): Result<List<Song>>
    
    /**
     * Unified search for songs and playlists.
     */
    suspend fun searchAll(query: String, limit: Int = 20): Result<SearchResult>
    
    /**
     * Get stream URL for a song.
     */
    suspend fun getStreamUrl(videoId: String): Result<String>
    
    /**
     * Get song metadata including lyrics.
     */
    suspend fun getMetadata(videoId: String): Result<SongMetadata>
    
    /**
     * Get related songs.
     */
    suspend fun getRelatedSongs(videoId: String, limit: Int = 20): Result<List<Song>>
    
    /**
     * Get YTM playlist detail with songs.
     */
    suspend fun getPlaylist(playlistId: String): Result<YtmPlaylistDetail>

    /**
     * Get search suggestions for autocomplete.
     */
    suspend fun getSuggestions(query: String): Result<List<String>>

    /**
     * Get YTM album detail with tracks.
     */
    suspend fun getAlbum(browseId: String): Result<YtmAlbumDetail>
}

/**
 * Song metadata including lyrics.
 */
data class SongMetadata(
    val videoId: String,
    val title: String,
    val artist: String,
    val album: String?,
    val durationSeconds: Int?,
    val hasLyrics: Boolean,
    val lyrics: Lyrics?
)
