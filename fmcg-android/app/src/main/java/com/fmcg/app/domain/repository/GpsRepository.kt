package com.fmcg.app.domain.repository

import com.fmcg.app.domain.model.RouteInfo
import com.fmcg.app.util.Resource
import kotlinx.coroutines.flow.Flow

interface GpsRepository {
    suspend fun recordPoint(lat: Double, lng: Double, accuracy: Float?, timestampMillis: Long)
    /** Push buffered points; returns the number uploaded. */
    suspend fun syncPending(): Resource<Int>
    fun bufferedCount(): Flow<Int>
    /** Fetch the recorded route for a given yyyy-MM-dd (UTC) from the backend. */
    suspend fun getRoute(date: String): Resource<RouteInfo>
}
