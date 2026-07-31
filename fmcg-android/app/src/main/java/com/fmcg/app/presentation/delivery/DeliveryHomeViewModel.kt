package com.fmcg.app.presentation.delivery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fmcg.app.domain.model.Delivery
import com.fmcg.app.domain.repository.DeliveryRepository
import com.fmcg.app.domain.usecase.LogoutUseCase
import com.fmcg.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DeliveryHomeUiState(
    val isLoading: Boolean = true,
    val deliveries: List<Delivery> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class DeliveryHomeViewModel @Inject constructor(
    private val repository: DeliveryRepository,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(DeliveryHomeUiState())
    val state: StateFlow<DeliveryHomeUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val r = repository.listMine()) {
                is Resource.Success -> _state.update { it.copy(isLoading = false, deliveries = r.data) }
                is Resource.Error -> _state.update { it.copy(isLoading = false, error = r.message) }
                Resource.Loading -> Unit
            }
        }
    }

    fun logout() = logoutUseCase()
}
