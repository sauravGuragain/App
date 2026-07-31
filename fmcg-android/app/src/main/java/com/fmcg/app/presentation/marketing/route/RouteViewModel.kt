package com.fmcg.app.presentation.marketing.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fmcg.app.domain.model.LatLng
import com.fmcg.app.domain.repository.GpsRepository
import com.fmcg.app.util.Resource
import com.fmcg.app.util.todayUtc
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RouteUiState(
    val isLoading: Boolean = true,
    val points: List<LatLng> = emptyList(),
    val distanceKm: Double = 0.0,
    val error: String? = null,
)

@HiltViewModel
class RouteViewModel @Inject constructor(
    private val gpsRepository: GpsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RouteUiState())
    val state: StateFlow<RouteUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val res = gpsRepository.getRoute(todayUtc())) {
                is Resource.Success -> _state.update {
                    it.copy(isLoading = false, points = res.data.points,
                            distanceKm = res.data.distanceKm)
                }
                is Resource.Error -> _state.update {
                    it.copy(isLoading = false, error = res.message)
                }
                Resource.Loading -> Unit
            }
        }
    }
}
