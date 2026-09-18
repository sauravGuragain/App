"""Routes endpoints. Marketing reps manage routes and their shops."""
from fastapi import APIRouter, HTTPException, status

from app import crud
from app.api.deps import AdminUser, CurrentUser, FieldUser, PageDep, SessionDep
from app.models.route import Route
from app.schemas.common import Message, Page
from app.schemas.route import RouteCreate, RouteOut, RouteUpdate

router = APIRouter()


@router.get("", response_model=Page[RouteOut])
def list_routes(
    db: SessionDep,
    page: PageDep,
    user: CurrentUser,
    search: str | None = None,
):
    """List routes for the user's organization."""
    if user.organization_id is None:
        raise HTTPException(status.HTTP_403_FORBIDDEN, "User not assigned to organization")

    filters = [
        Route.organization_id == user.organization_id,
    ]
    if search:
        filters.append(Route.name.ilike(f"%{search}%"))

    rows, total = crud.route.list(db, page.skip, page.limit, *filters)
    return Page(items=rows, total=total, skip=page.skip, limit=page.limit)


@router.post("", response_model=RouteOut, status_code=status.HTTP_201_CREATED)
def create_route(
    payload: RouteCreate,
    db: SessionDep,
    user: FieldUser,
):
    """Create a new route. Field user can create routes for their organization."""
    if user.organization_id is None:
        raise HTTPException(status.HTTP_403_FORBIDDEN, "User not assigned to organization")

    return crud.route.create(
        db,
        {
            **payload.model_dump(),
            "organization_id": user.organization_id,
            "created_by": user.id,
        },
    )


@router.get("/{route_id}", response_model=RouteOut)
def get_route(
    route_id: int,
    db: SessionDep,
    user: CurrentUser,
):
    """Get a route by ID. User must belong to the same organization."""
    obj = crud.route.get(db, route_id)
    if obj is None:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Route not found")
    if obj.organization_id != user.organization_id:
        raise HTTPException(status.HTTP_403_FORBIDDEN, "Access denied")
    return obj


@router.patch("/{route_id}", response_model=RouteOut)
def update_route(
    route_id: int,
    payload: RouteUpdate,
    db: SessionDep,
    user: FieldUser,
):
    """Update a route. Field user can update routes in their organization."""
    obj = crud.route.get(db, route_id)
    if obj is None:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Route not found")
    if obj.organization_id != user.organization_id:
        raise HTTPException(status.HTTP_403_FORBIDDEN, "Access denied")

    return crud.route.update(db, obj, payload.model_dump(exclude_unset=True))


@router.delete("/{route_id}", response_model=Message)
def delete_route(
    route_id: int,
    db: SessionDep,
    user: AdminUser,
):
    """Delete a route. Admin can delete routes in their organization."""
    obj = crud.route.get(db, route_id)
    if obj is None:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Route not found")
    if obj.organization_id != user.organization_id:
        raise HTTPException(status.HTTP_403_FORBIDDEN, "Access denied")

    if not crud.route.remove(db, route_id):
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Route not found")
    return Message(message="Route deleted")
