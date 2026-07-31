"""Media upload endpoint.

Stores images on local disk (free, no cloud) and returns a relative URL served
by the StaticFiles mount in app.main. Used for delivery proof photos (Phase 10)
and, later, optional store photos.
"""
import os
import shutil
import uuid

from fastapi import APIRouter, File, HTTPException, UploadFile, status
from pydantic import BaseModel

from app.api.deps import CurrentUser
from app.core.config import settings

router = APIRouter()

_EXT = {"image/jpeg": "jpg", "image/png": "png", "image/webp": "webp"}


class MediaUrl(BaseModel):
    url: str


@router.post("/upload", response_model=MediaUrl)
def upload(user: CurrentUser, file: UploadFile = File(...)):
    if file.content_type not in _EXT:
        raise HTTPException(
            status.HTTP_415_UNSUPPORTED_MEDIA_TYPE,
            "Only JPEG, PNG or WebP images are allowed",
        )
    os.makedirs(settings.MEDIA_DIR, exist_ok=True)
    name = f"{uuid.uuid4().hex}.{_EXT[file.content_type]}"
    dest = os.path.join(settings.MEDIA_DIR, name)
    with open(dest, "wb") as out:
        shutil.copyfileobj(file.file, out)
    return MediaUrl(url=f"{settings.MEDIA_URL_PREFIX}/{name}")
