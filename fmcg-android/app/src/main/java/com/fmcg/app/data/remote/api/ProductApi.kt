package com.fmcg.app.data.remote.api

import com.fmcg.app.data.remote.dto.ProductPageDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ProductApi {
    @GET("products")
    suspend fun list(
        @Query("active_only") activeOnly: Boolean = true,
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 200,
    ): ProductPageDto
}
