package com.fmcg.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.fmcg.app.data.local.entity.GpsPointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GpsDao {
    @Insert
    suspend fun insert(point: GpsPointEntity)

    @Query("SELECT * FROM gps_points WHERE synced = 0 ORDER BY recordedAt ASC LIMIT :limit")
    suspend fun getUnsynced(limit: Int = 500): List<GpsPointEntity>

    @Query("DELETE FROM gps_points WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM gps_points WHERE synced = 0")
    fun unsyncedCountFlow(): Flow<Int>
}
