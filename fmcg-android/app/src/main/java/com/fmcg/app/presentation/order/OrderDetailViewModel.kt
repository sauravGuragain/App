package com.fmcg.app.presentation.order

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fmcg.app.data.local.TokenManager
import com.fmcg.app.domain.model.Order
import com.fmcg.app.domain.model.OrderStatus
import com.fmcg.app.domain.repository.OrderRepository
import com.fmcg.app.domain.repository.ProductRepository
import com.fmcg.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrderDetailUiState(
    val isLoading: Boolean = true,
    val order: Order? = null,
    val productNames: Map<Int, String> = emptyMap(),
    val isAdmin: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    tokenManager: TokenManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val orderId: Int = savedStateHandle.get<Int>("orderId") ?: -1
    private val isAdmin: Boolean = tokenManager.role == "ADMIN"

    private val _state = MutableStateFlow(OrderDetailUiState(isAdmin = isAdmin))
    val state: StateFlow<OrderDetailUiState> = _state.asStateFlow()

    init {
        observeProductNames()
        load()
    }

    private fun observeProductNames() = viewModelScope.launch {
        productRepository.refresh()
        productRepository.observeProducts().collect { products ->
            _state.update { it.copy(productNames = products.associate { p -> p.id to p.name }) }
        }
    }

    fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val r = orderRepository.getOrder(orderId)) {
                is Resource.Success -> _state.update { it.copy(isLoading = false, order = r.data) }
                is Resource.Error -> _state.update { it.copy(isLoading = false, error = r.message) }
                Resource.Loading -> Unit
            }
        }
    }

    fun changeStatus(target: OrderStatus) = viewModelScope.launch {
        when (val r = orderRepository.updateStatus(orderId, target)) {
            is Resource.Success -> _state.update { it.copy(order = r.data) }
            is Resource.Error -> _state.update { it.copy(error = r.message) }
            Resource.Loading -> Unit
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }
}
