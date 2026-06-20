package com.lnxteam.atmoscope.data

import com.google.gson.annotations.SerializedName

data class ApodResponse(
    val date: String,
    val title: String,
    val explanation: String,
    val url: String?,
    @SerializedName("hdurl") val hdUrl: String?,
    @SerializedName("media_type") val mediaType: String
)

object NasaConfig {
    const val API_KEY = ""
}