package com.fmcg.app.domain.repository

interface PushRepository {
    /** Register a specific FCM token with the backend (best-effort). */
    suspend fun register(token: String)
    /** Fetch the current FCM token and register it. No-op if Firebase isn't
     *  configured or the user isn't signed in. */
    suspend fun registerCurrentToken()
}
