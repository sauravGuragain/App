"""Concrete repositories.

Each extends CRUDBase with the resource-specific lookups its endpoints need.
Singletons at the bottom are what the API imports (`from app import crud`).
"""
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.crud.base import CRUDBase
from app.models.delivery import Delivery
from app.models.gps import GpsPoint
from app.models.order import Order, OrderItem
from app.models.product import Product
from app.models.store import Store
from app.models.user import User


class CRUDUser(CRUDBase[User]):
    def get_by_email(self, db: Session, email: str) -> User | None:
        return db.scalar(select(User).where(User.email == email))


class CRUDProduct(CRUDBase[Product]):
    def get_by_sku(self, db: Session, sku: str) -> Product | None:
        return db.scalar(select(Product).where(Product.sku == sku))


class CRUDStore(CRUDBase[Store]):
    pass


class CRUDOrder(CRUDBase[Order]):
    def create_with_items(self, db: Session, order_data: dict, items: list[dict]) -> Order:
        order = Order(**order_data)
        order.items = [OrderItem(**i) for i in items]
        db.add(order)
        db.commit()
        db.refresh(order)
        return order


class CRUDDelivery(CRUDBase[Delivery]):
    pass


class CRUDGps(CRUDBase[GpsPoint]):
    def add_batch(self, db: Session, rows: list[dict]) -> int:
        db.add_all([GpsPoint(**r) for r in rows])
        db.commit()
        return len(rows)

    def route_points(self, db: Session, user_id: int, day: str) -> list[GpsPoint]:
        stmt = (
            select(GpsPoint)
            .where(GpsPoint.user_id == user_id)
            .order_by(GpsPoint.recorded_at)
        )
        return [p for p in db.scalars(stmt).all()
                if p.recorded_at.date().isoformat() == day]


user = CRUDUser(User)
product = CRUDProduct(Product)
store = CRUDStore(Store)
order = CRUDOrder(Order)
delivery = CRUDDelivery(Delivery)
gps = CRUDGps(GpsPoint)
