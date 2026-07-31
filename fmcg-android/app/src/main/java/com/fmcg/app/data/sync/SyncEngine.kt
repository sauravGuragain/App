package com.fmcg.app.data.sync

import com.fmcg.app.data.local.dao.OutboxDao
import com.fmcg.app.data.local.dao.StoreDao
import com.fmcg.app.data.local.entity.OutboxEntity
import com.fmcg.app.data.mapper.toEntity
import com.fmcg.app.data.remote.api.OrderApi
import com.fmcg.app.data.remote.api.StoreApi
import com.fmcg.app.data.remote.dto.OrderCreateDto
import com.fmcg.app.data.remote.dto.StoreCreateDto
import com.fmcg.app.data.remote.dto.StoreUpdateDto
import com.fmcg.app.domain.repository.GpsRepository
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Drains the outbox in order. Conflict policy:
 *  - updates are last-write-wins (we send the local state, server accepts);
 *  - a delete of an already-deleted row (404) is treated as success (idempotent);
 *  - permanent 4xx (validation/forbidden/conflict) parks the row with its error
 *    rather than retrying forever;
 *  - transient (network / 5xx / 408 / 429) increments the attempt and asks the
 *    Worker to retry later.
 */
@Singleton
class SyncEngine @Inject constructor(
    private val outboxDao: OutboxDao,
    private val storeDao: StoreDao,
    private val storeApi: StoreApi,
    private val orderApi: OrderApi,
    private val gpsRepository: GpsRepository,
    private val json: Json,
) {
    /** @return true if some item still needs a retry. */
    suspend fun drain(): Boolean {
        var needsRetry = false
        for (item in outboxDao.getPending(MAX_SYNC_ATTEMPTS)) {
            try {
                process(item)
                outboxDao.delete(item.id)
            } catch (e: HttpException) {
                if (isPermanent(e.code())) {
                    outboxDao.markDead(item.id, "HTTP ${e.code()}", MAX_SYNC_ATTEMPTS)
                } else {
                    outboxDao.bumpAttempt(item.id, "HTTP ${e.code()}")
                    needsRetry = true
                }
            } catch (e: IOException) {
                outboxDao.bumpAttempt(item.id, "offline")
                needsRetry = true
            }
        }
        // Opportunistically flush buffered GPS points too.
        runCatching { gpsRepository.syncPending() }
        return needsRetry
    }

    private suspend fun process(item: OutboxEntity) {
        when (OutboxType.valueOf(item.type)) {
            OutboxType.STORE_CREATE -> {
                val dto = json.decodeFromString<StoreCreateDto>(item.payload)
                val created = storeApi.create(dto)
                // Reconcile the temp local row with the server-assigned id.
                item.targetId?.let { storeDao.deleteById(it) }
                storeDao.upsert(created.toEntity())
            }
            OutboxType.STORE_UPDATE -> {
                val dto = json.decodeFromString<StoreUpdateDto>(item.payload)
                storeApi.update(item.targetId!!, dto)
            }
            OutboxType.STORE_DELETE -> {
                try {
                    storeApi.delete(item.targetId!!)
                } catch (e: HttpException) {
                    if (e.code() != 404) throw e   // already gone == done
                }
            }
            OutboxType.ORDER_CREATE -> {
                val dto = json.decodeFromString<OrderCreateDto>(item.payload)
                orderApi.create(dto)
            }
        }
    }

    private fun isPermanent(code: Int): Boolean =
        code in 400..499 && code != 408 && code != 429
}
