"""Seed the first admin so the admin-only user API is reachable on a fresh DB.

Idempotent: only creates the admin when the users table is empty. Credentials
come from settings (override via env); change the password immediately in any
real deployment. Also creates default organizations (NTC and FMCG).
"""
from sqlalchemy import func, select
from sqlalchemy.orm import Session

from app.core.config import settings
from app.core.security import hash_password
from app.models.organization import Organization
from app.models.user import User
from app.schemas.common import UserRole


def ensure_organizations(db: Session) -> Organization:
    """Ensure default organizations exist and return the FMCG organization."""
    # Check if FMCG organization exists
    fmcg = db.scalar(
        select(Organization).where(Organization.name == "FMCG")
    )
    if fmcg is None:
        fmcg = Organization(name="FMCG")
        db.add(fmcg)
        db.flush()

    # Ensure NTC organization exists
    ntc = db.scalar(
        select(Organization).where(Organization.name == "NTC")
    )
    if ntc is None:
        ntc = Organization(name="NTC")
        db.add(ntc)
        db.flush()

    db.commit()
    return fmcg


def ensure_first_admin(db: Session) -> bool:
    count = db.scalar(select(func.count()).select_from(User)) or 0
    if count > 0:
        return False

    # Ensure organizations exist
    fmcg_org = ensure_organizations(db)

    db.add(
        User(
            username="admin",
            email=settings.FIRST_ADMIN_EMAIL,
            full_name=settings.FIRST_ADMIN_NAME,
            hashed_password=hash_password(settings.FIRST_ADMIN_PASSWORD),
            role=UserRole.ADMIN,
            organization_id=fmcg_org.id,
            is_active=True,
        )
    )
    db.commit()
    return True
