package com.fmcg.app.data.remote.api

import com.fmcg.app.data.remote.dto.GpsBatchDto
import com.fmcg.app.data.remote.dto.MessageDto
import com.fmcg.app.data.remote.dto.RouteDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GpsApi {
    @POST("gps/{userId}/batch")
    suspend fun uploadBatch(
        @Path("userId") userId: Int,
        @Body body: GpsBatchDto,
    ): MessageDto

    @GET("gps/{userId}/route")
    suspend fun getRoute(
        @Path("userId") userId: Int,
        @Query("date") date: String,
    ): RouteDto
}
