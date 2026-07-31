package com.fmcg.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.fmcg.app.data.local.entity.StoreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDao {
    @Upsert
    suspend fun upsertAll(stores: List<StoreEntity>)

    @Upsert
    suspend fun upsert(store: StoreEntity)

    @Query("SELECT * FROM stores WHERE name LIKE '%' || :query || '%' ORDER BY name")
    fun observe(query: String): Flow<List<StoreEntity>>

    @Query("SELECT * FROM stores WHERE id = :id")
    suspend fun getById(id: Int): StoreEntity?

    @Query("DELETE FROM stores WHERE id = :id")
    suspend fun deleteById(id: Int)
}
