package com.frq.ytmusic.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Generic API response wrapper.
 */
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("data")
    val data: T?,
    
    @SerializedName("error")
    val error: String? = null
)
