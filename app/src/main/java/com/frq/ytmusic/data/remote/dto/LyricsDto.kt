package com.frq.ytmusic.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Data transfer object for lyrics.
 */
data class LyricsDto(
    @SerializedName("type")
    val type: String,
    
    @SerializedName("lines")
    val lines: List<LyricsLineDto>,
    
    @SerializedName("source")
    val source: String
)

data class LyricsLineDto(
    @SerializedName("text")
    val text: String,
    
    @SerializedName("start_time_ms")
    val startTimeMs: Long?,
    
    @SerializedName("end_time_ms")
    val endTimeMs: Long?
)
