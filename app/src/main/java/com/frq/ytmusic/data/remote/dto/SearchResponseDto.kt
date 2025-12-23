package com.frq.ytmusic.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Search response from API (unified search with songs + playlists + albums).
 */
data class SearchResponseDto(
    @SerializedName("songs")
    val songs: List<SongDto>,
    
    @SerializedName("playlists")
    val playlists: List<PlaylistDto> = emptyList(),
    
    @SerializedName("albums")
    val albums: List<AlbumDto> = emptyList(),
    
    @SerializedName("query")
    val query: String,
    
    @SerializedName("count")
    val count: Int
)


