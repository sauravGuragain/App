package com.fmcg.app.domain.model

data class Store(
    val id: Int,
    val name: String,
    val ownerName: String?,
    val phone: String?,
    val address: String?,
    val latitude: Double,
    val longitude: Double,
    val notes: String?,
    val photoUrl: String?,
)

/** Fields a rep supplies when creating or editing a store. */
data class StoreDraft(
    val name: String,
    val ownerName: String?,
    val phone: String?,
    val address: String?,
    val latitude: Double,
    val longitude: Double,
    val notes: String?,
)
