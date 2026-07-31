package com.fmcg.app.domain.usecase

import com.fmcg.app.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    operator fun invoke() = repository.logout()
}
