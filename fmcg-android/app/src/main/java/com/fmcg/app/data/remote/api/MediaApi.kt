package com.fmcg.app.data.remote.api

import com.fmcg.app.data.remote.dto.MediaUrlDto
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface MediaApi {
    @Multipart
    @POST("media/upload")
    suspend fun upload(@Part file: MultipartBody.Part): MediaUrlDto
}
