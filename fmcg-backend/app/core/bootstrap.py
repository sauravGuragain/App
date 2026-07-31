"""Seed the first admin so the admin-only user API is reachable on a fresh DB.

Idempotent: only creates the admin when the users table is empty. Credentials
come from settings (override via env); change the password immediately in any
real deployment.
"""
from sqlalchemy import func, select
from sqlalchemy.orm import Session

from app.core.config import settings
from app.core.security import hash_password
from app.models.user import User
from app.schemas.common import UserRole


def ensure_first_admin(db: Session) -> bool:
    count = db.scalar(select(func.count()).select_from(User)) or 0
    if count > 0:
        return False
    db.add(
        User(
            email=settings.FIRST_ADMIN_EMAIL,
            full_name=settings.FIRST_ADMIN_NAME,
            hashed_password=hash_password(settings.FIRST_ADMIN_PASSWORD),
            role=UserRole.ADMIN,
            is_active=True,
        )
    )
    db.commit()
    return True
