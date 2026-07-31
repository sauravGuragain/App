"""Product catalogue schemas.

`default_price` is the list price; the actual price charged is set per order
line by the marketing rep (customer-specific pricing), so it lives on the
order item, not here.
"""
from datetime import datetime
from decimal import Decimal

from pydantic import BaseModel, Field

from app.schemas.common import ORMModel


class ProductBase(BaseModel):
    name: str = Field(min_length=1, max_length=160)
    sku: str = Field(min_length=1, max_length=64)
    unit: str = Field(default="pcs", max_length=20)
    default_price: Decimal = Field(gt=0, max_digits=12, decimal_places=2)
    is_active: bool = True


class ProductCreate(ProductBase):
    pass


class ProductUpdate(BaseModel):
    name: str | None = Field(default=None, min_length=1, max_length=160)
    unit: str | None = Field(default=None, max_length=20)
    default_price: Decimal | None = Field(
        default=None, gt=0, max_digits=12, decimal_places=2
    )
    is_active: bool | None = None


class ProductOut(ORMModel, ProductBase):
    id: int
    created_at: datetime
