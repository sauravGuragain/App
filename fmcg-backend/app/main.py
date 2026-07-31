"""FastAPI application factory / entrypoint.

Run locally:
    uvicorn app.main:app --reload
Swagger UI:  http://localhost:8000/docs
"""
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
import os

from app.api.v1.router import api_router
from app.core.config import settings


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Seed the first admin on startup (best-effort — DB may be warming up).
    try:
        from app.core.bootstrap import ensure_first_admin
        from app.core.database import SessionLocal

        with SessionLocal() as db:
            ensure_first_admin(db)
    except Exception:  # pragma: no cover - defensive
        pass
    yield


def create_app() -> FastAPI:
    app = FastAPI(
        title=settings.PROJECT_NAME,
        openapi_url=f"{settings.API_V1_PREFIX}/openapi.json",
        docs_url="/docs",
        lifespan=lifespan,
    )

    if settings.cors_origins:
        app.add_middleware(
            CORSMiddleware,
            allow_origins=settings.cors_origins,
            allow_credentials=True,
            allow_methods=["*"],
            allow_headers=["*"],
        )

    app.include_router(api_router, prefix=settings.API_V1_PREFIX)

    os.makedirs(settings.MEDIA_DIR, exist_ok=True)
    app.mount(settings.MEDIA_URL_PREFIX, StaticFiles(directory=settings.MEDIA_DIR), name="media")

    @app.get("/health", tags=["health"])
    def health() -> dict:
        return {"status": "healthy", "service": settings.PROJECT_NAME}

    return app


app = create_app()
