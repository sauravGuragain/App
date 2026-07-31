"""Report response schemas (Phase 12)."""
from decimal import Decimal

from pydantic import BaseModel


class SalesByProduct(BaseModel):
    product_id: int
    name: str
    quantity: int
    total: Decimal


class SalesByStore(BaseModel):
    store_id: int
    name: str
    order_count: int
    total: Decimal


class DeliverySummary(BaseModel):
    pending: int = 0
    out_for_delivery: int = 0
    delivered: int = 0
    failed: int = 0


class DistanceByRep(BaseModel):
    user_id: int
    full_name: str
    distance_km: float


class NewStoresCount(BaseModel):
    count: int
