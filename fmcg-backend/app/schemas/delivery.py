"""Delivery, GPS-tracking and report schemas."""
from datetime import datetime
from decimal import Decimal

from pydantic import BaseModel, Field

from app.schemas.common import DeliveryStatus, ORMModel


# ---------- Deliveries ----------
class DeliveryAssign(BaseModel):
    order_id: int
    driver_id: int


class DeliveryStatusUpdate(BaseModel):
    status: DeliveryStatus
    notes: str | None = Field(default=None, max_length=1000)
    proof_photo_url: str | None = None


class DeliveryOut(ORMModel):
    id: int
    order_id: int
    driver_id: int
    status: DeliveryStatus
    notes: str | None = None
    proof_photo_url: str | None = None
    assigned_at: datetime
    delivered_at: datetime | None = None


# ---------- GPS tracking ----------
class GpsPointCreate(BaseModel):
    latitude: float = Field(ge=-90, le=90)
    longitude: float = Field(ge=-180, le=180)
    accuracy: float | None = Field(default=None, ge=0)
    recorded_at: datetime


class GpsBatchCreate(BaseModel):
    """Reps upload buffered points in bulk when connectivity returns."""

    points: list[GpsPointCreate] = Field(min_length=1, max_length=1000)


class GpsPointOut(ORMModel):
    id: int
    user_id: int
    latitude: float
    longitude: float
    accuracy: float | None = None
    recorded_at: datetime


class RouteOut(BaseModel):
    user_id: int
    date: str
    points: list[GpsPointOut]
    distance_km: float


# ---------- Reports ----------
class SalesByRep(BaseModel):
    user_id: int
    full_name: str
    order_count: int
    total_sales: Decimal


class DeliveryPerformance(BaseModel):
    driver_id: int
    full_name: str
    completed: int
    pending: int
    failed: int
