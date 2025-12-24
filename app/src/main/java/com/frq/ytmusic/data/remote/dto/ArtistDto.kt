package com.frq.ytmusic.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO for artist from search results.
 */
data class ArtistDto(
    @SerializedName("browse_id")
    val browseId: String,
    
    val name: String,
    
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String,
    
    val subscribers: String?
)
