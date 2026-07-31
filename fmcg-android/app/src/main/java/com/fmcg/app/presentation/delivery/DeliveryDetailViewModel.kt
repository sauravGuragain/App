package com.fmcg.app.presentation.delivery

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fmcg.app.domain.model.Delivery
import com.fmcg.app.domain.model.DeliveryStatus
import com.fmcg.app.domain.repository.DeliveryRepository
import com.fmcg.app.domain.repository.OrderRepository
import com.fmcg.app.domain.repository.StoreRepository
import com.fmcg.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class DeliveryDetailUiState(
    val isLoading: Boolean = true,
    val delivery: Delivery? = null,
    val storeName: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val notes: String = "",
    val proofUrl: String? = null,
    val isBusy: Boolean = false,
    val error: String? = null,
    val done: Boolean = false,
)

@HiltViewModel
class DeliveryDetailViewModel @Inject constructor(
    private val deliveryRepository: DeliveryRepository,
    private val orderRepository: OrderRepository,
    private val storeRepository: StoreRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val deliveryId: Int = savedStateHandle.get<Int>("deliveryId") ?: -1

    private val _state = MutableStateFlow(DeliveryDetailUiState())
    val state: StateFlow<DeliveryDetailUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val d = deliveryRepository.getDelivery(deliveryId)) {
                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            delivery = d.data,
                            notes = d.data.notes.orEmpty(),
                            proofUrl = d.data.proofPhotoUrl,
                        )
                    }
                    resolveStore(d.data.orderId)
                }
                is Resource.Error -> _state.update { it.copy(isLoading = false, error = d.message) }
                Resource.Loading -> Unit
            }
        }
    }

    private suspend fun resolveStore(orderId: Int) {
        val order = orderRepository.getOrder(orderId)
        if (order is Resource.Success) {
            val store = storeRepository.getStore(order.data.storeId)
            if (store is Resource.Success) {
                _state.update {
                    it.copy(
                        storeName = store.data.name,
                        latitude = store.data.latitude,
                        longitude = store.data.longitude,
                    )
                }
            }
        }
    }

    fun setNotes(v: String) = _state.update { it.copy(notes = v) }

    fun onProofCaptured(path: String) = viewModelScope.launch {
        _state.update { it.copy(isBusy = true, error = null) }
        when (val r = deliveryRepository.uploadProof(File(path))) {
            is Resource.Success -> _state.update { it.copy(isBusy = false, proofUrl = r.data) }
            is Resource.Error -> _state.update { it.copy(isBusy = false, error = r.message) }
            Resource.Loading -> Unit
        }
    }

    fun startDelivery() = changeStatus(DeliveryStatus.OUT_FOR_DELIVERY)

    fun markDelivered() {
        if (_state.value.proofUrl == null) {
            _state.update { it.copy(error = "Capture a proof photo first") }
            return
        }
        changeStatus(DeliveryStatus.DELIVERED, withProof = true)
    }

    fun markFailed() = changeStatus(DeliveryStatus.FAILED)

    private fun changeStatus(target: DeliveryStatus, withProof: Boolean = false) {
        val s = _state.value
        if (s.isBusy) return
        _state.update { it.copy(isBusy = true, error = null) }
        viewModelScope.launch {
            val r = deliveryRepository.updateStatus(
                deliveryId, target,
                notes = s.notes.ifBlank { null },
                proofPhotoUrl = if (withProof) s.proofUrl else null,
            )
            when (r) {
                is Resource.Success -> _state.update {
                    it.copy(isBusy = false, delivery = r.data,
                            done = target == DeliveryStatus.DELIVERED || target == DeliveryStatus.FAILED)
                }
                is Resource.Error -> _state.update { it.copy(isBusy = false, error = r.message) }
                Resource.Loading -> Unit
            }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }
}
