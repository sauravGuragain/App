"""Central application configuration.

Values are read from environment variables (or a local .env file) exactly
once at import time and exposed through the `settings` singleton. Import it
anywhere with `from app.core.config import settings`.
"""
from functools import lru_cache

from pydantic import Field, PostgresDsn, computed_field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=".env", env_file_encoding="utf-8", extra="ignore"
    )

    PROJECT_NAME: str = "FMCG Platform"
    API_V1_PREFIX: str = "/api/v1"

    # Database
    POSTGRES_USER: str = "fmcg"
    POSTGRES_PASSWORD: str = "fmcg_secret"
    POSTGRES_DB: str = "fmcg"
    POSTGRES_HOST: str = "db"
    POSTGRES_PORT: int = 5432

    # Security
    SECRET_KEY: str = Field(default="change_me", min_length=8)
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60
    REFRESH_TOKEN_EXPIRE_DAYS: int = 30
    ALGORITHM: str = "HS256"

    BACKEND_CORS_ORIGINS: str = "http://localhost"

    FCM_CREDENTIALS_FILE: str = ""

    # Uploaded media (proof photos) — served as static files.
    MEDIA_DIR: str = "media"
    MEDIA_URL_PREFIX: str = "/media"

    # First admin, seeded on startup if the users table is empty.
    FIRST_ADMIN_EMAIL: str = "admin@example.com"
    FIRST_ADMIN_PASSWORD: str = "changeme123"
    FIRST_ADMIN_NAME: str = "System Admin"

    @computed_field  # type: ignore[misc]
    @property
    def DATABASE_URL(self) -> str:
        return str(
            PostgresDsn.build(
                scheme="postgresql+psycopg2",
                username=self.POSTGRES_USER,
                password=self.POSTGRES_PASSWORD,
                host=self.POSTGRES_HOST,
                port=self.POSTGRES_PORT,
                path=self.POSTGRES_DB,
            )
        )

    @computed_field  # type: ignore[misc]
    @property
    def cors_origins(self) -> list[str]:
        return [o.strip() for o in self.BACKEND_CORS_ORIGINS.split(",") if o.strip()]


@lru_cache
def get_settings() -> Settings:
    return Settings()


settings = get_settings()
