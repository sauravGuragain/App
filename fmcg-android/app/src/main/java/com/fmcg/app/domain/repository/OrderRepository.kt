package com.fmcg.app.domain.repository

import com.fmcg.app.domain.model.Order
import com.fmcg.app.domain.model.OrderDraft
import com.fmcg.app.domain.model.OrderStatus
import com.fmcg.app.util.Resource

interface OrderRepository {
    suspend fun listOrders(storeId: Int? = null): Resource<List<Order>>
    suspend fun getOrder(id: Int): Resource<Order>
    suspend fun createOrder(draft: OrderDraft): Resource<Boolean>  // true = queued offline
    suspend fun updateStatus(id: Int, status: OrderStatus): Resource<Order>
}
