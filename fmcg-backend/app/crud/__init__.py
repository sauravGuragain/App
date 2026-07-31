"""CRUD singletons: `from app import crud; crud.product.get(db, 1)`."""
from app.crud.repositories import (
    delivery,
    gps,
    order,
    product,
    store,
    user,
)

__all__ = ["user", "product", "store", "order", "delivery", "gps"]
