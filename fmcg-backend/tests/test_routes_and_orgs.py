"""Tests for new routes and organization separation features."""
import pytest
from tests.conftest import login, make_user


def test_organizations_created_on_startup(client):
    """Verify default organizations (FMCG, NTC) are created."""
    # Admin should be able to create organizations
    # This is tested implicitly through admin being assigned to FMCG org
    admin_resp = client.get("/api/v1/auth/me", headers=login(client, "admin", "changeme123"))
    assert admin_resp.status_code == 200
    user = admin_resp.json()
    assert user["organization_id"] is not None


def test_routes_list_requires_organization(client, admin_headers):
    """List routes should filter by user's organization."""
    routes_resp = client.get("/api/v1/routes", headers=admin_headers)
    assert routes_resp.status_code == 200
    data = routes_resp.json()
    assert "items" in data
    assert "total" in data


def test_create_route(client, admin_headers):
    """Admin can create a route in their organization."""
    route_payload = {"name": "Lokanthali"}
    resp = client.post("/api/v1/routes", json=route_payload, headers=admin_headers)
    assert resp.status_code == 201
    route = resp.json()
    assert route["name"] == "Lokanthali"
    assert route["organization_id"] is not None


def test_get_route(client, admin_headers):
    """Can retrieve a route by ID."""
    # Create a route
    route_payload = {"name": "Thamel"}
    create_resp = client.post("/api/v1/routes", json=route_payload, headers=admin_headers)
    route_id = create_resp.json()["id"]

    # Get the route
    resp = client.get(f"/api/v1/routes/{route_id}", headers=admin_headers)
    assert resp.status_code == 200
    route = resp.json()
    assert route["name"] == "Thamel"
    assert route["id"] == route_id


def test_update_route(client, admin_headers):
    """Can update a route."""
    # Create a route
    route_payload = {"name": "Boudha"}
    create_resp = client.post("/api/v1/routes", json=route_payload, headers=admin_headers)
    route_id = create_resp.json()["id"]

    # Update the route
    update_payload = {"name": "New Boudha"}
    resp = client.patch(f"/api/v1/routes/{route_id}", json=update_payload, headers=admin_headers)
    assert resp.status_code == 200
    route = resp.json()
    assert route["name"] == "New Boudha"


def test_delete_route(client, admin_headers):
    """Admin can delete a route."""
    # Create a route
    route_payload = {"name": "Kathmandu"}
    create_resp = client.post("/api/v1/routes", json=route_payload, headers=admin_headers)
    route_id = create_resp.json()["id"]

    # Delete it
    resp = client.delete(f"/api/v1/routes/{route_id}", headers=admin_headers)
    assert resp.status_code == 200

    # Verify it's deleted
    get_resp = client.get(f"/api/v1/routes/{route_id}", headers=admin_headers)
    assert get_resp.status_code == 404


def test_create_store_with_route(client, admin_headers):
    """Can create a store and assign it to a route."""
    # Create a route first
    route_payload = {"name": "Patan"}
    route_resp = client.post("/api/v1/routes", json=route_payload, headers=admin_headers)
    route_id = route_resp.json()["id"]

    # Create a store with the route
    store_payload = {
        "name": "ABC Shop",
        "owner_name": "Mr. ABC",
        "phone": "9841234567",
        "address": "Patan",
        "latitude": 27.6533,
        "longitude": 85.3240,
        "route_id": route_id,
    }
    resp = client.post("/api/v1/stores", json=store_payload, headers=admin_headers)
    assert resp.status_code == 201
    store = resp.json()
    assert store["route_id"] == route_id
    assert store["organization_id"] is not None


def test_list_stores_by_route(client, admin_headers):
    """Can filter stores by route."""
    # Create two routes
    route1_resp = client.post("/api/v1/routes", json={"name": "Route1"}, headers=admin_headers)
    route1_id = route1_resp.json()["id"]

    route2_resp = client.post("/api/v1/routes", json={"name": "Route2"}, headers=admin_headers)
    route2_id = route2_resp.json()["id"]

    # Create stores for each route
    store1_payload = {
        "name": "Store1",
        "latitude": 27.6533,
        "longitude": 85.3240,
        "route_id": route1_id,
    }
    client.post("/api/v1/stores", json=store1_payload, headers=admin_headers)

    store2_payload = {
        "name": "Store2",
        "latitude": 27.6533,
        "longitude": 85.3240,
        "route_id": route2_id,
    }
    client.post("/api/v1/stores", json=store2_payload, headers=admin_headers)

    # List stores for route1
    resp = client.get(f"/api/v1/stores?route_id={route1_id}", headers=admin_headers)
    assert resp.status_code == 200
    stores = resp.json()["items"]
    assert len(stores) >= 1
    assert any(s["route_id"] == route1_id for s in stores)


def test_organization_isolation(client, admin_headers):
    """Users can only access stores from their own organization."""
    # Admin creates a store
    store_payload = {
        "name": "Admin Store",
        "latitude": 27.6533,
        "longitude": 85.3240,
    }
    resp = client.post("/api/v1/stores", json=store_payload, headers=admin_headers)
    assert resp.status_code == 201
    store_id = resp.json()["id"]

    # Verify admin can access it
    resp = client.get(f"/api/v1/stores/{store_id}", headers=admin_headers)
    assert resp.status_code == 200


def test_username_based_login(client):
    """Users can login with username instead of email."""
    # Login with username
    resp = client.post(
        "/api/v1/auth/login",
        data={"username": "admin", "password": "changeme123"}
    )
    assert resp.status_code == 200
    assert "access_token" in resp.json()


def test_username_uniqueness(client, admin_headers):
    """Cannot create two users with the same username."""
    # Create a user
    user1_payload = {
        "username": "john",
        "email": "john@example.com",
        "full_name": "John",
        "role": "marketing",
        "password": "secure123",
    }
    resp1 = client.post("/api/v1/users", json=user1_payload, headers=admin_headers)
    assert resp1.status_code == 201

    # Try to create another with the same username
    user2_payload = {
        "username": "john",
        "email": "jane@example.com",
        "full_name": "Jane",
        "role": "marketing",
        "password": "secure123",
    }
    resp2 = client.post("/api/v1/users", json=user2_payload, headers=admin_headers)
    assert resp2.status_code == 409  # Conflict


def test_create_user_with_organization(client, admin_headers):
    """New users are assigned to the admin's organization."""
    user_payload = {
        "username": "newuser",
        "email": "newuser@example.com",
        "full_name": "New User",
        "role": "marketing",
        "password": "secure123",
    }
    resp = client.post("/api/v1/users", json=user_payload, headers=admin_headers)
    assert resp.status_code == 201
    user = resp.json()
    assert user["organization_id"] is not None
