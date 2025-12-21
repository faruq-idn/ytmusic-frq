package com.frq.ytmusic.data.remote.api

import com.frq.ytmusic.data.remote.dto.ApiResponse
import com.frq.ytmusic.data.remote.dto.MetadataResponseDto
import com.frq.ytmusic.data.remote.dto.RelatedResponseDto
import com.frq.ytmusic.data.remote.dto.SearchResponseDto
import com.frq.ytmusic.data.remote.dto.StreamDataDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

/**
 * Retrofit API interface for YT Music backend.
 */
interface YtMusicApi {

    /**
     * Search for songs.
     */
    @GET("api/v1/search")
    suspend fun searchSongs(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20
    ): ApiResponse<SearchResponseDto>

    /**
     * Get stream URL for a song.
     */
    @GET("api/v1/stream/{video_id}")
    suspend fun getStreamUrl(
        @Path("video_id") videoId: String
    ): ApiResponse<StreamDataDto>

    /**
     * Get song metadata including lyrics.
     */
    @GET("api/v1/metadata/{video_id}")
    suspend fun getMetadata(
        @Path("video_id") videoId: String
    ): ApiResponse<MetadataResponseDto>

    /**
     * Get related songs.
     */
    @GET("api/v1/related/{video_id}")
    suspend fun getRelatedSongs(
        @Path("video_id") videoId: String,
        @Query("limit") limit: Int = 20
    ): ApiResponse<RelatedResponseDto>

    /**
     * Download audio file.
     */
    @Streaming
    @GET("api/v1/download/{video_id}")
    suspend fun downloadAudio(
        @Path("video_id") videoId: String,
        @Query("quality") quality: String = "best"
    ): okhttp3.ResponseBody
}
