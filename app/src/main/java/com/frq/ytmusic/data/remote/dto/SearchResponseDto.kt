package com.frq.ytmusic.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Search response from API (unified search with songs + playlists).
 */
data class SearchResponseDto(
    @SerializedName("songs")
    val songs: List<SongDto>,
    
    @SerializedName("playlists")
    val playlists: List<PlaylistDto> = emptyList(),
    
    @SerializedName("query")
    val query: String,
    
    @SerializedName("count")
    val count: Int
)

