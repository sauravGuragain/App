"""Aggregate router for API v1."""
from fastapi import APIRouter

from app.api.v1.endpoints import (
    auth,
    media,
    notifications,
    deliveries,
    gps,
    orders,
    products,
    reports,
    stores,
    users,
)

api_router = APIRouter()


@api_router.get("/ping", tags=["health"])
def ping() -> dict:
    return {"status": "ok"}


api_router.include_router(auth.router, prefix="/auth", tags=["auth"])
api_router.include_router(users.router, prefix="/users", tags=["users"])
api_router.include_router(products.router, prefix="/products", tags=["products"])
api_router.include_router(stores.router, prefix="/stores", tags=["stores"])
api_router.include_router(orders.router, prefix="/orders", tags=["orders"])
api_router.include_router(deliveries.router, prefix="/deliveries", tags=["deliveries"])
api_router.include_router(gps.router, prefix="/gps", tags=["gps"])
api_router.include_router(reports.router, prefix="/reports", tags=["reports"])
api_router.include_router(media.router, prefix="/media", tags=["media"])
api_router.include_router(notifications.router, prefix="/notifications", tags=["notifications"])
