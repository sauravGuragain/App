package com.fmcg.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderItemDto(
    val id: Int,
    @SerialName("product_id") val productId: Int,
    val quantity: Int,
    @SerialName("unit_price") val unitPrice: String,
    val discount: String,
    @SerialName("line_total") val lineTotal: String,
)

@Serializable
data class OrderDto(
    val id: Int,
    @SerialName("store_id") val storeId: Int,
    @SerialName("created_by") val createdBy: Int? = null,
    val status: String,
    val items: List<OrderItemDto>,
    val subtotal: String,
    @SerialName("total_discount") val totalDiscount: String,
    val total: String,
    val notes: String? = null,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class OrderPageDto(
    val items: List<OrderDto>,
    val total: Int,
    val skip: Int,
    val limit: Int,
)

@Serializable
data class OrderItemCreateDto(
    @SerialName("product_id") val productId: Int,
    val quantity: Int,
    @SerialName("unit_price") val unitPrice: String,
    val discount: String = "0",
)

@Serializable
data class OrderCreateDto(
    @SerialName("store_id") val storeId: Int,
    val items: List<OrderItemCreateDto>,
    val notes: String? = null,
)

@Serializable
data class OrderStatusUpdateDto(
    val status: String,
)
