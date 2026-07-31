package com.fmcg.app.di

import com.fmcg.app.BuildConfig
import com.fmcg.app.data.remote.api.AuthApi
import com.fmcg.app.data.remote.interceptor.AuthInterceptor
import com.fmcg.app.data.remote.interceptor.TokenAuthenticator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val API_PATH = "api/v1/"

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideLogging(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                else HttpLoggingInterceptor.Level.NONE
    }

    /** Bare client for token refresh only — no auth interceptor/authenticator,
     *  so a refresh call can never recurse into [TokenAuthenticator]. */
    @Provides
    @Singleton
    @Named("refresh")
    fun provideRefreshApi(json: Json, logging: HttpLoggingInterceptor): AuthApi {
        val client = OkHttpClient.Builder().addInterceptor(logging).build()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL + API_PATH)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        authenticator: TokenAuthenticator,
        logging: HttpLoggingInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .authenticator(authenticator)
        .addInterceptor(logging)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL + API_PATH)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create()

    @Provides
    @Singleton
    fun provideGpsApi(retrofit: Retrofit): com.fmcg.app.data.remote.api.GpsApi =
        retrofit.create()

    @Provides
    @Singleton
    fun provideStoreApi(retrofit: Retrofit): com.fmcg.app.data.remote.api.StoreApi =
        retrofit.create()

    @Provides
    @Singleton
    fun provideProductApi(retrofit: Retrofit): com.fmcg.app.data.remote.api.ProductApi =
        retrofit.create()

    @Provides
    @Singleton
    fun provideOrderApi(retrofit: Retrofit): com.fmcg.app.data.remote.api.OrderApi =
        retrofit.create()

    @Provides
    @Singleton
    fun provideDeliveryApi(retrofit: Retrofit): com.fmcg.app.data.remote.api.DeliveryApi =
        retrofit.create()

    @Provides
    @Singleton
    fun provideMediaApi(retrofit: Retrofit): com.fmcg.app.data.remote.api.MediaApi =
        retrofit.create()

    @Provides
    @Singleton
    fun provideReportApi(retrofit: Retrofit): com.fmcg.app.data.remote.api.ReportApi =
        retrofit.create()

    @Provides
    @Singleton
    fun provideNotificationApi(retrofit: Retrofit): com.fmcg.app.data.remote.api.NotificationApi =
        retrofit.create()
}
