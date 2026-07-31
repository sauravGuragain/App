package com.fmcg.app.domain.model

enum class OrderStatus(val api: String, val label: String) {
    DRAFT("draft", "Draft"),
    PENDING("pending", "Pending"),
    APPROVED("approved", "Approved"),
    ASSIGNED("assigned", "Assigned"),
    OUT_FOR_DELIVERY("out_for_delivery", "Out for delivery"),
    DELIVERED("delivered", "Delivered"),
    CANCELLED("cancelled", "Cancelled"),
    RETURNED("returned", "Returned"),
    UNKNOWN("unknown", "Unknown");

    /** Admin-driven transitions, mirroring the backend state machine. */
    fun allowedNext(): List<OrderStatus> = when (this) {
        DRAFT -> listOf(PENDING, CANCELLED)
        PENDING -> listOf(APPROVED, CANCELLED)
        APPROVED -> listOf(ASSIGNED, CANCELLED)
        ASSIGNED -> listOf(OUT_FOR_DELIVERY, CANCELLED)
        OUT_FOR_DELIVERY -> listOf(DELIVERED, RETURNED)
        DELIVERED -> listOf(RETURNED)
        else -> emptyList()
    }

    companion object {
        fun fromApi(value: String): OrderStatus =
            entries.firstOrNull { it.api == value } ?: UNKNOWN
    }
}
