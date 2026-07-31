package com.fmcg.app.domain.model

data class LatLng(val latitude: Double, val longitude: Double)

data class RouteInfo(
    val distanceKm: Double,
    val points: List<LatLng>,
)
