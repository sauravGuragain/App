package com.fmcg.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A pending mutation captured while offline (or on a failed write), drained
 *  FIFO by the sync engine. `attempts` caps retries; a row is considered dead
 *  once attempts reaches the max. */
@Entity(tableName = "outbox")
data class OutboxEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,          // OutboxType.name
    val payload: String,       // JSON of the request DTO
    val targetId: Int?,        // server id (update/delete) or temp local id (create)
    val createdAt: Long,
    val attempts: Int = 0,
    val lastError: String? = null,
)
