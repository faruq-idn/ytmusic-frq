package com.frq.ytmusic.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO for album detail response from API.
 */
data class AlbumDetailDto(
    @SerializedName("browse_id")
    val browseId: String,
    
    val title: String,
    
    val artist: String?,
    
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String,
    
    val year: String?,
    
    @SerializedName("track_count")
    val trackCount: Int,
    
    val duration: String?,
    
    val songs: List<SongDto>
)
