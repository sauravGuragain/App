"""Business rules for orders.

Two responsibilities that must never live in the endpoint:
  1. Totals are computed here from the line items — the client's numbers are
     ignored, preventing tampered totals.
  2. The status workflow is a state machine; only declared transitions are
     allowed.
"""
from decimal import Decimal

from fastapi import HTTPException, status

from app.schemas.common import OrderStatus
from app.schemas.order import OrderItemCreate

# Allowed status transitions (from -> set of reachable states).
_TRANSITIONS: dict[OrderStatus, set[OrderStatus]] = {
    OrderStatus.DRAFT: {OrderStatus.PENDING, OrderStatus.CANCELLED},
    OrderStatus.PENDING: {OrderStatus.APPROVED, OrderStatus.CANCELLED},
    OrderStatus.APPROVED: {OrderStatus.ASSIGNED, OrderStatus.CANCELLED},
    OrderStatus.ASSIGNED: {OrderStatus.OUT_FOR_DELIVERY, OrderStatus.CANCELLED},
    OrderStatus.OUT_FOR_DELIVERY: {OrderStatus.DELIVERED, OrderStatus.RETURNED},
    OrderStatus.DELIVERED: {OrderStatus.RETURNED},
    OrderStatus.CANCELLED: set(),
    OrderStatus.RETURNED: set(),
}


def compute_line(item: OrderItemCreate) -> Decimal:
    return (item.unit_price * item.quantity) - item.discount


def compute_totals(items: list[OrderItemCreate]) -> tuple[Decimal, Decimal, Decimal]:
    subtotal = sum((i.unit_price * i.quantity for i in items), Decimal("0"))
    total_discount = sum((i.discount for i in items), Decimal("0"))
    total = subtotal - total_discount
    if total < 0:
        raise HTTPException(
            status.HTTP_422_UNPROCESSABLE_ENTITY, "Discounts exceed order subtotal"
        )
    return subtotal, total_discount, total


def assert_transition(current: OrderStatus, target: OrderStatus) -> None:
    if target not in _TRANSITIONS[current]:
        raise HTTPException(
            status.HTTP_409_CONFLICT,
            f"Cannot move order from {current.value} to {target.value}",
        )
