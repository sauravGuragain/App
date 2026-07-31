"""Test harness: SQLite DB (FK on), get_db override, seeded first admin.

TestClient is used WITHOUT its context manager so the app's Postgres startup
seeder doesn't run — the admin is seeded directly into the test DB instead.
"""
import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine, event
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import StaticPool

from app.core.bootstrap import ensure_first_admin
from app.core.config import settings
from app.core.database import Base, get_db
from app.main import app


@pytest.fixture()
def client():
    engine = create_engine(
        "sqlite://",
        connect_args={"check_same_thread": False},
        poolclass=StaticPool,
    )

    @event.listens_for(engine, "connect")
    def _fk_on(dbapi_conn, _):
        dbapi_conn.execute("PRAGMA foreign_keys=ON")

    Base.metadata.create_all(engine)
    TestingSession = sessionmaker(bind=engine, autoflush=False, expire_on_commit=False)
    with TestingSession() as db:
        ensure_first_admin(db)

    def _override_get_db():
        db = TestingSession()
        try:
            yield db
        finally:
            db.close()

    app.dependency_overrides[get_db] = _override_get_db
    yield TestClient(app)
    app.dependency_overrides.clear()
    Base.metadata.drop_all(engine)


# ---- helpers shared by tests ----
def login(client, email, password):
    r = client.post(
        "/api/v1/auth/login", data={"username": email, "password": password}
    )
    assert r.status_code == 200, r.text
    return {"Authorization": f"Bearer {r.json()['access_token']}"}


def make_user(client, admin_headers, email, role, pw="supersecret123"):
    r = client.post(
        "/api/v1/users",
        headers=admin_headers,
        json={"email": email, "full_name": email.split("@")[0],
              "role": role, "password": pw},
    )
    assert r.status_code == 201, r.text
    return r.json(), login(client, email, pw)


@pytest.fixture()
def admin_headers(client):
    return login(client, settings.FIRST_ADMIN_EMAIL, settings.FIRST_ADMIN_PASSWORD)
