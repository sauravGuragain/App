"""Unit tests for password hashing and JWT helpers."""
from app.core.security import (
    create_access_token,
    create_refresh_token,
    decode_token,
    hash_password,
    verify_password,
)


def test_password_hash_round_trip():
    h = hash_password("s3cret-pass")
    assert h != "s3cret-pass"
    assert verify_password("s3cret-pass", h)
    assert not verify_password("wrong", h)


def test_access_token_round_trip():
    token = create_access_token("42")
    payload = decode_token(token)
    assert payload is not None
    assert payload["sub"] == "42"
    assert payload["type"] == "access"


def test_refresh_token_type():
    payload = decode_token(create_refresh_token("7"))
    assert payload["type"] == "refresh"


def test_garbage_token_returns_none():
    assert decode_token("not-a-real-token") is None
