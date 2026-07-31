"""GPS tracking. A user may only upload/read their own points (admin: any)."""
from fastapi import APIRouter, HTTPException, status

from app import crud
from app.api.deps import CurrentUser, SessionDep
from app.models.user import User
from app.schemas.common import Message, UserRole
from app.schemas.delivery import GpsBatchCreate, RouteOut
from app.utils.geo import path_distance_km


def _assert_self_or_admin(user: User, user_id: int) -> None:
    if user.role != UserRole.ADMIN and user.id != user_id:
        raise HTTPException(status.HTTP_403_FORBIDDEN, "Cannot access another user's GPS data")


router = APIRouter()


@router.post("/{user_id}/batch", response_model=Message)
def upload_points(user_id: int, payload: GpsBatchCreate, db: SessionDep, user: CurrentUser):
    _assert_self_or_admin(user, user_id)
    rows = [{"user_id": user_id, **p.model_dump()} for p in payload.points]
    n = crud.gps.add_batch(db, rows)
    return Message(message=f"Stored {n} point(s)")


@router.get("/{user_id}/route", response_model=RouteOut)
def get_route(user_id: int, date: str, db: SessionDep, user: CurrentUser):
    _assert_self_or_admin(user, user_id)
    points = crud.gps.route_points(db, user_id, date)
    distance = path_distance_km([(p.latitude, p.longitude) for p in points])
    return RouteOut(user_id=user_id, date=date, points=points, distance_km=distance)
