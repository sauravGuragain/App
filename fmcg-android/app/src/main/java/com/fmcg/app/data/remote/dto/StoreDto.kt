package com.fmcg.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StoreDto(
    val id: Int,
    val name: String,
    @SerialName("owner_name") val ownerName: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val latitude: Double,
    val longitude: Double,
    val notes: String? = null,
    @SerialName("photo_url") val photoUrl: String? = null,
)

@Serializable
data class StorePageDto(
    val items: List<StoreDto>,
    val total: Int,
    val skip: Int,
    val limit: Int,
)

@Serializable
data class StoreCreateDto(
    val name: String,
    @SerialName("owner_name") val ownerName: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val latitude: Double,
    val longitude: Double,
    val notes: String? = null,
)

@Serializable
data class StoreUpdateDto(
    val name: String? = null,
    @SerialName("owner_name") val ownerName: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val notes: String? = null,
)
