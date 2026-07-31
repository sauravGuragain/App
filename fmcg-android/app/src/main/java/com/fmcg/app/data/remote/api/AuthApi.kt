package com.fmcg.app.data.remote.api

import com.fmcg.app.data.remote.dto.RefreshRequestDto
import com.fmcg.app.data.remote.dto.TokenDto
import com.fmcg.app.data.remote.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    // Backend /auth/login uses the OAuth2 password form (username = email).
    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
    ): TokenDto

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequestDto): TokenDto

    @GET("auth/me")
    suspend fun me(): UserDto
}
