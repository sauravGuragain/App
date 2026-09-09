package com.fmcg.app.domain.model

/** What an admin or marketing rep fills in to add a product. */
data class ProductDraft(
    val name: String,
    val sku: String,
    val unit: String,
    val defaultPrice: String,
)
