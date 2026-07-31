package com.fmcg.app.presentation.order

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fmcg.app.domain.model.OrderDraft
import com.fmcg.app.domain.model.OrderLineDraft
import com.fmcg.app.domain.model.Product
import com.fmcg.app.domain.model.Store
import com.fmcg.app.domain.repository.OrderRepository
import com.fmcg.app.domain.repository.ProductRepository
import com.fmcg.app.domain.repository.StoreRepository
import com.fmcg.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

data class LineInput(
    val key: Long,
    val productId: Int? = null,
    val productName: String = "",
    val quantity: String = "1",
    val unitPrice: String = "",
    val discount: String = "0",
)

data class OrderCreateUiState(
    val lockedStore: Boolean = false,
    val stores: List<Store> = emptyList(),
    val products: List<Product> = emptyList(),
    val selectedStoreId: Int? = null,
    val lines: List<LineInput> = emptyList(),
    val notes: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
    val submitted: Boolean = false,
    val queuedOffline: Boolean = false,
) {
    val total: String
        get() = lines.fold(BigDecimal.ZERO) { acc, l ->
            val price = l.unitPrice.toBigDecimalOrNull() ?: BigDecimal.ZERO
            val qty = l.quantity.toBigDecimalOrNull() ?: BigDecimal.ZERO
            val disc = l.discount.toBigDecimalOrNull() ?: BigDecimal.ZERO
            acc + (price * qty - disc)
        }.toPlainString()

    val canSave: Boolean
        get() = selectedStoreId != null && lines.any { it.productId != null }
}

@HiltViewModel
class OrderCreateViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val storeRepository: StoreRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val lockedStoreId: Int? = savedStateHandle.get<Int>("storeId")?.takeIf { it > 0 }
    private var seq = 0L

    private val _state = MutableStateFlow(
        OrderCreateUiState(
            lockedStore = lockedStoreId != null,
            selectedStoreId = lockedStoreId,
            lines = listOf(LineInput(key = seq++)),
        )
    )
    val state: StateFlow<OrderCreateUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch { productRepository.refresh() }
        viewModelScope.launch { storeRepository.refresh() }
        viewModelScope.launch {
            productRepository.observeProducts().collect { p ->
                _state.update { it.copy(products = p) }
            }
        }
        viewModelScope.launch {
            storeRepository.observeStores("").collect { s ->
                _state.update { it.copy(stores = s) }
            }
        }
    }

    fun setStore(id: Int) = _state.update { it.copy(selectedStoreId = id) }
    fun setNotes(v: String) = _state.update { it.copy(notes = v) }

    fun addLine() = _state.update { it.copy(lines = it.lines + LineInput(key = seq++)) }

    fun removeLine(key: Long) = _state.update { s ->
        s.copy(lines = if (s.lines.size > 1) s.lines.filterNot { it.key == key } else s.lines)
    }

    fun selectProduct(key: Long, product: Product) = _state.update { s ->
        s.copy(lines = s.lines.map {
            if (it.key == key) it.copy(
                productId = product.id,
                productName = product.name,
                unitPrice = if (it.unitPrice.isBlank()) product.defaultPrice else it.unitPrice,
            ) else it
        })
    }

    fun updateLine(key: Long, quantity: String? = null, unitPrice: String? = null, discount: String? = null) =
        _state.update { s ->
            s.copy(lines = s.lines.map {
                if (it.key == key) it.copy(
                    quantity = quantity ?: it.quantity,
                    unitPrice = unitPrice ?: it.unitPrice,
                    discount = discount ?: it.discount,
                ) else it
            })
        }

    fun submit() {
        val s = _state.value
        if (!s.canSave || s.isSaving) return
        val lines = s.lines.mapNotNull { l ->
            val pid = l.productId ?: return@mapNotNull null
            val qty = l.quantity.toIntOrNull() ?: return@mapNotNull null
            if (qty <= 0) return@mapNotNull null
            val price = l.unitPrice.toBigDecimalOrNull() ?: return@mapNotNull null
            OrderLineDraft(pid, qty, price.toPlainString(),
                (l.discount.toBigDecimalOrNull() ?: BigDecimal.ZERO).toPlainString())
        }
        if (lines.isEmpty()) {
            _state.update { it.copy(error = "Add at least one valid line item") }
            return
        }
        _state.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            val draft = OrderDraft(s.selectedStoreId!!, lines, s.notes.ifBlank { null })
            when (val r = orderRepository.createOrder(draft)) {
                is Resource.Success -> _state.update { it.copy(isSaving = false, submitted = true, queuedOffline = r.data) }
                is Resource.Error -> _state.update { it.copy(isSaving = false, error = r.message) }
                Resource.Loading -> Unit
            }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }
}
