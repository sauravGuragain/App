package com.fmcg.app.presentation.marketing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fmcg.app.data.sync.OutboxManager
import com.fmcg.app.domain.repository.GpsRepository
import com.fmcg.app.domain.usecase.LogoutUseCase
import com.fmcg.app.service.TrackingController
import com.fmcg.app.service.TrackingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MarketingViewModel @Inject constructor(
    private val controller: TrackingController,
    private val logoutUseCase: LogoutUseCase,
    gpsRepository: GpsRepository,
    outboxManager: OutboxManager,
    trackingState: TrackingState,
) : ViewModel() {

    val isTracking: StateFlow<Boolean> = trackingState.isTracking
    val syncedCount: StateFlow<Int> = trackingState.lastSyncedCount
    val pendingSync: StateFlow<Int> =
        outboxManager.pendingCount()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
    val bufferedCount: StateFlow<Int> =
        gpsRepository.bufferedCount()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun startWorkday() = controller.startWorkday()
    fun endWorkday() = controller.endWorkday()
    fun logout() = logoutUseCase()
}
