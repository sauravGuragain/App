package com.fmcg.app.data.mapper

import com.fmcg.app.data.local.entity.StoreEntity
import com.fmcg.app.data.remote.dto.StoreCreateDto
import com.fmcg.app.data.remote.dto.StoreDto
import com.fmcg.app.data.remote.dto.StoreUpdateDto
import com.fmcg.app.domain.model.Store
import com.fmcg.app.domain.model.StoreDraft

fun StoreDto.toEntity() = StoreEntity(
    id = id, name = name, ownerName = ownerName, phone = phone, address = address,
    latitude = latitude, longitude = longitude, notes = notes, photoUrl = photoUrl,
)

fun StoreEntity.toDomain() = Store(
    id = id, name = name, ownerName = ownerName, phone = phone, address = address,
    latitude = latitude, longitude = longitude, notes = notes, photoUrl = photoUrl,
)

fun StoreDto.toDomain() = toEntity().toDomain()

fun StoreDraft.toCreateDto() = StoreCreateDto(
    name = name, ownerName = ownerName, phone = phone, address = address,
    latitude = latitude, longitude = longitude, notes = notes,
)

fun StoreDraft.toUpdateDto() = StoreUpdateDto(
    name = name, ownerName = ownerName, phone = phone, address = address,
    latitude = latitude, longitude = longitude, notes = notes,
)

fun StoreDraft.toEntity(id: Int) = StoreEntity(
    id = id, name = name, ownerName = ownerName, phone = phone, address = address,
    latitude = latitude, longitude = longitude, notes = notes, photoUrl = null,
)
