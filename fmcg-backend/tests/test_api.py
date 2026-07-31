"""End-to-end API tests with JWT auth and RBAC."""
from datetime import datetime, timezone

from tests.conftest import login, make_user


def test_unauthenticated_is_rejected(client):
    assert client.get("/api/v1/products").status_code == 401
    assert client.get("/api/v1/users").status_code == 401


def test_login_me_and_refresh(client, admin_headers):
    me = client.get("/api/v1/auth/me", headers=admin_headers).json()
    assert me["role"] == "admin"

    # refresh flow
    login_resp = client.post(
        "/api/v1/auth/login",
        data={"username": "admin@example.com", "password": "changeme123"},
    ).json()
    refreshed = client.post(
        "/api/v1/auth/refresh", json={"refresh_token": login_resp["refresh_token"]}
    )
    assert refreshed.status_code == 200
    new_access = refreshed.json()["access_token"]
    ok = client.get("/api/v1/auth/me",
                    headers={"Authorization": f"Bearer {new_access}"})
    assert ok.status_code == 200


def test_bad_password_rejected(client):
    r = client.post(
        "/api/v1/auth/login",
        data={"username": "admin@example.com", "password": "wrong"},
    )
    assert r.status_code == 401


def test_non_admin_cannot_manage_users(client, admin_headers):
    _, rep_headers = make_user(client, admin_headers, "rep1@ex.com", "marketing")
    assert client.get("/api/v1/users", headers=rep_headers).status_code == 403


def test_rep_cannot_create_product(client, admin_headers):
    _, rep_headers = make_user(client, admin_headers, "rep2@ex.com", "marketing")
    r = client.post(
        "/api/v1/products", headers=rep_headers,
        json={"name": "X", "sku": "X-1", "default_price": "1.00"},
    )
    assert r.status_code == 403


def test_product_crud_admin(client, admin_headers):
    r = client.post(
        "/api/v1/products", headers=admin_headers,
        json={"name": "Juice 250ml", "sku": "JUC-250", "default_price": "0.90"},
    )
    assert r.status_code == 201, r.text
    pid = r.json()["id"]
    dup = client.post(
        "/api/v1/products", headers=admin_headers,
        json={"name": "dup", "sku": "JUC-250", "default_price": "1.00"},
    )
    assert dup.status_code == 409
    patched = client.patch(
        f"/api/v1/products/{pid}", headers=admin_headers,
        json={"default_price": "0.95"},
    )
    assert patched.json()["default_price"] == "0.95"


def test_rep_creates_store_and_order_scoped_to_self(client, admin_headers):
    rep, rep_headers = make_user(client, admin_headers, "rep3@ex.com", "marketing")
    other, other_headers = make_user(client, admin_headers, "rep4@ex.com", "marketing")

    store = client.post(
        "/api/v1/stores", headers=rep_headers,
        json={"name": "Rep Mart", "latitude": 27.7, "longitude": 85.3},
    ).json()
    assert store["created_by"] == rep["id"]

    prod = client.post(
        "/api/v1/products", headers=admin_headers,
        json={"name": "Snack", "sku": "SNK-1", "default_price": "2.00"},
    ).json()

    order = client.post(
        "/api/v1/orders", headers=rep_headers,
        json={"store_id": store["id"], "items": [
            {"product_id": prod["id"], "quantity": 3,
             "unit_price": "2.50", "discount": "1.00"}]},
    ).json()
    assert order["created_by"] == rep["id"]
    assert order["total"] == "6.50"

    # the other rep does not see this order
    assert client.get("/api/v1/orders", headers=other_headers).json()["total"] == 0
    # but the owner does
    assert client.get("/api/v1/orders", headers=rep_headers).json()["total"] == 1


def test_full_delivery_workflow(client, admin_headers):
    rep, rep_headers = make_user(client, admin_headers, "rep5@ex.com", "marketing")
    driver, driver_headers = make_user(client, admin_headers, "drv1@ex.com", "delivery")

    store = client.post(
        "/api/v1/stores", headers=rep_headers,
        json={"name": "S", "latitude": 27.7, "longitude": 85.3},
    ).json()
    prod = client.post(
        "/api/v1/products", headers=admin_headers,
        json={"name": "P", "sku": "P-1", "default_price": "1.00"},
    ).json()
    order = client.post(
        "/api/v1/orders", headers=rep_headers,
        json={"store_id": store["id"], "items": [
            {"product_id": prod["id"], "quantity": 2, "unit_price": "1.00"}]},
    ).json()
    oid = order["id"]

    # admin approves; illegal jump rejected
    assert client.patch(f"/api/v1/orders/{oid}/status", headers=admin_headers,
                        json={"status": "delivered"}).status_code == 409
    assert client.patch(f"/api/v1/orders/{oid}/status", headers=admin_headers,
                        json={"status": "approved"}).status_code == 200

    # rep may not assign a delivery (admin only)
    assert client.post("/api/v1/deliveries", headers=rep_headers,
                       json={"order_id": oid, "driver_id": driver["id"]}).status_code == 403

    delivery = client.post("/api/v1/deliveries", headers=admin_headers,
                           json={"order_id": oid, "driver_id": driver["id"]})
    assert delivery.status_code == 201, delivery.text
    did = delivery.json()["id"]
    assert client.get(f"/api/v1/orders/{oid}", headers=admin_headers).json()["status"] == "assigned"

    # driver completes it
    done = client.patch(
        f"/api/v1/deliveries/{did}/status", headers=driver_headers,
        json={"status": "delivered", "proof_photo_url": "file://p.jpg"},
    )
    assert done.status_code == 200
    assert client.get(f"/api/v1/orders/{oid}", headers=admin_headers).json()["status"] == "delivered"

    perf = client.get("/api/v1/reports/delivery-performance", headers=admin_headers).json()
    assert any(row["completed"] == 1 for row in perf)


