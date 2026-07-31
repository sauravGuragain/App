package com.fmcg.app.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fmcg.app.domain.model.UserRole
import com.fmcg.app.domain.repository.AuthRepository
import com.fmcg.app.domain.repository.PushRepository
import com.fmcg.app.domain.usecase.GetCurrentUserUseCase
import com.fmcg.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SplashDestination {
    data object Login : SplashDestination
    data class Home(val role: UserRole) : SplashDestination
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getCurrentUser: GetCurrentUserUseCase,
    private val authRepository: AuthRepository,
    private val pushRepository: PushRepository,
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination?>(null)
    val destination: StateFlow<SplashDestination?> = _destination.asStateFlow()

    init { resolve() }

    private fun resolve() = viewModelScope.launch {
        if (!authRepository.isLoggedIn()) {
            _destination.value = SplashDestination.Login
            return@launch
        }
        _destination.value = when (val res = getCurrentUser()) {
            is Resource.Success -> {
                pushRepository.registerCurrentToken()
                SplashDestination.Home(res.data.role)
            }
            else -> SplashDestination.Login
        }
    }
}
