"""Shared schema primitives used across every resource.

Enums here are the single source of truth for roles and status workflows;
Phase 3 references the same values when defining ORM columns so the API and
the database can never drift apart.
"""
from enum import Enum
from typing import Generic, TypeVar

from pydantic import BaseModel, ConfigDict, Field

T = TypeVar("T")


class UserRole(str, Enum):
    ADMIN = "admin"
    MARKETING = "marketing"
    DELIVERY = "delivery"


class OrderStatus(str, Enum):
    DRAFT = "draft"
    PENDING = "pending"
    APPROVED = "approved"
    ASSIGNED = "assigned"
    OUT_FOR_DELIVERY = "out_for_delivery"
    DELIVERED = "delivered"
    CANCELLED = "cancelled"
    RETURNED = "returned"


class DeliveryStatus(str, Enum):
    PENDING = "pending"
    OUT_FOR_DELIVERY = "out_for_delivery"
    DELIVERED = "delivered"
    FAILED = "failed"


class ORMModel(BaseModel):
    """Base for every *response* schema — reads attributes off ORM objects."""

    model_config = ConfigDict(from_attributes=True)


class Page(BaseModel, Generic[T]):
    """Envelope returned by every list endpoint."""

    items: list[T]
    total: int
    skip: int
    limit: int


class ErrorResponse(BaseModel):
    detail: str
    code: str = "error"


class Message(BaseModel):
    message: str


class PageParams(BaseModel):
    skip: int = Field(default=0, ge=0)
    limit: int = Field(default=50, ge=1, le=200)
