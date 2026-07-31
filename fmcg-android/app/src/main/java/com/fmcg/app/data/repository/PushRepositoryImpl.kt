package com.fmcg.app.data.repository

import com.fmcg.app.data.local.TokenManager
import com.fmcg.app.data.remote.api.NotificationApi
import com.fmcg.app.data.remote.dto.DeviceTokenRegisterDto
import com.fmcg.app.domain.repository.PushRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume

class PushRepositoryImpl @Inject constructor(
    private val api: NotificationApi,
    private val tokenManager: TokenManager,
    private val io: CoroutineDispatcher,
) : PushRepository {

    override suspend fun register(token: String) = withContext(io) {
        if (!tokenManager.isLoggedIn()) return@withContext
        runCatching { api.register(DeviceTokenRegisterDto(token)) }
        Unit
    }

    override suspend fun registerCurrentToken() {
        val token = fetchToken() ?: return
        register(token)
    }

    // Bridges the Firebase Task API to coroutines without an extra dependency.
    private suspend fun fetchToken(): String? = suspendCancellableCoroutine { cont ->
        try {
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resume(null) }
        } catch (e: Exception) {
            cont.resume(null)   // Firebase not initialised (no google-services.json)
        }
    }
}
