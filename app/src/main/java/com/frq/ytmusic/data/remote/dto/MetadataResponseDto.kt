package com.frq.ytmusic.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Metadata response from API.
 */
data class MetadataResponseDto(
    @SerializedName("video_id")
    val videoId: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("artist")
    val artist: String,
    
    @SerializedName("album")
    val album: String?,
    
    @SerializedName("duration_seconds")
    val durationSeconds: Int?,
    
    @SerializedName("has_lyrics")
    val hasLyrics: Boolean,
    
    @SerializedName("lyrics")
    val lyrics: String?
)
