package com.fmcg.app.domain.repository

import com.fmcg.app.domain.model.Product
import com.fmcg.app.util.Resource
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun observeProducts(): Flow<List<Product>>
    suspend fun refresh(): Resource<Unit>
}
