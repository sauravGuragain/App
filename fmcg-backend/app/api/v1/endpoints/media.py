"""Media upload endpoint.

Delegates persistence to app.core.storage, which writes to Supabase Storage
when configured and falls back to local disk otherwise. Used for delivery
proof photos (Phase 10) and, later, optional store photos.
"""
import httpx
from fastapi import APIRouter, File, HTTPException, UploadFile, status
from pydantic import BaseModel

from app.api.deps import CurrentUser
from app.core.storage import EXT_BY_TYPE, save_image

router = APIRouter()

MAX_UPLOAD_BYTES = 8 * 1024 * 1024


class MediaUrl(BaseModel):
    url: str


@router.post("/upload", response_model=MediaUrl)
def upload(user: CurrentUser, file: UploadFile = File(...)):
    if file.content_type not in EXT_BY_TYPE:
        raise HTTPException(
            status.HTTP_415_UNSUPPORTED_MEDIA_TYPE,
            "Only JPEG, PNG or WebP images are allowed",
        )

    data = file.file.read()
    if not data:
        raise HTTPException(status.HTTP_400_BAD_REQUEST, "Empty file")
    if len(data) > MAX_UPLOAD_BYTES:
        raise HTTPException(
            status.HTTP_413_REQUEST_ENTITY_TOO_LARGE,
            "Image is too large (max 8 MB)",
        )

    try:
        url = save_image(data, file.content_type)
    except httpx.HTTPError as exc:  # upstream storage rejected or unreachable
        raise HTTPException(
            status.HTTP_502_BAD_GATEWAY,
            "Could not store the image — try again",
        ) from exc

    return MediaUrl(url=url)
