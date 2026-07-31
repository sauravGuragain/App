"""Store (customer) schemas.

Coordinates are validated to real WGS-84 ranges so a bad map tap can never
be persisted.
"""
from datetime import datetime

from pydantic import BaseModel, Field

from app.schemas.common import ORMModel

Latitude = Field(ge=-90, le=90)
Longitude = Field(ge=-180, le=180)


class StoreBase(BaseModel):
    name: str = Field(min_length=1, max_length=160)
    owner_name: str | None = Field(default=None, max_length=120)
    phone: str | None = Field(default=None, max_length=20)
    address: str | None = Field(default=None, max_length=300)
    latitude: float = Latitude
    longitude: float = Longitude
    notes: str | None = Field(default=None, max_length=1000)
    photo_url: str | None = None


class StoreCreate(StoreBase):
    pass


class StoreUpdate(BaseModel):
    name: str | None = Field(default=None, min_length=1, max_length=160)
    owner_name: str | None = Field(default=None, max_length=120)
    phone: str | None = Field(default=None, max_length=20)
    address: str | None = Field(default=None, max_length=300)
    latitude: float | None = Field(default=None, ge=-90, le=90)
    longitude: float | None = Field(default=None, ge=-180, le=180)
    notes: str | None = Field(default=None, max_length=1000)
    photo_url: str | None = None


class StoreOut(ORMModel, StoreBase):
    id: int
    created_by: int | None = None
    created_at: datetime
