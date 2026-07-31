package com.fmcg.app.presentation.navigation

import com.fmcg.app.domain.model.UserRole

/** Top-level routes. Feature sub-graphs are nested under each home in later phases. */
object Destinations {
    const val LOGIN = "login"
    const val MARKETING_HOME = "marketing_home"
    const val DELIVERY_HOME = "delivery_home"
    const val ADMIN_HOME = "admin_home"

    fun homeFor(role: UserRole): String = when (role) {
        UserRole.ADMIN -> ADMIN_HOME
        UserRole.MARKETING -> MARKETING_HOME
        UserRole.DELIVERY -> DELIVERY_HOME
    }
}
