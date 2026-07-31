"""Database engine, session factory and the declarative Base.

Phase 3 will define concrete ORM models that inherit from `Base`. The
`get_db` generator is the single FastAPI dependency used by every endpoint
that needs a transactional session.
"""
from collections.abc import Generator

from sqlalchemy import create_engine
from sqlalchemy.orm import DeclarativeBase, sessionmaker

from app.core.config import settings

engine = create_engine(
    settings.DATABASE_URL,
    pool_pre_ping=True,  # transparently recover dropped connections
    future=True,
)

SessionLocal = sessionmaker(
    bind=engine, autocommit=False, autoflush=False, expire_on_commit=False
)


class Base(DeclarativeBase):
    """Declarative base shared by every ORM model."""


def get_db() -> Generator:
    """Yield a session and guarantee it is closed after the request."""
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
