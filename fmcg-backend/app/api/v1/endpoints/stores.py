"""Stores. Reps/admin create & edit; admin deletes; created_by = the creator."""
from fastapi import APIRouter, HTTPException, status

from app import crud
from app.api.deps import AdminUser, CurrentUser, FieldUser, PageDep, SessionDep
from app.models.store import Store
from app.schemas.common import Message, Page
from app.schemas.store import StoreCreate, StoreOut, StoreUpdate

router = APIRouter()


@router.get("", response_model=Page[StoreOut])
def list_stores(db: SessionDep, page: PageDep, _: CurrentUser, search: str | None = None):
    filters = [Store.name.ilike(f"%{search}%")] if search else []
    rows, total = crud.store.list(db, page.skip, page.limit, *filters)
    return Page(items=rows, total=total, skip=page.skip, limit=page.limit)


@router.post("", response_model=StoreOut, status_code=status.HTTP_201_CREATED)
def create_store(payload: StoreCreate, db: SessionDep, user: FieldUser):
    return crud.store.create(db, {**payload.model_dump(), "created_by": user.id})


@router.get("/{store_id}", response_model=StoreOut)
def get_store(store_id: int, db: SessionDep, _: CurrentUser):
    obj = crud.store.get(db, store_id)
    if obj is None:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Store not found")
    return obj


@router.patch("/{store_id}", response_model=StoreOut)
def update_store(store_id: int, payload: StoreUpdate, db: SessionDep, _: FieldUser):
    obj = crud.store.get(db, store_id)
    if obj is None:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Store not found")
    return crud.store.update(db, obj, payload.model_dump(exclude_unset=True))


@router.delete("/{store_id}", response_model=Message)
def delete_store(store_id: int, db: SessionDep, _: AdminUser):
    if not crud.store.remove(db, store_id):
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Store not found")
    return Message(message="Store deleted")
