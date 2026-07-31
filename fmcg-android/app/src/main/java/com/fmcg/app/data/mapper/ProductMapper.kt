package com.fmcg.app.data.mapper

import com.fmcg.app.data.local.entity.ProductEntity
import com.fmcg.app.data.remote.dto.ProductDto
import com.fmcg.app.domain.model.Product

fun ProductDto.toEntity() = ProductEntity(id, name, sku, unit, defaultPrice, isActive)
fun ProductEntity.toDomain() = Product(id, name, sku, unit, defaultPrice, isActive)
