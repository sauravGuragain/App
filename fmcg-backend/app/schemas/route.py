"""Route schemas."""
from datetime import datetime

from pydantic import BaseModel, Field

from app.schemas.common import ORMModel


class RouteBase(BaseModel):
    name: str = Field(min_length=1, max_length=160)


class RouteCreate(RouteBase):
    pass


class RouteUpdate(BaseModel):
    name: str | None = Field(default=None, min_length=1, max_length=160)


class RouteOut(ORMModel, RouteBase):
    id: int
    organization_id: int
    created_by: int | None = None
    created_at: datetime
