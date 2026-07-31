package com.fmcg.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.fmcg.app.data.local.entity.OutboxEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OutboxDao {
    @Insert
    suspend fun insert(item: OutboxEntity)

    @Query("SELECT * FROM outbox WHERE attempts < :max ORDER BY id ASC")
    suspend fun getPending(max: Int): List<OutboxEntity>

    @Query("DELETE FROM outbox WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("UPDATE outbox SET attempts = attempts + 1, lastError = :error WHERE id = :id")
    suspend fun bumpAttempt(id: Long, error: String)

    @Query("UPDATE outbox SET attempts = :max, lastError = :error WHERE id = :id")
    suspend fun markDead(id: Long, error: String, max: Int)

    @Query("SELECT COUNT(*) FROM outbox WHERE attempts < :max")
    fun pendingCountFlow(max: Int): Flow<Int>
}
