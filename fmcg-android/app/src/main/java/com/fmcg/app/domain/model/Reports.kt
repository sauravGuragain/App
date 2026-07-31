package com.fmcg.app.domain.model

data class SalesRow(val label: String, val secondary: String, val total: String)

data class DeliverySummary(
    val pending: Int,
    val outForDelivery: Int,
    val delivered: Int,
    val failed: Int,
)

data class DistanceRow(val name: String, val distanceKm: Double)

data class ReportsBundle(
    val salesByRep: List<SalesRow>,
    val salesByProduct: List<SalesRow>,
    val salesByStore: List<SalesRow>,
    val deliverySummary: DeliverySummary,
    val distanceByRep: List<DistanceRow>,
    val newStoresCount: Int,
)
