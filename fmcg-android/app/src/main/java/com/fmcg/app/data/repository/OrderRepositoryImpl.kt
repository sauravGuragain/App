package com.fmcg.app.data.repository

import com.fmcg.app.data.mapper.toCreateDto
import com.fmcg.app.data.mapper.toDomain
import com.fmcg.app.data.remote.api.OrderApi
import com.fmcg.app.data.remote.dto.OrderStatusUpdateDto
import com.fmcg.app.data.sync.OutboxManager
import com.fmcg.app.data.sync.OutboxType
import com.fmcg.app.data.sync.SyncScheduler
import com.fmcg.app.domain.model.Order
import com.fmcg.app.domain.model.OrderDraft
import com.fmcg.app.domain.model.OrderStatus
import com.fmcg.app.domain.repository.OrderRepository
import com.fmcg.app.util.Resource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/** Network-backed; order create falls back to the offline outbox. Offline order
 *  caching for reads is a future enhancement. */
class OrderRepositoryImpl @Inject constructor(
    private val api: OrderApi,
    private val outbox: OutboxManager,
    private val scheduler: SyncScheduler,
    private val json: Json,
    private val io: CoroutineDispatcher,
) : OrderRepository {

    override suspend fun listOrders(storeId: Int?): Resource<List<Order>> = withContext(io) {
        try {
            Resource.Success(api.list(storeId = storeId).items.map { it.toDomain() })
        } catch (e: HttpException) {
            Resource.Error("Could not load orders (${e.code()})", e.code())
        } catch (e: IOException) {
            Resource.Error("Offline — orders unavailable")
        }
    }

    override suspend fun getOrder(id: Int): Resource<Order> = withContext(io) {
        try {
            Resource.Success(api.get(id).toDomain())
        } catch (e: HttpException) {
            Resource.Error("Order not found (${e.code()})", e.code())
        } catch (e: IOException) {
            Resource.Error("Offline — order unavailable")
        }
    }

    override suspend fun createOrder(draft: OrderDraft): Resource<Boolean> = withContext(io) {
        try {
            api.create(draft.toCreateDto())
            Resource.Success(false)   // created online
        } catch (e: HttpException) {
            Resource.Error("Could not create order (${e.code()})", e.code())
        } catch (e: IOException) {
            outbox.enqueue(OutboxType.ORDER_CREATE, json.encodeToString(draft.toCreateDto()), null)
            scheduler.requestSync()
            Resource.Success(true)    // queued offline
        }
    }

    override suspend fun updateStatus(id: Int, status: OrderStatus): Resource<Order> =
        withContext(io) {
            try {
                Resource.Success(api.updateStatus(id, OrderStatusUpdateDto(status.api)).toDomain())
            } catch (e: HttpException) {
                val msg = when (e.code()) {
                    403 -> "Only admins can change order status"
                    409 -> "That status change isn't allowed"
                    else -> "Could not update status (${e.code()})"
                }
                Resource.Error(msg, e.code())
            } catch (e: IOException) {
                Resource.Error("Offline — try again when connected")
            }
        }
}
