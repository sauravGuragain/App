package com.fmcg.app.domain.model

enum class DeliveryStatus(val api: String, val label: String) {
    PENDING("pending", "Pending"),
    OUT_FOR_DELIVERY("out_for_delivery", "Out for delivery"),
    DELIVERED("delivered", "Delivered"),
    FAILED("failed", "Failed"),
    UNKNOWN("unknown", "Unknown");

    companion object {
        fun fromApi(value: String): DeliveryStatus =
            entries.firstOrNull { it.api == value } ?: UNKNOWN
    }
}

data class Delivery(
    val id: Int,
    val orderId: Int,
    val driverId: Int,
    val status: DeliveryStatus,
    val notes: String?,
    val proofPhotoUrl: String?,
    val assignedAt: String,
    val deliveredAt: String?,
)
