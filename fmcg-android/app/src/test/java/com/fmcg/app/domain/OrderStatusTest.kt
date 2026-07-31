package com.fmcg.app.domain

import com.fmcg.app.domain.model.OrderStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderStatusTest {

    @Test
    fun fromApi_maps_known_value() {
        assertEquals(OrderStatus.DELIVERED, OrderStatus.fromApi("delivered"))
        assertEquals(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.fromApi("out_for_delivery"))
    }

    @Test
    fun fromApi_unknown_falls_back() {
        assertEquals(OrderStatus.UNKNOWN, OrderStatus.fromApi("nonsense"))
    }

    @Test
    fun pending_can_move_to_approved_or_cancelled() {
        val next = OrderStatus.PENDING.allowedNext()
        assertTrue(next.contains(OrderStatus.APPROVED))
        assertTrue(next.contains(OrderStatus.CANCELLED))
    }

    @Test
    fun terminal_states_have_no_transitions() {
        assertTrue(OrderStatus.CANCELLED.allowedNext().isEmpty())
        assertTrue(OrderStatus.RETURNED.allowedNext().isEmpty())
    }
}
