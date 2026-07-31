package com.fmcg.app.domain.model

data class Product(
    val id: Int,
    val name: String,
    val sku: String,
    val unit: String,
    val defaultPrice: String,
    val isActive: Boolean,
)
