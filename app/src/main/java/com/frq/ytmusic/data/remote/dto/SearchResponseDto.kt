package com.frq.ytmusic.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Search response from API.
 */
data class SearchResponseDto(
    @SerializedName("songs")
    val songs: List<SongDto>,
    
    @SerializedName("query")
    val query: String,
    
    @SerializedName("count")
    val count: Int
)
