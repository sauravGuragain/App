package com.fmcg.app.data.remote.interceptor

import com.fmcg.app.data.local.TokenManager
import com.fmcg.app.data.remote.api.AuthApi
import com.fmcg.app.data.remote.dto.RefreshRequestDto
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Named

/**
 * On a 401, exchanges the refresh token for a new pair and retries once.
 * Uses a dedicated [refreshApi] built on a bare OkHttp client (no interceptor,
 * no authenticator) so the refresh call itself can never recurse into here.
 */
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    @Named("refresh") private val refreshApi: AuthApi,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null  // already retried once
        val refresh = tokenManager.refreshToken ?: return null

        val newTokens = runCatching {
            runBlocking { refreshApi.refresh(RefreshRequestDto(refresh)) }
        }.getOrNull() ?: run {
            tokenManager.clear()          // refresh failed → force re-login
            return null
        }

        tokenManager.save(newTokens.accessToken, newTokens.refreshToken)
        return response.request.newBuilder()
            .header("Authorization", "Bearer ${newTokens.accessToken}")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
