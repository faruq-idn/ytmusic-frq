package com.frq.ytmusic.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO for album data from API.
 */
data class AlbumDto(
    @SerializedName("browse_id")
    val browseId: String,
    
    val title: String,
    
    val artist: String?,
    
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String,
    
    val year: String?,
    
    @SerializedName("is_explicit")
    val isExplicit: Boolean = false
)
