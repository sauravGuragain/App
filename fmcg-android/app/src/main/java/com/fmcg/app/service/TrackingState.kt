package com.fmcg.app.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Process-wide tracking flags the service writes and the UI observes. */
@Singleton
class TrackingState @Inject constructor() {
    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _lastSyncedCount = MutableStateFlow(0)
    val lastSyncedCount: StateFlow<Int> = _lastSyncedCount.asStateFlow()

    fun setTracking(value: Boolean) { _isTracking.value = value }
    fun addSynced(n: Int) { _lastSyncedCount.value += n }
}
