package com.fmcg.app.domain.repository

import com.fmcg.app.domain.model.User
import com.fmcg.app.util.Resource

interface AuthRepository {
    suspend fun login(email: String, password: String): Resource<User>
    suspend fun currentUser(): Resource<User>
    fun isLoggedIn(): Boolean
    fun logout()
}
