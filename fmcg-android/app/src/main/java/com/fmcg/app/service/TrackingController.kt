package com.fmcg.app.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Thin wrapper so ViewModels can start/stop the foreground service without
 *  holding an Activity context. */
@Singleton
class TrackingController @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun startWorkday() {
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_START
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun endWorkday() {
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_STOP
        }
        context.startService(intent)
    }
}
