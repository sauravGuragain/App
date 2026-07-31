package com.fmcg.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A buffered GPS fix. Lives locally until pushed to the backend; `synced`
 *  guards against re-uploading and enables offline-first collection. */
@Entity(tableName = "gps_points")
data class GpsPointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float?,
    val recordedAt: Long,      // epoch millis (UTC)
    val synced: Boolean = false,
)
