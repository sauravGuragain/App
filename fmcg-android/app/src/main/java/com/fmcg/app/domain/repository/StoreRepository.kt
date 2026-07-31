package com.fmcg.app.domain.repository

import com.fmcg.app.domain.model.Store
import com.fmcg.app.domain.model.StoreDraft
import com.fmcg.app.util.Resource
import kotlinx.coroutines.flow.Flow

interface StoreRepository {
    /** Offline-first: emits from the local cache, filtered by [query]. */
    fun observeStores(query: String): Flow<List<Store>>
    /** Pull the latest stores from the backend into the cache. */
    suspend fun refresh(): Resource<Unit>
    suspend fun getStore(id: Int): Resource<Store>
    suspend fun createStore(draft: StoreDraft): Resource<Store>
    suspend fun updateStore(id: Int, draft: StoreDraft): Resource<Store>
    suspend fun deleteStore(id: Int): Resource<Unit>
}
