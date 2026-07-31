"""Unit tests for the order pricing + status-workflow rules."""
from decimal import Decimal

import pytest
from fastapi import HTTPException

from app.schemas.common import OrderStatus
from app.schemas.order import OrderItemCreate
from app.services import order_service


def _item(qty, price, discount="0"):
    return OrderItemCreate(product_id=1, quantity=qty, unit_price=Decimal(price),
                           discount=Decimal(discount))


def test_compute_line():
    assert order_service.compute_line(_item(3, "2.50", "1.00")) == Decimal("6.50")


def test_compute_totals():
    subtotal, discount, total = order_service.compute_totals(
        [_item(3, "2.50", "1.00"), _item(2, "1.00")]
    )
    assert subtotal == Decimal("9.50")
    assert discount == Decimal("1.00")
    assert total == Decimal("8.50")


def test_discount_exceeding_subtotal_raises():
    with pytest.raises(HTTPException) as exc:
        order_service.compute_totals([_item(1, "1.00", "5.00")])
    assert exc.value.status_code == 422


def test_legal_transition_ok():
    order_service.assert_transition(OrderStatus.PENDING, OrderStatus.APPROVED)  # no raise


def test_illegal_transition_raises_409():
    with pytest.raises(HTTPException) as exc:
        order_service.assert_transition(OrderStatus.APPROVED, OrderStatus.DELIVERED)
    assert exc.value.status_code == 409


def test_terminal_states_have_no_forward_moves():
    with pytest.raises(HTTPException):
        order_service.assert_transition(OrderStatus.CANCELLED, OrderStatus.PENDING)
