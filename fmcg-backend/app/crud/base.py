"""Generic CRUD base.

The only layer that runs queries. Endpoints and services call these methods
with a Session; they never build SQL themselves. `list` returns a
(rows, total) tuple so list endpoints can populate the Page envelope.
"""
from typing import Any, Generic, TypeVar

from sqlalchemy import func, select
from sqlalchemy.orm import Session

from app.core.database import Base

ModelT = TypeVar("ModelT", bound=Base)


class CRUDBase(Generic[ModelT]):
    def __init__(self, model: type[ModelT]) -> None:
        self.model = model

    def get(self, db: Session, obj_id: int) -> ModelT | None:
        return db.get(self.model, obj_id)

    def list(
        self, db: Session, skip: int = 0, limit: int = 50, *filters: Any
    ) -> tuple[list[ModelT], int]:
        stmt = select(self.model)
        count_stmt = select(func.count()).select_from(self.model)
        for f in filters:
            stmt = stmt.where(f)
            count_stmt = count_stmt.where(f)
        total = db.scalar(count_stmt) or 0
        rows = list(
            db.scalars(stmt.order_by(self.model.id).offset(skip).limit(limit)).all()
        )
        return rows, total

    def create(self, db: Session, data: dict) -> ModelT:
        obj = self.model(**data)
        db.add(obj)
        db.commit()
        db.refresh(obj)
        return obj

    def update(self, db: Session, db_obj: ModelT, patch: dict) -> ModelT:
        for field, value in patch.items():
            setattr(db_obj, field, value)
        db.commit()
        db.refresh(db_obj)
        return db_obj

    def remove(self, db: Session, obj_id: int) -> bool:
        obj = db.get(self.model, obj_id)
        if obj is None:
            return False
        db.delete(obj)
        db.commit()
        return True
