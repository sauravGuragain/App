package com.fmcg.app.domain.usecase

import com.fmcg.app.domain.model.User
import com.fmcg.app.domain.repository.AuthRepository
import com.fmcg.app.util.Resource
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(): Resource<User> = repository.currentUser()
}
