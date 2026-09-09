package com.fmcg.app.data.repository

import com.fmcg.app.data.local.dao.ProductDao
import com.fmcg.app.data.mapper.toDomain
import com.fmcg.app.data.mapper.toEntity
import com.fmcg.app.data.remote.api.ProductApi
import com.fmcg.app.data.remote.dto.ProductCreateDto
import com.fmcg.app.data.remote.dto.ProductUpdateDto
import com.fmcg.app.domain.model.Product
import com.fmcg.app.domain.model.ProductDraft
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

    override fun observeAllProducts(): Flow<List<Product>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun refresh(): Resource<Unit> = withContext(io) {
        try {
            // activeOnly = false so deactivated products still reach the
            // management screen; order entry filters them out separately.
            dao.upsertAll(api.list(activeOnly = false).items.map { it.toEntity() })
            Resource.Success(Unit)
        } catch (e: HttpException) {
            Resource.Error("Could not refresh products (${e.code()})", e.code())
        } catch (e: IOException) {
            Resource.Error("Offline — showing cached products")
        }
    }

    override suspend fun createProduct(draft: ProductDraft): Resource<Product> = withContext(io) {
        try {
            val dto = api.create(
                ProductCreateDto(
                    name = draft.name.trim(),
                    sku = draft.sku.trim(),
                    unit = draft.unit.trim().ifEmpty { "pcs" },
                    defaultPrice = draft.defaultPrice.trim(),
                )
            )
            dao.upsertAll(listOf(dto.toEntity()))
            Resource.Success(dto.toEntity().toDomain())
        } catch (e: HttpException) {
            Resource.Error(writeError(e), e.code())
        } catch (e: IOException) {
            Resource.Error("Network error — check your connection")
        }
    }

    override suspend fun updateProduct(
        id: Int,
        name: String?,
        unit: String?,
        defaultPrice: String?,
        isActive: Boolean?,
    ): Resource<Product> = withContext(io) {
        try {
            val dto = api.update(
                id,
                ProductUpdateDto(
                    name = name?.trim(),
                    unit = unit?.trim(),
                    defaultPrice = defaultPrice?.trim(),
                    isActive = isActive,
                )
            )
            dao.upsertAll(listOf(dto.toEntity()))
            Resource.Success(dto.toEntity().toDomain())
        } catch (e: HttpException) {
            Resource.Error(writeError(e), e.code())
        } catch (e: IOException) {
            Resource.Error("Network error — check your connection")
        }
    }

    private fun writeError(e: HttpException) = when (e.code()) {
        409 -> "That SKU already exists"
        422 -> "Check the details — the server rejected them"
        403 -> "You don't have permission to change products"
        404 -> "Product not found"
        else -> "Request failed (${e.code()})"
    }
}
