package com.frq.ytmusic.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Related songs response from API.
 */
data class RelatedResponseDto(
    @SerializedName("songs")
    val songs: List<SongDto>
)
