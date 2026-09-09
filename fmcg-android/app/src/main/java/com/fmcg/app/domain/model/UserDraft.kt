package com.fmcg.app.domain.model

/** What the admin fills in to create a new user account. */
data class UserDraft(
    val email: String,
    val fullName: String,
    val role: UserRole,
    val password: String,
    val phone: String? = null,
)
