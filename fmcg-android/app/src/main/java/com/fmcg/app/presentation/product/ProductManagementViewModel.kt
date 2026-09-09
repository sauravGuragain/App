package com.fmcg.app.presentation.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fmcg.app.domain.model.Product
import com.fmcg.app.domain.model.ProductDraft
import com.fmcg.app.domain.repository.ProductRepository
import com.fmcg.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductManagementUiState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val message: String? = null,
    // form
    val editingId: Int? = null,
    val name: String = "",
    val sku: String = "",
    val unit: String = "pcs",
    val price: String = "",
) {
    val isEditing: Boolean get() = editingId != null

    val canSubmit: Boolean
        get() = name.isNotBlank() &&
            (isEditing || sku.isNotBlank()) &&
            (price.toDoubleOrNull() ?: 0.0) > 0.0 &&
            !isSaving
}

@HiltViewModel
class ProductManagementViewModel @Inject constructor(
    private val repository: ProductRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProductManagementUiState())
    val state: StateFlow<ProductManagementUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeAllProducts().collect { list ->
                _state.update { it.copy(products = list) }
            }
        }
        refresh()
    }

    fun refresh() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = repository.refresh()
            _state.update {
                it.copy(
                    isLoading = false,
                    message = (result as? Resource.Error)?.message,
                )
            }
        }
    }

    fun onNameChange(v: String) = _state.update { it.copy(name = v) }
    fun onSkuChange(v: String) = _state.update { it.copy(sku = v) }
    fun onUnitChange(v: String) = _state.update { it.copy(unit = v) }
    fun onPriceChange(v: String) = _state.update { it.copy(price = v) }
    fun clearMessage() = _state.update { it.copy(message = null) }

    fun startEditing(product: Product) = _state.update {
        it.copy(
            editingId = product.id,
            name = product.name,
            sku = product.sku,
            unit = product.unit,
            price = product.defaultPrice,
        )
    }

    fun cancelEditing() = _state.update {
        it.copy(editingId = null, name = "", sku = "", unit = "pcs", price = "")
    }

    fun submit() {
        val s = _state.value
        if (!s.canSubmit) return
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val result = if (s.editingId != null) {
                repository.updateProduct(
                    id = s.editingId,
                    name = s.name,
                    unit = s.unit,
                    defaultPrice = s.price,
                )
            } else {
                repository.createProduct(
                    ProductDraft(
                        name = s.name,
                        sku = s.sku,
                        unit = s.unit,
                        defaultPrice = s.price,
                    )
                )
            }
            when (result) {
                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            isSaving = false,
                            editingId = null,
                            name = "",
                            sku = "",
                            unit = "pcs",
                            price = "",
                            message = if (s.editingId != null) "Product updated"
                                      else "Added ${result.data.name}",
                        )
                    }
                }
                is Resource.Error ->
                    _state.update { it.copy(isSaving = false, message = result.message) }
                Resource.Loading -> Unit
            }
        }
    }

    fun toggleActive(product: Product) {
        viewModelScope.launch {
            val result = repository.updateProduct(product.id, isActive = !product.isActive)
            if (result is Resource.Error) {
                _state.update { it.copy(message = result.message) }
            }
        }
    }
}
