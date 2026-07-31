package com.fmcg.app.domain.model

data class User(
    val id: Int,
    val email: String,
    val fullName: String,
    val role: UserRole,
    val phone: String?,
    val isActive: Boolean,
)
