package com.fmcg.app.data.remote.api

import com.fmcg.app.data.remote.dto.RefreshRequestDto
import com.fmcg.app.data.remote.dto.TokenDto
import com.fmcg.app.data.remote.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Backend surface. Auth endpoints are added here in Phase 5; feature endpoints
 * (stores, orders, deliveries, gps) are appended in their respective phases.
 */
interface FmcgApi {

    // OAuth2 password form: username = email.
    @FormUrlEncoded
    @POST("api/v1/auth/login")
    suspend fun login(
        @Field("username") email: String,
        @Field("password") password: String,
    ): TokenDto

    @POST("api/v1/auth/refresh")
    suspend fun refresh(@Body body: RefreshRequestDto): TokenDto

    @GET("api/v1/auth/me")
    suspend fun me(): UserDto
}
