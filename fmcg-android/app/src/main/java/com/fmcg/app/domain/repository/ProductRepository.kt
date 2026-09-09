package com.fmcg.app.domain.repository

import com.fmcg.app.domain.model.Product
import com.fmcg.app.domain.model.ProductDraft
import com.fmcg.app.util.Resource
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    /** Active products only — what order entry offers. */
    fun observeProducts(): Flow<List<Product>>

    /** Everything, including deactivated items — for the management screen. */
    fun observeAllProducts(): Flow<List<Product>>

    suspend fun refresh(): Resource<Unit>
    suspend fun createProduct(draft: ProductDraft): Resource<Product>
    suspend fun updateProduct(
        id: Int,
        name: String? = null,
        unit: String? = null,
        defaultPrice: String? = null,
        isActive: Boolean? = null,
    ): Resource<Product>
}
