package com.frq.ytmusic.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Song data from API.
 */
data class SongDto(
    @SerializedName("video_id")
    val videoId: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("artist")
    val artist: String,
    
    @SerializedName("artist_id")
    val artistId: String? = null,
    
    @SerializedName("album")
    val album: String?,
    
    @SerializedName("duration_text")
    val durationText: String?,
    
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String?
)
