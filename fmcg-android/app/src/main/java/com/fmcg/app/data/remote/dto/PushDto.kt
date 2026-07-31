package com.fmcg.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class DeviceTokenRegisterDto(
    val token: String,
    val platform: String = "android",
)
