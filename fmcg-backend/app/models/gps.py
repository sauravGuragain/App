"""GPS tracking point.

High-write telemetry: `user_id` is indexed but intentionally NOT a hard
foreign key, so bulk ingest never pays FK-check cost and buffered offline
points can be flushed even if a user row is mid-change. Integrity of user_id
is guaranteed upstream by the authenticated token (Phase 4).
"""
from datetime import datetime

from sqlalchemy import DateTime, Float, Integer, Index
from sqlalchemy.orm import Mapped, mapped_column

from app.core.database import Base


class GpsPoint(Base):
    __tablename__ = "gps_points"

    id: Mapped[int] = mapped_column(primary_key=True)
    user_id: Mapped[int] = mapped_column(Integer, index=True)
    latitude: Mapped[float] = mapped_column(Float)
    longitude: Mapped[float] = mapped_column(Float)
    accuracy: Mapped[float | None] = mapped_column(Float, nullable=True)
    recorded_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), index=True)

    __table_args__ = (Index("ix_gps_user_recorded", "user_id", "recorded_at"),)
