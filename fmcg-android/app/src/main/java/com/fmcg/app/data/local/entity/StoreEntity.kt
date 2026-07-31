package com.fmcg.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stores")
data class StoreEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val ownerName: String?,
    val phone: String?,
    val address: String?,
    val latitude: Double,
    val longitude: Double,
    val notes: String?,
    val photoUrl: String?,
)
