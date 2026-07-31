package com.fmcg.app.data.remote.api

import com.fmcg.app.data.remote.dto.DeliverySummaryDto
import com.fmcg.app.data.remote.dto.DistanceByRepDto
import com.fmcg.app.data.remote.dto.NewStoresCountDto
import com.fmcg.app.data.remote.dto.SalesByProductDto
import com.fmcg.app.data.remote.dto.SalesByRepDto
import com.fmcg.app.data.remote.dto.SalesByStoreDto
import retrofit2.http.GET

interface ReportApi {
    @GET("reports/sales-by-rep")
    suspend fun salesByRep(): List<SalesByRepDto>

    @GET("reports/sales-by-product")
    suspend fun salesByProduct(): List<SalesByProductDto>

    @GET("reports/sales-by-store")
    suspend fun salesByStore(): List<SalesByStoreDto>

    @GET("reports/delivery-summary")
    suspend fun deliverySummary(): DeliverySummaryDto

    @GET("reports/distance-by-rep")
    suspend fun distanceByRep(): List<DistanceByRepDto>

    @GET("reports/new-stores")
    suspend fun newStores(): NewStoresCountDto
}
