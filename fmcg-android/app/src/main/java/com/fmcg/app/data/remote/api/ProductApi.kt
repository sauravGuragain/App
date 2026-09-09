package com.fmcg.app.data.remote.api

import com.fmcg.app.data.remote.dto.MessageDto
import com.fmcg.app.data.remote.dto.ProductCreateDto
import com.fmcg.app.data.remote.dto.ProductDto
import com.fmcg.app.data.remote.dto.ProductPageDto
import com.fmcg.app.data.remote.dto.ProductUpdateDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductApi {
    @GET("products")
    suspend fun list(
        @Query("active_only") activeOnly: Boolean = true,
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 200,
    ): ProductPageDto

    @POST("products")
    suspend fun create(@Body body: ProductCreateDto): ProductDto

    @PATCH("products/{id}")
    suspend fun update(@Path("id") id: Int, @Body body: ProductUpdateDto): ProductDto

    @DELETE("products/{id}")
    suspend fun delete(@Path("id") id: Int): MessageDto
}
