package com.fmcg.app.domain.model

data class OrderItem(
    val productId: Int,
    val quantity: Int,
    val unitPrice: String,
    val discount: String,
    val lineTotal: String,
)

data class Order(
    val id: Int,
    val storeId: Int,
    val status: OrderStatus,
    val items: List<OrderItem>,
    val subtotal: String,
    val totalDiscount: String,
    val total: String,
    val notes: String?,
    val createdAt: String,
)

data class OrderLineDraft(
    val productId: Int,
    val quantity: Int,
    val unitPrice: String,
    val discount: String,
)

data class OrderDraft(
    val storeId: Int,
    val items: List<OrderLineDraft>,
    val notes: String?,
)