def test_assign_rejects_non_delivery_user(client, admin_headers):
    rep, rep_headers = make_user(client, admin_headers, "rep6@ex.com", "marketing")
    store = client.post("/api/v1/stores", headers=rep_headers,
                        json={"name": "S", "latitude": 0, "longitude": 0}).json()
    prod = client.post("/api/v1/products", headers=admin_headers,
                       json={"name": "P", "sku": "P-6", "default_price": "1.00"}).json()
    order = client.post("/api/v1/orders", headers=rep_headers,
                        json={"store_id": store["id"], "items": [
                            {"product_id": prod["id"], "quantity": 1, "unit_price": "1.00"}]}).json()
    client.patch(f"/api/v1/orders/{order['id']}/status", headers=admin_headers,
                 json={"status": "approved"})
    # assigning to a marketing user is rejected
    r = client.post("/api/v1/deliveries", headers=admin_headers,
                    json={"order_id": order["id"], "driver_id": rep["id"]})
    assert r.status_code == 422


def test_store_coordinate_validation(client, admin_headers):
    _, rep_headers = make_user(client, admin_headers, "rep7@ex.com", "marketing")
    bad = client.post("/api/v1/stores", headers=rep_headers,
                      json={"name": "Bad", "latitude": 999, "longitude": 0})
    assert bad.status_code == 422


def test_gps_ownership(client, admin_headers):
    rep, rep_headers = make_user(client, admin_headers, "rep8@ex.com", "marketing")
    now = datetime.now(timezone.utc)
    day = now.date().isoformat()

    # rep uploads own points
    ok = client.post(f"/api/v1/gps/{rep['id']}/batch", headers=rep_headers,
                     json={"points": [
                         {"latitude": 27.70, "longitude": 85.30, "recorded_at": now.isoformat()},
                         {"latitude": 27.71, "longitude": 85.31, "recorded_at": now.isoformat()}]})
    assert ok.status_code == 200

    # rep cannot upload for a different user id
    forbidden = client.post("/api/v1/gps/99999/batch", headers=rep_headers,
                            json={"points": [
                                {"latitude": 1, "longitude": 1, "recorded_at": now.isoformat()}]})
    assert forbidden.status_code == 403

    # admin can read the rep's route
    route = client.get(f"/api/v1/gps/{rep['id']}/route?date={day}",
                       headers=admin_headers).json()
    assert route["distance_km"] > 0 and len(route["points"]) == 2


def test_reports_admin_only_and_shapes(client, admin_headers):
    rep, rep_headers = make_user(client, admin_headers, "repR@ex.com", "marketing")
    store = client.post("/api/v1/stores", headers=rep_headers,
                        json={"name": "Report Mart", "latitude": 27.7, "longitude": 85.3}).json()
    prod = client.post("/api/v1/products", headers=admin_headers,
                       json={"name": "Widget", "sku": "W-1", "default_price": "3.00"}).json()
    client.post("/api/v1/orders", headers=rep_headers,
                json={"store_id": store["id"], "items": [
                    {"product_id": prod["id"], "quantity": 4, "unit_price": "3.00"}]})

    # non-admin is forbidden
    assert client.get("/api/v1/reports/sales-by-product", headers=rep_headers).status_code == 403

    # admin gets each report
    by_product = client.get("/api/v1/reports/sales-by-product", headers=admin_headers)
    assert by_product.status_code == 200
    assert any(r["name"] == "Widget" and r["quantity"] == 4 for r in by_product.json())

    by_store = client.get("/api/v1/reports/sales-by-store", headers=admin_headers).json()
    assert any(r["store_id"] == store["id"] and r["total"] == "12.00" for r in by_store)

    summary = client.get("/api/v1/reports/delivery-summary", headers=admin_headers)
    assert summary.status_code == 200 and "delivered" in summary.json()

    assert client.get("/api/v1/reports/distance-by-rep", headers=admin_headers).status_code == 200
    assert client.get("/api/v1/reports/new-stores", headers=admin_headers).json()["count"] >= 1


def test_register_device_token(client, admin_headers):
    _, driver_headers = make_user(client, admin_headers, "drvN@ex.com", "delivery")
    r = client.post("/api/v1/notifications/register", headers=driver_headers,
                    json={"token": "fake-fcm-token-123"})
    assert r.status_code == 200, r.text
    # re-registering the same token is idempotent (upsert)
    r2 = client.post("/api/v1/notifications/register", headers=driver_headers,
                     json={"token": "fake-fcm-token-123"})
    assert r2.status_code == 200
    # unauthenticated is rejected
    assert client.post("/api/v1/notifications/register",
                       json={"token": "x"}).status_code == 401
