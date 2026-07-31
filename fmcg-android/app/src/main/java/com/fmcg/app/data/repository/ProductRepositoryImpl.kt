package com.fmcg.app.data.repository

import com.fmcg.app.data.local.dao.ProductDao
import com.fmcg.app.data.mapper.toDomain
import com.fmcg.app.data.mapper.toEntity
import com.fmcg.app.data.remote.api.ProductApi
import com.fmcg.app.domain.model.Product
import com.fmcg.app.domain.repository.ProductRepository
import com.fmcg.app.util.Resource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val api: ProductApi,
    private val dao: ProductDao,
    private val io: CoroutineDispatcher,
) : ProductRepository {

    override fun observeProducts(): Flow<List<Product>> =
        dao.observeActive().map { list -> list.map { it.toDomain() } }

    override suspend fun refresh(): Resource<Unit> = withContext(io) {
        try {
            dao.upsertAll(api.list().items.map { it.toEntity() })
            Resource.Success(Unit)
        } catch (e: HttpException) {
            Resource.Error("Could not refresh products (${e.code()})", e.code())
        } catch (e: IOException) {
            Resource.Error("Offline — showing cached products")
        }
    }
}
