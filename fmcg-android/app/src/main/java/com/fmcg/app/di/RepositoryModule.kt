package com.fmcg.app.di

import com.fmcg.app.data.repository.AuthRepositoryImpl
import com.fmcg.app.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindGpsRepository(
        impl: com.fmcg.app.data.repository.GpsRepositoryImpl,
    ): com.fmcg.app.domain.repository.GpsRepository

    @Binds
    @Singleton
    abstract fun bindStoreRepository(
        impl: com.fmcg.app.data.repository.StoreRepositoryImpl,
    ): com.fmcg.app.domain.repository.StoreRepository

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        impl: com.fmcg.app.data.repository.ProductRepositoryImpl,
    ): com.fmcg.app.domain.repository.ProductRepository

    @Binds
    @Singleton
    abstract fun bindOrderRepository(
        impl: com.fmcg.app.data.repository.OrderRepositoryImpl,
    ): com.fmcg.app.domain.repository.OrderRepository

    @Binds
    @Singleton
    abstract fun bindDeliveryRepository(
        impl: com.fmcg.app.data.repository.DeliveryRepositoryImpl,
    ): com.fmcg.app.domain.repository.DeliveryRepository

    @Binds
    @Singleton
    abstract fun bindReportRepository(
        impl: com.fmcg.app.data.repository.ReportRepositoryImpl,
    ): com.fmcg.app.domain.repository.ReportRepository

    @Binds
    @Singleton
    abstract fun bindPushRepository(
        impl: com.fmcg.app.data.repository.PushRepositoryImpl,
    ): com.fmcg.app.domain.repository.PushRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: com.fmcg.app.data.repository.UserRepositoryImpl,
    ): com.fmcg.app.domain.repository.UserRepository
}
