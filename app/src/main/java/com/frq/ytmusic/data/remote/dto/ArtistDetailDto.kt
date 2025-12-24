package com.frq.ytmusic.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO for artist detail from API.
 */
data class ArtistDetailDto(
    @SerializedName("browse_id")
    val browseId: String,
    
    val name: String,
    
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String,
    
    val description: String?,
    
    val subscribers: String?,
    
    val songs: List<SongDto>,
    
    val albums: List<AlbumDto>
)
