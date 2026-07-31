package com.fmcg.app.domain

import com.fmcg.app.domain.model.DeliveryStatus
import com.fmcg.app.domain.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Test

class EnumMappingTest {

    @Test
    fun userRole_fromApi() {
        assertEquals(UserRole.ADMIN, UserRole.fromApi("admin"))
        assertEquals(UserRole.MARKETING, UserRole.fromApi("marketing"))
        assertEquals(UserRole.DELIVERY, UserRole.fromApi("delivery"))
        assertEquals(UserRole.UNKNOWN, UserRole.fromApi("ceo"))
    }

    @Test
    fun deliveryStatus_fromApi() {
        assertEquals(DeliveryStatus.DELIVERED, DeliveryStatus.fromApi("delivered"))
        assertEquals(DeliveryStatus.UNKNOWN, DeliveryStatus.fromApi("teleported"))
    }
}
