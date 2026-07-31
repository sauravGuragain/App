package com.fmcg.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GpsPointDto(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float? = null,
    @SerialName("recorded_at") val recordedAt: String,
)

@Serializable
data class GpsBatchDto(
    val points: List<GpsPointDto>,
)

@Serializable
data class MessageDto(
    val message: String,
)
