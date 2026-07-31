package com.fmcg.app.data.remote.api

import com.fmcg.app.data.remote.dto.DeliveryDto
import com.fmcg.app.data.remote.dto.DeliveryPageDto
import com.fmcg.app.data.remote.dto.DeliveryStatusUpdateDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface DeliveryApi {
    @GET("deliveries")
    suspend fun list(
        @Query("driver_id") driverId: Int? = null,
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 100,
    ): DeliveryPageDto

    @GET("deliveries/{id}")
    suspend fun get(@Path("id") id: Int): DeliveryDto

    @PATCH("deliveries/{id}/status")
    suspend fun updateStatus(
        @Path("id") id: Int,
        @Body body: DeliveryStatusUpdateDto,
    ): DeliveryDto
}
