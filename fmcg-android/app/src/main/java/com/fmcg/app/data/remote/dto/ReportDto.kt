package com.fmcg.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SalesByRepDto(
    @SerialName("user_id") val userId: Int,
    @SerialName("full_name") val fullName: String,
    @SerialName("order_count") val orderCount: Int,
    @SerialName("total_sales") val totalSales: String,
)

@Serializable
data class SalesByProductDto(
    @SerialName("product_id") val productId: Int,
    val name: String,
    val quantity: Int,
    val total: String,
)

@Serializable
data class SalesByStoreDto(
    @SerialName("store_id") val storeId: Int,
    val name: String,
    @SerialName("order_count") val orderCount: Int,
    val total: String,
)

@Serializable
data class DeliverySummaryDto(
    val pending: Int = 0,
    @SerialName("out_for_delivery") val outForDelivery: Int = 0,
    val delivered: Int = 0,
    val failed: Int = 0,
)

@Serializable
data class DistanceByRepDto(
    @SerialName("user_id") val userId: Int,
    @SerialName("full_name") val fullName: String,
    @SerialName("distance_km") val distanceKm: Double,
)

@Serializable
data class NewStoresCountDto(
    val count: Int,
)
