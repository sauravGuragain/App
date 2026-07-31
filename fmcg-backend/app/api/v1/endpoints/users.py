"""User management endpoints — admin only."""
from fastapi import APIRouter, HTTPException, status

from app import crud
from app.api.deps import AdminUser, PageDep, SessionDep
from app.core.security import hash_password
from app.schemas.common import Message, Page
from app.schemas.user import UserCreate, UserOut, UserUpdate

router = APIRouter()


@router.get("", response_model=Page[UserOut])
def list_users(db: SessionDep, page: PageDep, _: AdminUser):
    rows, total = crud.user.list(db, page.skip, page.limit)
    return Page(items=rows, total=total, skip=page.skip, limit=page.limit)


@router.post("", response_model=UserOut, status_code=status.HTTP_201_CREATED)
def create_user(payload: UserCreate, db: SessionDep, _: AdminUser):
    if crud.user.get_by_email(db, payload.email):
        raise HTTPException(status.HTTP_409_CONFLICT, "Email already registered")
    data = payload.model_dump(exclude={"password"})
    data["hashed_password"] = hash_password(payload.password)
    return crud.user.create(db, data)


@router.get("/{user_id}", response_model=UserOut)
def get_user(user_id: int, db: SessionDep, _: AdminUser):
    obj = crud.user.get(db, user_id)
    if obj is None:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "User not found")
    return obj


@router.patch("/{user_id}", response_model=UserOut)
def update_user(user_id: int, payload: UserUpdate, db: SessionDep, _: AdminUser):
    obj = crud.user.get(db, user_id)
    if obj is None:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "User not found")
    patch = payload.model_dump(exclude_unset=True, exclude={"password"})
    if payload.password:
        patch["hashed_password"] = hash_password(payload.password)
    return crud.user.update(db, obj, patch)


@router.delete("/{user_id}", response_model=Message)
def delete_user(user_id: int, db: SessionDep, _: AdminUser):
    if not crud.user.remove(db, user_id):
        raise HTTPException(status.HTTP_404_NOT_FOUND, "User not found")
    return Message(message="User deleted")
