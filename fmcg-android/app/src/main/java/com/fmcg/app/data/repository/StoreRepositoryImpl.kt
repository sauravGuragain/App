package com.fmcg.app.data.repository

import com.fmcg.app.data.local.dao.StoreDao
import com.fmcg.app.data.mapper.toCreateDto
import com.fmcg.app.data.mapper.toDomain
import com.fmcg.app.data.mapper.toEntity
import com.fmcg.app.data.mapper.toUpdateDto
import com.fmcg.app.data.remote.api.StoreApi
import com.fmcg.app.data.sync.OutboxManager
import com.fmcg.app.data.sync.OutboxType
import com.fmcg.app.data.sync.SyncScheduler
import com.fmcg.app.domain.model.Store
import com.fmcg.app.domain.model.StoreDraft
import com.fmcg.app.domain.repository.StoreRepository
import com.fmcg.app.util.Resource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * Network-first with an offline outbox fallback. Online writes get real ids
 * immediately; offline writes are applied to the local cache optimistically
 * (creates use a temp negative id) and queued for the sync engine.
 */
class StoreRepositoryImpl @Inject constructor(
    private val api: StoreApi,
    private val dao: StoreDao,
    private val outbox: OutboxManager,
    private val scheduler: SyncScheduler,
    private val json: Json,
    private val io: CoroutineDispatcher,
) : StoreRepository {

    override fun observeStores(query: String): Flow<List<Store>> =
        dao.observe(query).map { list -> list.map { it.toDomain() } }

    override suspend fun refresh(): Resource<Unit> = withContext(io) {
        try {
            dao.upsertAll(api.list().items.map { it.toEntity() })
            Resource.Success(Unit)
        } catch (e: HttpException) {
            Resource.Error("Could not refresh stores (${e.code()})", e.code())
        } catch (e: IOException) {
            Resource.Error("Offline — showing cached stores")
        }
    }

    override suspend fun getStore(id: Int): Resource<Store> = withContext(io) {
        dao.getById(id)?.let { return@withContext Resource.Success(it.toDomain()) }
        try {
            val dto = api.get(id)
            dao.upsert(dto.toEntity())
            Resource.Success(dto.toDomain())
        } catch (e: HttpException) {
            Resource.Error("Store not found (${e.code()})", e.code())
        } catch (e: IOException) {
            Resource.Error("Offline — store not cached")
        }
    }

    override suspend fun createStore(draft: StoreDraft): Resource<Store> = withContext(io) {
        try {
            val dto = api.create(draft.toCreateDto())
            dao.upsert(dto.toEntity())
            Resource.Success(dto.toDomain())
        } catch (e: HttpException) {
            Resource.Error("Could not create store (${e.code()})", e.code())
        } catch (e: IOException) {
            val tempId = tempId()
            val entity = draft.toEntity(tempId)
            dao.upsert(entity)
            outbox.enqueue(OutboxType.STORE_CREATE, json.encodeToString(draft.toCreateDto()), tempId)
            scheduler.requestSync()
            Resource.Success(entity.toDomain())
        }
    }

    override suspend fun updateStore(id: Int, draft: StoreDraft): Resource<Store> =
        withContext(io) {
            try {
                val dto = api.update(id, draft.toUpdateDto())
                dao.upsert(dto.toEntity())
                Resource.Success(dto.toDomain())
            } catch (e: HttpException) {
                Resource.Error("Could not update store (${e.code()})", e.code())
            } catch (e: IOException) {
                val entity = draft.toEntity(id)
                dao.upsert(entity)
                outbox.enqueue(OutboxType.STORE_UPDATE, json.encodeToString(draft.toUpdateDto()), id)
                scheduler.requestSync()
                Resource.Success(entity.toDomain())
            }
        }

    override suspend fun deleteStore(id: Int): Resource<Unit> = withContext(io) {
        try {
            api.delete(id)
            dao.deleteById(id)
            Resource.Success(Unit)
        } catch (e: HttpException) {
            val msg = if (e.code() == 403) "Only admins can delete stores"
                      else "Could not delete store (${e.code()})"
            Resource.Error(msg, e.code())
        } catch (e: IOException) {
            dao.deleteById(id)
            outbox.enqueue(OutboxType.STORE_DELETE, "", id)
            scheduler.requestSync()
            Resource.Success(Unit)
        }
    }

    // Negative, ~unique id for an offline-created store until the server assigns one.
    private fun tempId(): Int =
        -((System.currentTimeMillis() % Int.MAX_VALUE).toInt().coerceAtLeast(1))
}
