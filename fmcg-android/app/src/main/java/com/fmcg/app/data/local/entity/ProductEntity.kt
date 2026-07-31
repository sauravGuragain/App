package com.fmcg.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val sku: String,
    val unit: String,
    val defaultPrice: String,   // money kept as string to preserve precision
    val isActive: Boolean,
)
