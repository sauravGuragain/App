"""Product catalogue. Reads: any authenticated user. Add/edit: admin + marketing. Delete: admin only."""
from fastapi import APIRouter, HTTPException, status

from app import crud
from app.api.deps import AdminUser, CurrentUser, FieldUser, PageDep, SessionDep
from app.models.product import Product
from app.schemas.common import Message, Page
from app.schemas.product import ProductCreate, ProductOut, ProductUpdate

router = APIRouter()


@router.get("", response_model=Page[ProductOut])
def list_products(db: SessionDep, page: PageDep, _: CurrentUser, active_only: bool = False):
    filters = [Product.is_active.is_(True)] if active_only else []
    rows, total = crud.product.list(db, page.skip, page.limit, *filters)
    return Page(items=rows, total=total, skip=page.skip, limit=page.limit)


@router.post("", response_model=ProductOut, status_code=status.HTTP_201_CREATED)
def create_product(payload: ProductCreate, db: SessionDep, _: FieldUser):
    if crud.product.get_by_sku(db, payload.sku):
        raise HTTPException(status.HTTP_409_CONFLICT, "SKU already exists")
    return crud.product.create(db, payload.model_dump())


@router.get("/{product_id}", response_model=ProductOut)
def get_product(product_id: int, db: SessionDep, _: CurrentUser):
    obj = crud.product.get(db, product_id)
    if obj is None:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Product not found")
    return obj


@router.patch("/{product_id}", response_model=ProductOut)
def update_product(product_id: int, payload: ProductUpdate, db: SessionDep, _: FieldUser):
    obj = crud.product.get(db, product_id)
    if obj is None:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Product not found")
    return crud.product.update(db, obj, payload.model_dump(exclude_unset=True))


@router.delete("/{product_id}", response_model=Message)
def delete_product(product_id: int, db: SessionDep, _: AdminUser):
    if not crud.product.remove(db, product_id):
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Product not found")
    return Message(message="Product deleted")
