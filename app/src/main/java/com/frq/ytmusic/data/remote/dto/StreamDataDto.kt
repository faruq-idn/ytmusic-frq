package com.frq.ytmusic.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Stream data from API.
 */
data class StreamDataDto(
    @SerializedName("video_id")
    val videoId: String,
    
    @SerializedName("stream_url")
    val streamUrl: String,
    
    @SerializedName("title")
    val title: String?,
    
    @SerializedName("duration")
    val duration: Int?
)
