package com.fmcg.app.domain.model

enum class UserRole {
    ADMIN, MARKETING, DELIVERY, UNKNOWN;

    companion object {
        fun fromApi(value: String): UserRole = when (value.lowercase()) {
            "admin" -> ADMIN
            "marketing" -> MARKETING
            "delivery" -> DELIVERY
            else -> UNKNOWN
        }
    }
}
