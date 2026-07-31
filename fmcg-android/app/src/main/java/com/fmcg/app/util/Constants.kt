package com.fmcg.app.util

object Constants {
    const val TOKEN_PREFS = "fmcg_auth_prefs"
    const val KEY_ACCESS_TOKEN = "access_token"
    const val KEY_REFRESH_TOKEN = "refresh_token"

    // Paths that must NOT carry an Authorization header (they mint/renew tokens).
    val AUTH_FREE_PATHS = listOf("/auth/login", "/auth/refresh")
}
