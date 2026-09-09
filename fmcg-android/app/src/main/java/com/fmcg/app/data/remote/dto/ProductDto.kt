package com.fmcg.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: Int,
    val name: String,
    val sku: String,
    val unit: String = "pcs",
    @SerialName("default_price") val defaultPrice: String,   // Decimal -> JSON string
    @SerialName("is_active") val isActive: Boolean = true,
)

@Serializable
data class ProductPageDto(
    val items: List<ProductDto>,
    val total: Int,
    val skip: Int,
    val limit: Int,
)

@Serializable
data class ProductCreateDto(
    val name: String,
    val sku: String,
    val unit: String = "pcs",
    @SerialName("default_price") val defaultPrice: String,
    @SerialName("is_active") val isActive: Boolean = true,
)

/** PATCH body — the backend ignores unset fields, and SKU is immutable. */
@Serializable
data class ProductUpdateDto(
    val name: String? = null,
    val unit: String? = null,
    @SerialName("default_price") val defaultPrice: String? = null,
    @SerialName("is_active") val isActive: Boolean? = null,
)
