package com.fmcg.app.di

import android.content.Context
import androidx.room.Room
import com.fmcg.app.data.local.FmcgDatabase
import com.fmcg.app.data.local.dao.GpsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FmcgDatabase =
        Room.databaseBuilder(context, FmcgDatabase::class.java, "fmcg.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideGpsDao(db: FmcgDatabase): GpsDao = db.gpsDao()

    @Provides
    fun provideStoreDao(db: FmcgDatabase): com.fmcg.app.data.local.dao.StoreDao =
        db.storeDao()

    @Provides
    fun provideProductDao(db: FmcgDatabase): com.fmcg.app.data.local.dao.ProductDao =
        db.productDao()

    @Provides
    fun provideOutboxDao(db: FmcgDatabase): com.fmcg.app.data.local.dao.OutboxDao =
        db.outboxDao()
}
