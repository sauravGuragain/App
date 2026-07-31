package com.fmcg.app.data.repository

import com.fmcg.app.data.local.TokenManager
import com.fmcg.app.data.local.dao.GpsDao
import com.fmcg.app.data.local.entity.GpsPointEntity
import com.fmcg.app.data.remote.api.GpsApi
import com.fmcg.app.data.remote.dto.GpsBatchDto
import com.fmcg.app.data.remote.dto.GpsPointDto
import com.fmcg.app.domain.model.LatLng
import com.fmcg.app.domain.model.RouteInfo
import com.fmcg.app.domain.repository.GpsRepository
import com.fmcg.app.util.Resource
import com.fmcg.app.util.toIso8601
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class GpsRepositoryImpl @Inject constructor(
    private val dao: GpsDao,
    private val api: GpsApi,
    private val tokenManager: TokenManager,
    private val io: CoroutineDispatcher,
) : GpsRepository {

    override suspend fun recordPoint(
        lat: Double, lng: Double, accuracy: Float?, timestampMillis: Long,
    ) = withContext(io) {
        dao.insert(
            GpsPointEntity(
                latitude = lat, longitude = lng,
                accuracy = accuracy, recordedAt = timestampMillis, synced = false,
            )
        )
    }

    override suspend fun syncPending(): Resource<Int> = withContext(io) {
        val userId = tokenManager.userId
            ?: return@withContext Resource.Error("No signed-in user")
        val pending = dao.getUnsynced()
        if (pending.isEmpty()) return@withContext Resource.Success(0)
        val batch = GpsBatchDto(
            points = pending.map {
                GpsPointDto(it.latitude, it.longitude, it.accuracy, it.recordedAt.toIso8601())
            }
        )
        try {
            api.uploadBatch(userId, batch)
            dao.deleteByIds(pending.map { it.id })   // drop only what we sent
            Resource.Success(pending.size)
        } catch (e: HttpException) {
            Resource.Error("Upload failed (${e.code()})", e.code())
        } catch (e: IOException) {
            Resource.Error("Offline — points kept for later sync")
        }
    }

    override fun bufferedCount(): Flow<Int> = dao.unsyncedCountFlow()

    override suspend fun getRoute(date: String): Resource<RouteInfo> = withContext(io) {
        val userId = tokenManager.userId
            ?: return@withContext Resource.Error("No signed-in user")
        try {
            val dto = api.getRoute(userId, date)
            Resource.Success(
                RouteInfo(
                    distanceKm = dto.distanceKm,
                    points = dto.points.map { LatLng(it.latitude, it.longitude) },
                )
            )
        } catch (e: HttpException) {
            Resource.Error("Could not load route (${e.code()})", e.code())
        } catch (e: IOException) {
            Resource.Error("Offline — route unavailable")
        }
    }
}
