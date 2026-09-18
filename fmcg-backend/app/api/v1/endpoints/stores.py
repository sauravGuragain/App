"""Stores. Reps/admin create & edit; admin deletes; created_by = the creator.

Stores are now organized by route and separated by organization (NTC/FMCG).
Users can only access stores in their own organization.
"""
from fastapi import APIRouter, HTTPException, status

from app import crud
from app.api.deps import AdminUser, CurrentUser, FieldUser, PageDep, SessionDep
from app.models.store import Store
from app.schemas.common import Message, Page
from app.schemas.store import StoreCreate, StoreOut, StoreUpdate

router = APIRouter()


@router.get("", response_model=Page[StoreOut])
def list_stores(
    db: SessionDep,
    page: PageDep,
    user: CurrentUser,
    search: str | None = None,
    route_id: int | None = None,
):
    """List stores for the user's organization, optionally filtered by route."""
    if user.organization_id is None:
        raise HTTPException(status.HTTP_403_FORBIDDEN, "User not assigned to organization")

    filters = [Store.organization_id == user.organization_id]

    if search:
        filters.append(Store.name.ilike(f"%{search}%"))

    if route_id is not None:
        filters.append(Store.route_id == route_id)

    rows, total = crud.store.list(db, page.skip, page.limit, *filters)
    return Page(items=rows, total=total, skip=page.skip, limit=page.limit)


@router.post("", response_model=StoreOut, status_code=status.HTTP_201_CREATED)
def create_store(payload: StoreCreate, db: SessionDep, user: FieldUser):
    """Create a store in the user's organization."""
    if user.organization_id is None:
        raise HTTPException(status.HTTP_403_FORBIDDEN, "User not assigned to organization")

    return crud.store.create(
        db,
        {
            **payload.model_dump(),
            "organization_id": user.organization_id,
            "created_by": user.id,
        },
    )


@router.get("/{store_id}", response_model=StoreOut)
def get_store(store_id: int, db: SessionDep, user: CurrentUser):
    """Get a store by ID. User must belong to the same organization."""
    obj = crud.store.get(db, store_id)
    if obj is None:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Store not found")
    if obj.organization_id != user.organization_id:
        raise HTTPException(status.HTTP_403_FORBIDDEN, "Access denied")
    return obj


@router.patch("/{store_id}", response_model=StoreOut)
def update_store(store_id: int, payload: StoreUpdate, db: SessionDep, user: FieldUser):
    """Update a store. User must belong to the same organization."""
    obj = crud.store.get(db, store_id)
    if obj is None:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Store not found")
    if obj.organization_id != user.organization_id:
        raise HTTPException(status.HTTP_403_FORBIDDEN, "Access denied")
    return crud.store.update(db, obj, payload.model_dump(exclude_unset=True))


@router.delete("/{store_id}", response_model=Message)
def delete_store(store_id: int, db: SessionDep, user: AdminUser):
    """Delete a store. Admin can delete stores in their organization."""
    obj = crud.store.get(db, store_id)
    if obj is None:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Store not found")
    if obj.organization_id != user.organization_id:
        raise HTTPException(status.HTTP_403_FORBIDDEN, "Access denied")

    if not crud.store.remove(db, store_id):
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Store not found")
    return Message(message="Store deleted")
