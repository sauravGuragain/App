package com.fmcg.app.data.mapper

import com.fmcg.app.data.remote.dto.DeliveryDto
import com.fmcg.app.domain.model.Delivery
import com.fmcg.app.domain.model.DeliveryStatus

fun DeliveryDto.toDomain() = Delivery(
    id = id,
    orderId = orderId,
    driverId = driverId,
    status = DeliveryStatus.fromApi(status),
    notes = notes,
    proofPhotoUrl = proofPhotoUrl,
    assignedAt = assignedAt,
    deliveredAt = deliveredAt,
)
