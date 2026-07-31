"""Deliveries. Admin assigns; the assigned driver (or admin) updates status.
Delivery staff see only their own assignments."""
from datetime import datetime, timezone

from fastapi import APIRouter, HTTPException, status

from app import crud
from app.api.deps import AdminUser, CurrentUser, PageDep, SessionDep
from app.models.delivery import Delivery
from app.schemas.common import DeliveryStatus, OrderStatus, Page, UserRole
from app.schemas.delivery import DeliveryAssign, DeliveryOut, DeliveryStatusUpdate
from app.services import notification_service

router = APIRouter()


@router.get("", response_model=Page[DeliveryOut])
def list_deliveries(db: SessionDep, page: PageDep, user: CurrentUser, driver_id: int | None = None):
    filters = []
    if user.role == UserRole.DELIVERY:
        filters.append(Delivery.driver_id == user.id)  # drivers see only their own
    elif driver_id:
        filters.append(Delivery.driver_id == driver_id)
    rows, total = crud.delivery.list(db, page.skip, page.limit, *filters)
    return Page(items=rows, total=total, skip=page.skip, limit=page.limit)


@router.post("", response_model=DeliveryOut, status_code=status.HTTP_201_CREATED)
def assign_delivery(payload: DeliveryAssign, db: SessionDep, _: AdminUser):
    order = crud.order.get(db, payload.order_id)
    if order is None:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Order not found")
    if order.status != OrderStatus.APPROVED:
        raise HTTPException(status.HTTP_409_CONFLICT, "Only approved orders can be assigned")
    driver = crud.user.get(db, payload.driver_id)
    if driver is None:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Driver not found")
    if driver.role != UserRole.DELIVERY:
        raise HTTPException(status.HTTP_422_UNPROCESSABLE_ENTITY, "Assignee is not delivery staff")

    obj = crud.delivery.create(
        db,
        {
            "order_id": payload.order_id,
            "driver_id": payload.driver_id,
            "status": DeliveryStatus.PENDING,
            "assigned_at": datetime.now(timezone.utc),
        },
    )
    crud.order.update(db, order, {"status": OrderStatus.ASSIGNED})
    notification_service.send_to_user(
        db, payload.driver_id, "New delivery assigned",
        f"Order #{order.id} is ready for delivery",
        {"delivery_id": obj.id, "order_id": order.id},
    )
    return obj


@router.get("/{delivery_id}", response_model=DeliveryOut)
def get_delivery(delivery_id: int, db: SessionDep, user: CurrentUser):
    obj = crud.delivery.get(db, delivery_id)
    if obj is None:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Delivery not found")
    if user.role == UserRole.DELIVERY and obj.driver_id != user.id:
        raise HTTPException(status.HTTP_403_FORBIDDEN, "Not your delivery")
    return obj


@router.patch("/{delivery_id}/status", response_model=DeliveryOut)
def update_delivery_status(
    delivery_id: int, payload: DeliveryStatusUpdate, db: SessionDep, user: CurrentUser
):
    obj = crud.delivery.get(db, delivery_id)
    if obj is None:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Delivery not found")
    # Only the assigned driver or an admin may update.
    if user.role == UserRole.DELIVERY and obj.driver_id != user.id:
        raise HTTPException(status.HTTP_403_FORBIDDEN, "Not your delivery")
    if user.role == UserRole.MARKETING:
        raise HTTPException(status.HTTP_403_FORBIDDEN, "Insufficient permissions")

    patch = payload.model_dump(exclude_unset=True)
    if payload.status == DeliveryStatus.DELIVERED:
        patch["delivered_at"] = datetime.now(timezone.utc)
        order = crud.order.get(db, obj.order_id)
        if order is not None:
            crud.order.update(db, order, {"status": OrderStatus.DELIVERED})
            if order.created_by:
                notification_service.send_to_user(
                    db, order.created_by, "Order delivered",
                    f"Order #{order.id} has been delivered",
                    {"order_id": order.id},
                )
    return crud.delivery.update(db, obj, patch)
