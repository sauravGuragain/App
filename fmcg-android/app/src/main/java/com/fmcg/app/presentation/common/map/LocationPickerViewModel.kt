package com.fmcg.app.presentation.common.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fmcg.app.data.location.DeviceLocationProvider
import com.fmcg.app.data.location.PlaceResult
import com.fmcg.app.data.location.PlaceSearch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PickedPoint(val lat: Double, val lng: Double, val label: String? = null)

data class LocationPickerUiState(
    val picked: PickedPoint? = null,
    val query: String = "",
    val results: List<PlaceResult> = emptyList(),
    val isSearching: Boolean = false,
    val isLocating: Boolean = false,
    val message: String? = null,
    /** Set when the camera should move; cleared once consumed. */
    val recenterTo: PickedPoint? = null,
)

@HiltViewModel
class LocationPickerViewModel @Inject constructor(
    private val deviceLocation: DeviceLocationProvider,
    private val placeSearch: PlaceSearch,
) : ViewModel() {

    private val _state = MutableStateFlow(LocationPickerUiState())
    val state: StateFlow<LocationPickerUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    fun hasLocationPermission(): Boolean = deviceLocation.hasPermission()

    /** Called on open and from the "My location" button. */
    fun detectCurrentLocation() {
        if (!deviceLocation.hasPermission()) return
        _state.update { it.copy(isLocating = true, message = null) }
        viewModelScope.launch {
            val fix = deviceLocation.current()
            if (fix == null) {
                _state.update {
                    it.copy(
                        isLocating = false,
                        message = "Couldn't get a GPS fix — tap the map or search instead",
                    )
                }
            } else {
                val point = PickedPoint(fix.latitude, fix.longitude, "Current location")
                _state.update {
                    it.copy(isLocating = false, picked = point, recenterTo = point)
                }
            }
        }
    }

    fun onQueryChange(value: String) {
        _state.update { it.copy(query = value) }
        searchJob?.cancel()
        if (value.isBlank()) {
            _state.update { it.copy(results = emptyList(), isSearching = false) }
            return
        }
        // Nominatim asks callers to stay near one request per second.
        searchJob = viewModelScope.launch {
            delay(600)
            _state.update { it.copy(isSearching = true) }
            val results = placeSearch.search(value)
            _state.update {
                it.copy(
                    isSearching = false,
                    results = results,
                    message = if (results.isEmpty()) "No matching places found" else null,
                )
            }
        }
    }

    fun onResultSelected(result: PlaceResult) {
        val point = PickedPoint(result.lat, result.lng, result.label)
        _state.update {
            it.copy(picked = point, recenterTo = point, results = emptyList(), query = "")
        }
    }

    fun onMapTap(lat: Double, lng: Double) {
        _state.update { it.copy(picked = PickedPoint(lat, lng), message = null) }
    }

    /** Manual latitude/longitude entry, for coordinates read off another device. */
    fun onCoordinatesEntered(latText: String, lngText: String) {
        val lat = latText.trim().toDoubleOrNull()
        val lng = lngText.trim().toDoubleOrNull()
        when {
            lat == null || lng == null ->
                _state.update { it.copy(message = "Enter numbers for latitude and longitude") }
            lat !in -90.0..90.0 ->
                _state.update { it.copy(message = "Latitude must be between -90 and 90") }
            lng !in -180.0..180.0 ->
                _state.update { it.copy(message = "Longitude must be between -180 and 180") }
            else -> {
                val point = PickedPoint(lat, lng, "Entered manually")
                _state.update { it.copy(picked = point, recenterTo = point, message = null) }
            }
        }
    }

    fun onRecenterConsumed() = _state.update { it.copy(recenterTo = null) }
    fun clearMessage() = _state.update { it.copy(message = null) }
}
