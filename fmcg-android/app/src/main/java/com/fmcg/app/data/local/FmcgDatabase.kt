package com.fmcg.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fmcg.app.data.local.dao.GpsDao
import com.fmcg.app.data.local.dao.OutboxDao
import com.fmcg.app.data.local.dao.ProductDao
import com.fmcg.app.data.local.dao.StoreDao
import com.fmcg.app.data.local.entity.GpsPointEntity
import com.fmcg.app.data.local.entity.OutboxEntity
import com.fmcg.app.data.local.entity.ProductEntity
import com.fmcg.app.data.local.entity.StoreEntity

@Database(
    entities = [
        GpsPointEntity::class, StoreEntity::class,
        ProductEntity::class, OutboxEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
abstract class FmcgDatabase : RoomDatabase() {
    abstract fun gpsDao(): GpsDao
    abstract fun storeDao(): StoreDao
    abstract fun productDao(): ProductDao
    abstract fun outboxDao(): OutboxDao
}
