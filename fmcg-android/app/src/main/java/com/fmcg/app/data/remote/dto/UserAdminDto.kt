package com.fmcg.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserPageDto(
    val items: List<UserDto>,
    val total: Int,
    val skip: Int,
    val limit: Int,
)

@Serializable
data class UserCreateDto(
    val email: String,
    @SerialName("full_name") val fullName: String,
    val role: String,
    val password: String,
    val phone: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
)
