package com.fmcg.app.data.sync

import com.fmcg.app.data.local.dao.OutboxDao
import com.fmcg.app.data.local.entity.OutboxEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OutboxManager @Inject constructor(private val dao: OutboxDao) {

    suspend fun enqueue(type: OutboxType, payload: String, targetId: Int?) {
        dao.insert(
            OutboxEntity(
                type = type.name,
                payload = payload,
                targetId = targetId,
                createdAt = System.currentTimeMillis(),
            )
        )
    }

    fun pendingCount(): Flow<Int> = dao.pendingCountFlow(MAX_SYNC_ATTEMPTS)
}
