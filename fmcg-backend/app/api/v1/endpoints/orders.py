"""Orders. Reps create (created_by = them) and see their own; admin sees all
and drives the approval/cancel status transitions."""
from fastapi import APIRouter, HTTPException, status

from app import crud
from app.api.deps import AdminUser, CurrentUser, FieldUser, PageDep, SessionDep
from app.models.order import Order
from app.schemas.common import OrderStatus, Page, UserRole
from app.schemas.order import OrderCreate, OrderOut, OrderStatusUpdate
from app.services import order_service

router = APIRouter()


@router.get("", response_model=Page[OrderOut])
def list_orders(db: SessionDep, page: PageDep, user: CurrentUser, store_id: int | None = None):
    filters = []
    if store_id:
        filters.append(Order.store_id == store_id)
    # Reps only see their own orders; admins see everything.
    if user.role == UserRole.MARKETING:
        filters.append(Order.created_by == user.id)
    rows, total = crud.order.list(db, page.skip, page.limit, *filters)
    return Page(items=rows, total=total, skip=page.skip, limit=page.limit)


@router.post("", response_model=OrderOut, status_code=status.HTTP_201_CREATED)
def create_order(payload: OrderCreate, db: SessionDep, user: FieldUser):
    if crud.store.get(db, payload.store_id) is None:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Store not found")
    for item in payload.items:
        if crud.product.get(db, item.product_id) is None:
            raise HTTPException(
                status.HTTP_404_NOT_FOUND, f"Product {item.product_id} not found"
            )

    subtotal, total_discount, total = order_service.compute_totals(payload.items)
    order_data = {
        "store_id": payload.store_id,
        "created_by": user.id,
        "status": OrderStatus.PENDING,
        "subtotal": subtotal,
        "total_discount": total_discount,
        "total": total,
        "notes": payload.notes,
    }
    item_rows = [
        {**i.model_dump(), "line_total": order_service.compute_line(i)}
        for i in payload.items
    ]
    return crud.order.create_with_items(db, order_data, item_rows)


@router.get("/{order_id}", response_model=OrderOut)
def get_order(order_id: int, db: SessionDep, user: CurrentUser):
    obj = crud.order.get(db, order_id)
    if obj is None:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Order not found")
    if user.role == UserRole.MARKETING and obj.created_by != user.id:
        raise HTTPException(status.HTTP_403_FORBIDDEN, "Not your order")
    return obj


@router.patch("/{order_id}/status", response_model=OrderOut)
def update_status(order_id: int, payload: OrderStatusUpdate, db: SessionDep, _: AdminUser):
    obj = crud.order.get(db, order_id)
    if obj is None:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Order not found")
    order_service.assert_transition(obj.status, payload.status)
    return crud.order.update(db, obj, {"status": payload.status})
