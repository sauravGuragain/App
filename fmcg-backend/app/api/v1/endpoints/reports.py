"""Aggregate report endpoints, computed with SQL group-by. Admin only."""
from collections import defaultdict
from decimal import Decimal

from fastapi import APIRouter
from sqlalchemy import func, select

from app.api.deps import AdminUser, SessionDep
from app.models.delivery import Delivery
from app.models.gps import GpsPoint
from app.models.order import Order, OrderItem
from app.models.product import Product
from app.models.store import Store
from app.models.user import User
from app.schemas.common import DeliveryStatus
from app.schemas.delivery import DeliveryPerformance, SalesByRep
from app.schemas.report import (
    DeliverySummary,
    DistanceByRep,
    NewStoresCount,
    SalesByProduct,
    SalesByStore,
)
from app.utils.geo import path_distance_km

router = APIRouter()


@router.get("/sales-by-rep", response_model=list[SalesByRep])
def sales_by_rep(db: SessionDep, _: AdminUser):
    stmt = select(
        Order.created_by, func.count(Order.id), func.coalesce(func.sum(Order.total), 0)
    ).group_by(Order.created_by)
    result = []
    for rep_id, count, total in db.execute(stmt).all():
        user = db.get(User, rep_id) if rep_id else None
        result.append(
            SalesByRep(
                user_id=rep_id or 0,
                full_name=user.full_name if user else "Unassigned",
                order_count=count,
                total_sales=Decimal(str(total)),
            )
        )
    return result


@router.get("/sales-by-product", response_model=list[SalesByProduct])
def sales_by_product(db: SessionDep, _: AdminUser):
    stmt = select(
        OrderItem.product_id,
        func.sum(OrderItem.quantity),
        func.coalesce(func.sum(OrderItem.line_total), 0),
    ).group_by(OrderItem.product_id)
    result = []
    for product_id, qty, total in db.execute(stmt).all():
        product = db.get(Product, product_id)
        result.append(
            SalesByProduct(
                product_id=product_id,
                name=product.name if product else f"Product {product_id}",
                quantity=int(qty or 0),
                total=Decimal(str(total)),
            )
        )
    return result


@router.get("/sales-by-store", response_model=list[SalesByStore])
def sales_by_store(db: SessionDep, _: AdminUser):
    stmt = select(
        Order.store_id, func.count(Order.id), func.coalesce(func.sum(Order.total), 0)
    ).group_by(Order.store_id)
    result = []
    for store_id, count, total in db.execute(stmt).all():
        store = db.get(Store, store_id)
        result.append(
            SalesByStore(
                store_id=store_id,
                name=store.name if store else f"Store {store_id}",
                order_count=count,
                total=Decimal(str(total)),
            )
        )
    return result


@router.get("/delivery-performance", response_model=list[DeliveryPerformance])
def delivery_performance(db: SessionDep, _: AdminUser):
    stmt = select(Delivery.driver_id, Delivery.status, func.count(Delivery.id)).group_by(
        Delivery.driver_id, Delivery.status
    )
    agg: dict[int, dict[str, int]] = {}
    for driver_id, st, count in db.execute(stmt).all():
        bucket = agg.setdefault(driver_id, {"completed": 0, "pending": 0, "failed": 0})
        key = (
            "completed" if st == DeliveryStatus.DELIVERED
            else "failed" if st == DeliveryStatus.FAILED
            else "pending"
        )
        bucket[key] += count
    result = []
    for driver_id, buckets in agg.items():
        user = db.get(User, driver_id)
        result.append(
            DeliveryPerformance(
                driver_id=driver_id,
                full_name=user.full_name if user else f"Driver {driver_id}",
                **buckets,
            )
        )
    return result


@router.get("/delivery-summary", response_model=DeliverySummary)
def delivery_summary(db: SessionDep, _: AdminUser):
    stmt = select(Delivery.status, func.count(Delivery.id)).group_by(Delivery.status)
    counts = {status: count for status, count in db.execute(stmt).all()}
    return DeliverySummary(
        pending=counts.get(DeliveryStatus.PENDING, 0),
        out_for_delivery=counts.get(DeliveryStatus.OUT_FOR_DELIVERY, 0),
        delivered=counts.get(DeliveryStatus.DELIVERED, 0),
        failed=counts.get(DeliveryStatus.FAILED, 0),
    )


@router.get("/distance-by-rep", response_model=list[DistanceByRep])
def distance_by_rep(db: SessionDep, _: AdminUser):
    stmt = select(
        GpsPoint.user_id, GpsPoint.latitude, GpsPoint.longitude
    ).order_by(GpsPoint.user_id, GpsPoint.recorded_at)
    paths: dict[int, list[tuple[float, float]]] = defaultdict(list)
    for user_id, lat, lng in db.execute(stmt).all():
        paths[user_id].append((lat, lng))
    result = []
    for user_id, points in paths.items():
        user = db.get(User, user_id)
        result.append(
            DistanceByRep(
                user_id=user_id,
                full_name=user.full_name if user else f"User {user_id}",
                distance_km=path_distance_km(points),
            )
        )
    return result


@router.get("/new-stores", response_model=NewStoresCount)
def new_stores(db: SessionDep, _: AdminUser):
    count = db.scalar(select(func.count()).select_from(Store)) or 0
    return NewStoresCount(count=count)
