package com.frq.ytmusic.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO for playlist detail response from API.
 */
data class PlaylistDetailDto(
    @SerializedName("playlist_id")
    val playlistId: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String?,
    
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String,
    
    @SerializedName("author")
    val author: String?,
    
    @SerializedName("song_count")
    val songCount: Int,
    
    @SerializedName("songs")
    val songs: List<SongDto>
)
