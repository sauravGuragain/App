package com.fmcg.app.data.mapper

import com.fmcg.app.data.remote.dto.UserDto
import com.fmcg.app.domain.model.User
import com.fmcg.app.domain.model.UserRole

fun UserDto.toDomain(): User = User(
    id = id,
    email = email,
    fullName = fullName,
    role = UserRole.fromApi(role),
    phone = phone,
    isActive = isActive,
)
