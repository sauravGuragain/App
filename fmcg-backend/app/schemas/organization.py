"""Organization schemas."""
from datetime import datetime

from pydantic import BaseModel, Field

from app.schemas.common import ORMModel


class OrganizationBase(BaseModel):
    name: str = Field(min_length=1, max_length=120)


class OrganizationCreate(OrganizationBase):
    pass


class OrganizationOut(ORMModel, OrganizationBase):
    id: int
    created_at: datetime
