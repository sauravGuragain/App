package com.fmcg.app.presentation.order

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fmcg.app.domain.model.Order
import com.fmcg.app.domain.repository.OrderRepository
import com.fmcg.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrderListUiState(
    val isLoading: Boolean = true,
    val orders: List<Order> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class OrderListViewModel @Inject constructor(
    private val repository: OrderRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val storeId: Int? = savedStateHandle.get<Int>("storeId")?.takeIf { it > 0 }

    private val _state = MutableStateFlow(OrderListUiState())
    val state: StateFlow<OrderListUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val r = repository.listOrders(storeId)) {
                is Resource.Success -> _state.update { it.copy(isLoading = false, orders = r.data) }
                is Resource.Error -> _state.update { it.copy(isLoading = false, error = r.message) }
                Resource.Loading -> Unit
            }
        }
    }
}
