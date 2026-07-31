package com.fmcg.app.data.remote.api

import com.fmcg.app.data.remote.dto.MessageDto
import com.fmcg.app.data.remote.dto.StoreCreateDto
import com.fmcg.app.data.remote.dto.StoreDto
import com.fmcg.app.data.remote.dto.StorePageDto
import com.fmcg.app.data.remote.dto.StoreUpdateDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface StoreApi {
    @GET("stores")
    suspend fun list(
        @Query("search") search: String? = null,
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 200,
    ): StorePageDto

    @GET("stores/{id}")
    suspend fun get(@Path("id") id: Int): StoreDto

    @POST("stores")
    suspend fun create(@Body body: StoreCreateDto): StoreDto

    @PATCH("stores/{id}")
    suspend fun update(@Path("id") id: Int, @Body body: StoreUpdateDto): StoreDto

    @DELETE("stores/{id}")
    suspend fun delete(@Path("id") id: Int): MessageDto
}
