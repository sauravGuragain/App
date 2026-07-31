package com.fmcg.app.data.remote.api

import com.fmcg.app.data.remote.dto.OrderCreateDto
import com.fmcg.app.data.remote.dto.OrderDto
import com.fmcg.app.data.remote.dto.OrderPageDto
import com.fmcg.app.data.remote.dto.OrderStatusUpdateDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface OrderApi {
    @GET("orders")
    suspend fun list(
        @Query("store_id") storeId: Int? = null,
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 100,
    ): OrderPageDto

    @GET("orders/{id}")
    suspend fun get(@Path("id") id: Int): OrderDto

    @POST("orders")
    suspend fun create(@Body body: OrderCreateDto): OrderDto

    @PATCH("orders/{id}/status")
    suspend fun updateStatus(@Path("id") id: Int, @Body body: OrderStatusUpdateDto): OrderDto
}
