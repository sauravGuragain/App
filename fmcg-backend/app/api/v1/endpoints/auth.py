"""Authentication endpoints.

/login uses the OAuth2 password form (username = email) so Swagger's
"Authorize" button works out of the box. Tokens are stateless JWTs; refresh
exchanges a valid refresh token for a fresh pair.
"""
from typing import Annotated

from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordRequestForm

from app import crud
from app.api.deps import CurrentUser, SessionDep
from app.core.security import (
    create_access_token,
    create_refresh_token,
    decode_token,
    verify_password,
)
from app.schemas.user import RefreshRequest, Token, UserOut

router = APIRouter()


def _issue(user_id: int) -> Token:
    return Token(
        access_token=create_access_token(user_id),
        refresh_token=create_refresh_token(user_id),
    )


@router.post("/login", response_model=Token)
def login(
    db: SessionDep,
    form: Annotated[OAuth2PasswordRequestForm, Depends()],
):
    user = crud.user.get_by_email(db, form.username)
    if user is None or not verify_password(form.password, user.hashed_password):
        raise HTTPException(
            status.HTTP_401_UNAUTHORIZED, "Incorrect email or password"
        )
    if not user.is_active:
        raise HTTPException(status.HTTP_403_FORBIDDEN, "Account is disabled")
    return _issue(user.id)


@router.post("/refresh", response_model=Token)
def refresh(payload: RefreshRequest, db: SessionDep):
    data = decode_token(payload.refresh_token)
    if not data or data.get("type") != "refresh":
        raise HTTPException(status.HTTP_401_UNAUTHORIZED, "Invalid refresh token")
    user = crud.user.get(db, int(data["sub"]))
    if user is None or not user.is_active:
        raise HTTPException(status.HTTP_401_UNAUTHORIZED, "Invalid refresh token")
    return _issue(user.id)


@router.get("/me", response_model=UserOut)
def me(user: CurrentUser):
    return user
