"""Reusable FastAPI dependencies: DB session, pagination, auth & RBAC.

get_current_user decodes the bearer access token and loads the user; the
require_role factory builds role guards. Endpoints depend on the typed
aliases at the bottom (CurrentUser, AdminUser, ...).
"""
from typing import Annotated

from fastapi import Depends, HTTPException, Query, status
from fastapi.security import OAuth2PasswordBearer
from sqlalchemy.orm import Session

from app import crud
from app.core.config import settings
from app.core.database import get_db as _get_db
from app.core.security import decode_token
from app.models.user import User
from app.schemas.common import PageParams, UserRole

oauth2_scheme = OAuth2PasswordBearer(tokenUrl=f"{settings.API_V1_PREFIX}/auth/login")


def pagination(
    skip: int = Query(0, ge=0),
    limit: int = Query(50, ge=1, le=200),
) -> PageParams:
    return PageParams(skip=skip, limit=limit)


SessionDep = Annotated[Session, Depends(_get_db)]
PageDep = Annotated[PageParams, Depends(pagination)]


_credentials_exc = HTTPException(
    status_code=status.HTTP_401_UNAUTHORIZED,
    detail="Could not validate credentials",
    headers={"WWW-Authenticate": "Bearer"},
)


def get_current_user(
    db: SessionDep, token: Annotated[str, Depends(oauth2_scheme)]
) -> User:
    payload = decode_token(token)
    if not payload or payload.get("type") != "access":
        raise _credentials_exc
    sub = payload.get("sub")
    if sub is None:
        raise _credentials_exc
    user = crud.user.get(db, int(sub))
    if user is None or not user.is_active:
        raise _credentials_exc
    return user


CurrentUser = Annotated[User, Depends(get_current_user)]


def require_role(*roles: UserRole):
    def checker(user: CurrentUser) -> User:
        if user.role not in roles:
            raise HTTPException(
                status.HTTP_403_FORBIDDEN,
                detail="Insufficient permissions for this action",
            )
        return user

    return checker


AdminUser = Annotated[User, Depends(require_role(UserRole.ADMIN))]
FieldUser = Annotated[User, Depends(require_role(UserRole.MARKETING, UserRole.ADMIN))]
