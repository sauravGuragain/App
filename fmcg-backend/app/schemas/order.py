"""Order schemas.

Pricing model: each line carries the *customer-specific* unit price chosen by
the rep plus an optional per-line discount. Totals are computed server-side in
the service layer — never trusted from the client.
"""
from datetime import datetime
from decimal import Decimal

from pydantic import BaseModel, Field

from app.schemas.common import ORMModel, OrderStatus


class OrderItemCreate(BaseModel):
    product_id: int
    quantity: int = Field(gt=0)
    unit_price: Decimal = Field(gt=0, max_digits=12, decimal_places=2)
    discount: Decimal = Field(default=Decimal("0"), ge=0, max_digits=12, decimal_places=2)


class OrderItemOut(ORMModel):
    id: int
    product_id: int
    quantity: int
    unit_price: Decimal
    discount: Decimal
    line_total: Decimal


class OrderCreate(BaseModel):
    store_id: int
    items: list[OrderItemCreate] = Field(min_length=1)
    notes: str | None = Field(default=None, max_length=1000)


class OrderStatusUpdate(BaseModel):
    status: OrderStatus


class OrderOut(ORMModel):
    id: int
    store_id: int
    created_by: int | None = None
    status: OrderStatus
    items: list[OrderItemOut]
    subtotal: Decimal
    total_discount: Decimal
    total: Decimal
    notes: str | None = None
    created_at: datetime
