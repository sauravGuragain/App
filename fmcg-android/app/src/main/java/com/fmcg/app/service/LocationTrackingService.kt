package com.fmcg.app.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.fmcg.app.domain.repository.GpsRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service that records the rep's route during the workday. Fixes are
 * written to Room immediately (offline-first) and pushed to the backend on a
 * periodic ticker plus once more on stop. Robust retry/conflict handling is
 * Phase 11; here we sync best-effort and keep unsent points buffered.
 */
@AndroidEntryPoint
class LocationTrackingService : Service() {

    @Inject lateinit var gpsRepository: GpsRepository
    @Inject lateinit var trackingState: TrackingState

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var syncJob: Job? = null
    private lateinit var fused: FusedLocationProviderClient

    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val loc = result.lastLocation ?: return
            scope.launch {
                gpsRepository.recordPoint(
                    loc.latitude, loc.longitude,
                    if (loc.hasAccuracy()) loc.accuracy else null,
                    System.currentTimeMillis(),
                )
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        fused = LocationServices.getFusedLocationProviderClient(this)
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return START_STICKY
    }

    @SuppressLint("MissingPermission") // caller requests permissions before start
    private fun start() {
        startForeground(NOTIF_ID, buildNotification())
        trackingState.setTracking(true)
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, INTERVAL_MS)
            .setMinUpdateIntervalMillis(FASTEST_MS)
            .setMinUpdateDistanceMeters(MIN_DISTANCE_M)
            .build()
        fused.requestLocationUpdates(request, callback, mainLooper)

        syncJob?.cancel()
        syncJob = scope.launch {
            while (isActive) {
                delay(SYNC_INTERVAL_MS)
                when (val r = gpsRepository.syncPending()) {
                    is com.fmcg.app.util.Resource.Success -> trackingState.addSynced(r.data)
                    else -> Unit  // stay buffered; try again next tick
                }
            }
        }
    }

    private fun stop() {
        fused.removeLocationUpdates(callback)
        syncJob?.cancel()
        scope.launch {
            gpsRepository.syncPending()  // final flush
        }.invokeOnCompletion {
            trackingState.setTracking(false)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification() =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Workday tracking active")
            .setContentText("Recording your route")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Route tracking", NotificationManager.IMPORTANCE_LOW,
            )
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    companion object {
        const val ACTION_START = "com.fmcg.app.action.START_TRACKING"
        const val ACTION_STOP = "com.fmcg.app.action.STOP_TRACKING"
        private const val CHANNEL_ID = "route_tracking"
        private const val NOTIF_ID = 1001
        private const val INTERVAL_MS = 15_000L
        private const val FASTEST_MS = 10_000L
        private const val MIN_DISTANCE_M = 10f
        private const val SYNC_INTERVAL_MS = 60_000L
    }
}
