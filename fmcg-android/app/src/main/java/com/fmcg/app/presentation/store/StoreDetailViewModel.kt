package com.fmcg.app.presentation.store

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fmcg.app.domain.model.Order
import com.fmcg.app.domain.model.Store
import com.fmcg.app.domain.repository.OrderRepository
import com.fmcg.app.domain.repository.StoreRepository
import com.fmcg.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StoreDetailUiState(
    val isLoading: Boolean = true,
    val store: Store? = null,
    val orders: List<Order> = emptyList(),
    val error: String? = null,
    val deleted: Boolean = false,
)

@HiltViewModel
class StoreDetailViewModel @Inject constructor(
    private val storeRepository: StoreRepository,
    private val orderRepository: OrderRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val storeId: Int = savedStateHandle.get<Int>("storeId") ?: -1

    private val _state = MutableStateFlow(StoreDetailUiState())
    val state: StateFlow<StoreDetailUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val r = storeRepository.getStore(storeId)) {
                is Resource.Success -> _state.update { it.copy(isLoading = false, store = r.data) }
                is Resource.Error -> _state.update { it.copy(isLoading = false, error = r.message) }
                Resource.Loading -> Unit
            }
        }
        viewModelScope.launch {
            (orderRepository.listOrders(storeId) as? Resource.Success)?.let { res ->
                _state.update { it.copy(orders = res.data) }
            }
        }
    }

    fun delete() = viewModelScope.launch {
        when (val r = storeRepository.deleteStore(storeId)) {
            is Resource.Success -> _state.update { it.copy(deleted = true) }
            is Resource.Error -> _state.update { it.copy(error = r.message) }
            Resource.Loading -> Unit
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }
}
