package com.fmcg.app.data.mapper

import com.fmcg.app.data.remote.dto.OrderCreateDto
import com.fmcg.app.data.remote.dto.OrderDto
import com.fmcg.app.data.remote.dto.OrderItemCreateDto
import com.fmcg.app.data.remote.dto.OrderItemDto
import com.fmcg.app.domain.model.Order
import com.fmcg.app.domain.model.OrderDraft
import com.fmcg.app.domain.model.OrderItem
import com.fmcg.app.domain.model.OrderStatus

fun OrderItemDto.toDomain() = OrderItem(productId, quantity, unitPrice, discount, lineTotal)

fun OrderDto.toDomain() = Order(
    id = id,
    storeId = storeId,
    status = OrderStatus.fromApi(status),
    items = items.map { it.toDomain() },
    subtotal = subtotal,
    totalDiscount = totalDiscount,
    total = total,
    notes = notes,
    createdAt = createdAt,
)

fun OrderDraft.toCreateDto() = OrderCreateDto(
    storeId = storeId,
    items = items.map { OrderItemCreateDto(it.productId, it.quantity, it.unitPrice, it.discount) },
    notes = notes,
)
