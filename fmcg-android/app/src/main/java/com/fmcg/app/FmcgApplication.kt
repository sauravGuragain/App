package com.fmcg.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.fmcg.app.data.sync.SyncScheduler
import com.fmcg.app.presentation.common.map.OsmConfig
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class FmcgApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var syncScheduler: SyncScheduler

    // WorkManager picks this up (default initializer is removed in the manifest),
    // so @HiltWorker workers can be constructed with injected dependencies.
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        OsmConfig.init(this)          // OSM User-Agent + tile cache
        syncScheduler.schedulePeriodic()
    }
}
