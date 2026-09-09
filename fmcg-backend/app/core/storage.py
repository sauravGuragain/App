"""Image storage.

Two backends, chosen at runtime:

* **Supabase Storage** — used when SUPABASE_URL and SUPABASE_SERVICE_KEY are
  set. Files survive redeploys, which matters on hosting with an ephemeral
  filesystem (Render's free tier rebuilds the container on every wake).
* **Local disk** — the fallback, served by the StaticFiles mount in app.main.
  Fine for local development; files are lost on redeploy in the cloud.

Returned URLs are stored verbatim in `deliveries.proof_photo_url`, so the
local backend keeps returning a relative path and Supabase returns an
absolute one. Both work in the Android client, which treats it as an opaque
string.
"""
import os
import uuid

import httpx

from app.core.config import settings

EXT_BY_TYPE = {"image/jpeg": "jpg", "image/png": "png", "image/webp": "webp"}


def uses_supabase() -> bool:
    """True when Supabase Storage is configured."""
    return bool(settings.SUPABASE_URL and settings.SUPABASE_SERVICE_KEY)


def save_image(data: bytes, content_type: str) -> str:
    """Persist an image and return the URL to store against the record."""
    name = f"{uuid.uuid4().hex}.{EXT_BY_TYPE[content_type]}"
    if uses_supabase():
        return _save_supabase(name, data, content_type)
    return _save_local(name, data)


def _save_local(name: str, data: bytes) -> str:
    os.makedirs(settings.MEDIA_DIR, exist_ok=True)
    with open(os.path.join(settings.MEDIA_DIR, name), "wb") as out:
        out.write(data)
    return f"{settings.MEDIA_URL_PREFIX}/{name}"


def _auth_headers() -> dict[str, str]:
    """Supabase accepts two key formats, on different headers.

    Legacy `service_role` keys are HS256 JWTs and go on Authorization. The
    newer `sb_secret_...` keys are opaque, not JWTs, and are rejected there
    with "Invalid Compact JWS" — they belong on the apikey header.
    """
    key = settings.SUPABASE_SERVICE_KEY
    if key.startswith("sb_"):
        return {"apikey": key}
    return {"Authorization": f"Bearer {key}", "apikey": key}


def _save_supabase(name: str, data: bytes, content_type: str) -> str:
    base = settings.SUPABASE_URL.rstrip("/")
    bucket = settings.SUPABASE_BUCKET
    resp = httpx.post(
        f"{base}/storage/v1/object/{bucket}/{name}",
        content=data,
        headers=_auth_headers() | {
            "Content-Type": content_type,
            "cache-control": "3600",
        },
        timeout=30.0,
    )
    resp.raise_for_status()
    # Public bucket — no signing needed.
    return f"{base}/storage/v1/object/public/{bucket}/{name}"
