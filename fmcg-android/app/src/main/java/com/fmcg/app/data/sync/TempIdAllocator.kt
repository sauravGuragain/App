package com.fmcg.app.data.sync

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Hands out decreasing negative ids for entities created offline, so they
 *  can't collide with server-assigned (positive) ids. The sync worker maps
 *  each temp id to its real id once the create reaches the backend. */
@Singleton
class TempIdAllocator @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences("sync_ids", Context.MODE_PRIVATE)

    @Synchronized
    fun next(): Int {
        val n = prefs.getInt(KEY, -1)
        prefs.edit().putInt(KEY, n - 1).apply()
        return n
    }

    private companion object { const val KEY = "next_temp_id" }
}
