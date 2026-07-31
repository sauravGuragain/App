package com.fmcg.app.presentation.store

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fmcg.app.domain.model.StoreDraft
import com.fmcg.app.domain.repository.StoreRepository
import com.fmcg.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StoreFormUiState(
    val name: String = "",
    val ownerName: String = "",
    val phone: String = "",
    val address: String = "",
    val notes: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
) {
    val canSave: Boolean get() = name.isNotBlank() && latitude != null && longitude != null
}

@HiltViewModel
class StoreFormViewModel @Inject constructor(
    private val repository: StoreRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val storeId: Int = savedStateHandle.get<Int>("storeId") ?: -1
    val isEditing: Boolean = storeId > 0

    private val _state = MutableStateFlow(StoreFormUiState())
    val state: StateFlow<StoreFormUiState> = _state.asStateFlow()

    init { if (isEditing) prefill() }

    private fun prefill() = viewModelScope.launch {
        when (val r = repository.getStore(storeId)) {
            is Resource.Success -> _state.update {
                it.copy(
                    name = r.data.name,
                    ownerName = r.data.ownerName.orEmpty(),
                    phone = r.data.phone.orEmpty(),
                    address = r.data.address.orEmpty(),
                    notes = r.data.notes.orEmpty(),
                    latitude = r.data.latitude,
                    longitude = r.data.longitude,
                )
            }
            is Resource.Error -> _state.update { it.copy(error = r.message) }
            Resource.Loading -> Unit
        }
    }

    fun onName(v: String) = _state.update { it.copy(name = v) }
    fun onOwner(v: String) = _state.update { it.copy(ownerName = v) }
    fun onPhone(v: String) = _state.update { it.copy(phone = v) }
    fun onAddress(v: String) = _state.update { it.copy(address = v) }
    fun onNotes(v: String) = _state.update { it.copy(notes = v) }

    fun setPickedLocation(lat: Double, lng: Double) =
        _state.update { it.copy(latitude = lat, longitude = lng) }

    fun save() {
        val s = _state.value
        if (!s.canSave || s.isSaving) return
        _state.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            val draft = StoreDraft(
                name = s.name.trim(),
                ownerName = s.ownerName.ifBlank { null },
                phone = s.phone.ifBlank { null },
                address = s.address.ifBlank { null },
                latitude = s.latitude!!,
                longitude = s.longitude!!,
                notes = s.notes.ifBlank { null },
            )
            val result = if (isEditing) repository.updateStore(storeId, draft)
                         else repository.createStore(draft)
            when (result) {
                is Resource.Success -> _state.update { it.copy(isSaving = false, saved = true) }
                is Resource.Error -> _state.update { it.copy(isSaving = false, error = result.message) }
                Resource.Loading -> Unit
            }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }
}
