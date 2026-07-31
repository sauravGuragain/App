package com.fmcg.app.domain.usecase

import com.fmcg.app.domain.model.User
import com.fmcg.app.domain.repository.AuthRepository
import com.fmcg.app.util.Resource
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): Resource<User> {
        if (email.isBlank() || password.isBlank()) {
            return Resource.Error("Email and password are required")
        }
        return repository.login(email.trim(), password)
    }
}
