package com.fmcg.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoutePointDto(
    val latitude: Double,
    val longitude: Double,
)

@Serializable
data class RouteDto(
    @SerialName("distance_km") val distanceKm: Double,
    val points: List<RoutePointDto>,
)
