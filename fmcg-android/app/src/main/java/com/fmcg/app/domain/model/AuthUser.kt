package com.fmcg.app.domain.model

/** The authenticated user, as the app cares about it (no wire details). */
data class AuthUser(
    val id: Int,
    val email: String,
    val fullName: String,
    val role: UserRole,
    val phone: String?,
    val isActive: Boolean,
)
