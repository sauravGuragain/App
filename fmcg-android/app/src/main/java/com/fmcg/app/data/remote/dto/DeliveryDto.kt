package com.fmcg.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeliveryDto(
    val id: Int,
    @SerialName("order_id") val orderId: Int,
    @SerialName("driver_id") val driverId: Int,
    val status: String,
    val notes: String? = null,
    @SerialName("proof_photo_url") val proofPhotoUrl: String? = null,
    @SerialName("assigned_at") val assignedAt: String,
    @SerialName("delivered_at") val deliveredAt: String? = null,
)

@Serializable
data class DeliveryPageDto(
    val items: List<DeliveryDto>,
    val total: Int,
    val skip: Int,
    val limit: Int,
)

@Serializable
data class DeliveryStatusUpdateDto(
    val status: String,
    val notes: String? = null,
    @SerialName("proof_photo_url") val proofPhotoUrl: String? = null,
)

@Serializable
data class MediaUrlDto(
    val url: String,
)
