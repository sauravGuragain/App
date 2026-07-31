package com.fmcg.app.data.remote.api

import com.fmcg.app.data.remote.dto.DeviceTokenRegisterDto
import com.fmcg.app.data.remote.dto.MessageDto
import retrofit2.http.Body
import retrofit2.http.POST

interface NotificationApi {
    @POST("notifications/register")
    suspend fun register(@Body body: DeviceTokenRegisterDto): MessageDto
}
