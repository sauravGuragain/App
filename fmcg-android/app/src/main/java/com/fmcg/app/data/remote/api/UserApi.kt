package com.fmcg.app.data.remote.api

import com.fmcg.app.data.remote.dto.MessageDto
import com.fmcg.app.data.remote.dto.UserCreateDto
import com.fmcg.app.data.remote.dto.UserDto
import com.fmcg.app.data.remote.dto.UserPageDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApi {
    @GET("users")
    suspend fun list(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 200,
    ): UserPageDto

    @POST("users")
    suspend fun create(@Body body: UserCreateDto): UserDto

    @DELETE("users/{id}")
    suspend fun delete(@Path("id") id: Int): MessageDto
}
