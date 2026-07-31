package com.fmcg.app.data.repository

import com.fmcg.app.data.local.TokenManager
import com.fmcg.app.data.mapper.toDomain
import com.fmcg.app.data.remote.api.AuthApi
import com.fmcg.app.domain.model.User
import com.fmcg.app.domain.repository.AuthRepository
import com.fmcg.app.util.Resource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val tokenManager: TokenManager,
    private val io: CoroutineDispatcher,
) : AuthRepository {

    override suspend fun login(email: String, password: String): Resource<User> =
        withContext(io) {
            try {
                val tokens = api.login(email, password)
                tokenManager.save(tokens.accessToken, tokens.refreshToken)
                val user = api.me().toDomain()
                tokenManager.userId = user.id
                tokenManager.role = user.role.name
                Resource.Success(user)
            } catch (e: HttpException) {
                val msg = if (e.code() == 401) "Incorrect email or password"
                          else "Login failed (${e.code()})"
                Resource.Error(msg, e.code())
            } catch (e: IOException) {
                Resource.Error("Network error — check your connection")
            }
        }

    override suspend fun currentUser(): Resource<User> = withContext(io) {
        try {
            val user = api.me().toDomain()
            tokenManager.userId = user.id
            tokenManager.role = user.role.name
            Resource.Success(user)
        } catch (e: HttpException) {
            Resource.Error("Session expired", e.code())
        } catch (e: IOException) {
            Resource.Error("Network error — check your connection")
        }
    }

    override fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()

    override fun logout() = tokenManager.clear()
}
