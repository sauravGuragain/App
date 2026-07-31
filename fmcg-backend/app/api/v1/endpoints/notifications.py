"""Device-token registration for push notifications."""
from fastapi import APIRouter

from app.api.deps import CurrentUser, SessionDep
from app.models.device_token import DeviceToken
from app.schemas.common import Message
from app.schemas.user import DeviceTokenRegister
from sqlalchemy import select

router = APIRouter()


@router.post("/register", response_model=Message)
def register_token(payload: DeviceTokenRegister, db: SessionDep, user: CurrentUser):
    existing = db.scalar(select(DeviceToken).where(DeviceToken.token == payload.token))
    if existing:
        existing.user_id = user.id  # token moved to a (possibly) new user
        existing.platform = payload.platform
    else:
        db.add(
            DeviceToken(user_id=user.id, token=payload.token, platform=payload.platform)
        )
    db.commit()
    return Message(message="Token registered")


@router.delete("/register", response_model=Message)
def unregister_token(token: str, db: SessionDep, user: CurrentUser):
    obj = db.scalar(select(DeviceToken).where(DeviceToken.token == token))
    if obj is not None:
        db.delete(obj)
        db.commit()
    return Message(message="Token removed")
