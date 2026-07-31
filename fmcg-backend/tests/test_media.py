"""Media upload endpoint tests."""
import base64

# Smallest valid 1x1 PNG.
_PNG = base64.b64decode(
    "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
)


def test_upload_requires_auth(client):
    r = client.post("/api/v1/media/upload",
                    files={"file": ("x.png", _PNG, "image/png")})
    assert r.status_code == 401


def test_upload_image_returns_url(client, admin_headers):
    r = client.post(
        "/api/v1/media/upload",
        headers=admin_headers,
        files={"file": ("proof.png", _PNG, "image/png")},
    )
    assert r.status_code == 200, r.text
    assert r.json()["url"].startswith("/media/")


def test_upload_rejects_non_image(client, admin_headers):
    r = client.post(
        "/api/v1/media/upload",
        headers=admin_headers,
        files={"file": ("note.txt", b"hello", "text/plain")},
    )
    assert r.status_code == 415
