# OM Services - API Changes Reference

## Authentication (Changed)

### Login Endpoint
**Endpoint:** `POST /api/v1/auth/login`

**Before:**
```bash
curl -X POST /api/v1/auth/login \
  -d "username=user@example.com&password=secret"
```

**After:**
```bash
curl -X POST /api/v1/auth/login \
  -d "username=john_doe&password=secret"
```

**Response (Same):**
```json
{
  "access_token": "eyJ...",
  "refresh_token": "eyJ...",
  "token_type": "bearer"
}
```

---

## Routes Management (New)

### Create Route
**Endpoint:** `POST /api/v1/routes`

**Request:**
```bash
curl -X POST http://localhost:8000/api/v1/routes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Lokanthali Route"
  }'
```

**Response:**
```json
{
  "id": 1,
  "name": "Lokanthali Route",
  "organization_id": 1,
  "created_by": 2,
  "created_at": "2026-09-18T12:31:00"
}
```

### List Routes
**Endpoint:** `GET /api/v1/routes`

**Parameters:**
- `skip` (optional, default=0)
- `limit` (optional, default=50)
- `search` (optional) - Filter by name

**Request:**
```bash
curl http://localhost:8000/api/v1/routes \
  -H "Authorization: Bearer $TOKEN"

# Filter by name
curl "http://localhost:8000/api/v1/routes?search=Lokanthali" \
  -H "Authorization: Bearer $TOKEN"
```

**Response:**
```json
{
  "items": [
    {
      "id": 1,
      "name": "Lokanthali Route",
      "organization_id": 1,
      "created_by": 2,
      "created_at": "2026-09-18T12:31:00"
    }
  ],
  "total": 1,
  "skip": 0,
  "limit": 50
}
```

### Get Route
**Endpoint:** `GET /api/v1/routes/{route_id}`

**Request:**
```bash
curl http://localhost:8000/api/v1/routes/1 \
  -H "Authorization: Bearer $TOKEN"
```

### Update Route
**Endpoint:** `PATCH /api/v1/routes/{route_id}`

**Request:**
```bash
curl -X PATCH http://localhost:8000/api/v1/routes/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "New Route Name"
  }'
```

### Delete Route
**Endpoint:** `DELETE /api/v1/routes/{route_id}`

**Request:**
```bash
curl -X DELETE http://localhost:8000/api/v1/routes/1 \
  -H "Authorization: Bearer $TOKEN"
```

**Response:**
```json
{
  "message": "Route deleted"
}
```

---

## Stores Management (Updated)

### Create Store (With Route)
**Endpoint:** `POST /api/v1/stores`

**Before:**
```json
{
  "name": "ABC Store",
  "owner_name": "John",
  "phone": "9841234567",
  "address": "Thamel",
  "latitude": 27.7172,
  "longitude": 85.3240,
  "notes": "Near main road"
}
```

**After (With Route Support):**
```json
{
  "name": "ABC Store",
  "owner_name": "John",
  "phone": "9841234567",
  "address": "Thamel",
  "latitude": 27.7172,
  "longitude": 85.3240,
  "notes": "Near main road",
  "route_id": 1
}
```

**Response Now Includes:**
```json
{
  "id": 1,
  "name": "ABC Store",
  "owner_name": "John",
  "phone": "9841234567",
  "address": "Thamel",
  "latitude": 27.7172,
  "longitude": 85.3240,
  "notes": "Near main road",
  "photo_url": null,
  "route_id": 1,
  "organization_id": 1,
  "created_by": 2,
  "created_at": "2026-09-18T12:31:00"
}
```

### List Stores (With Route Filter)
**Endpoint:** `GET /api/v1/stores`

**New Parameters:**
- `route_id` (optional) - Filter stores by route

**Requests:**
```bash
# List all stores in user's organization
curl http://localhost:8000/api/v1/stores \
  -H "Authorization: Bearer $TOKEN"

# Filter by route
curl "http://localhost:8000/api/v1/stores?route_id=1" \
  -H "Authorization: Bearer $TOKEN"

# Search and filter
curl "http://localhost:8000/api/v1/stores?search=ABC&route_id=1" \
  -H "Authorization: Bearer $TOKEN"
```

### Get Store
**Endpoint:** `GET /api/v1/stores/{store_id}`

**Response Now Includes:**
```json
{
  "id": 1,
  "name": "ABC Store",
  "route_id": 1,
  "organization_id": 1,
  ...
}
```

### Update Store
**Endpoint:** `PATCH /api/v1/stores/{store_id}`

**Request:**
```bash
curl -X PATCH http://localhost:8000/api/v1/stores/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "route_id": 2,
    "owner_name": "Jane",
    "phone": "9849876543"
  }'
```

---

## User Management (Updated)

### Create User
**Endpoint:** `POST /api/v1/users`

**Before:**
```json
{
  "email": "john@example.com",
  "full_name": "John Doe",
  "role": "marketing",
  "password": "secure123"
}
```

**After (Username Required):**
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "full_name": "John Doe",
  "role": "marketing",
  "phone": "+977-9841234567",
  "password": "secure123"
}
```

**Response Now Includes:**
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "full_name": "John Doe",
  "role": "marketing",
  "phone": "+977-9841234567",
  "organization_id": 1,
  "is_active": true,
  "created_at": "2026-09-18T12:31:00"
}
```

### Get Current User
**Endpoint:** `GET /api/v1/auth/me`

**Response Now Includes:**
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "full_name": "John Doe",
  "role": "marketing",
  "phone": "+977-9841234567",
  "organization_id": 1,
  "is_active": true,
  "created_at": "2026-09-18T12:31:00"
}
```

---

## Organization (Automatic)

### How Organizations Work

**Automatic Assignment:**
- Every user belongs to exactly one organization
- Admin who creates a user determines the organization
- User inherits admin's organization

**Enforcement:**
- Users can only see stores/routes in their organization
- Attempting to access another org's resources returns 403 Forbidden
- Creates complete data isolation between NTC and FMCG

**Example:** If admin belongs to FMCG:
```bash
# Admin creates a user → user assigned to FMCG
POST /api/v1/users {"username": "rep1", ...}
→ response includes "organization_id": 1 (FMCG)

# User can only see FMCG stores
GET /api/v1/stores
→ Returns only FMCG stores

# User's routes are FMCG only
GET /api/v1/routes
→ Returns only FMCG routes
```

---

## Error Responses

### 403 Forbidden - Organization Access Denied
```json
{
  "detail": "Access denied",
  "code": "error"
}
```
Occurs when trying to access resources from different organization.

### 409 Conflict - Username Already Taken
```json
{
  "detail": "Username already taken",
  "code": "error"
}
```

### 409 Conflict - Email Already Registered
```json
{
  "detail": "Email already registered",
  "code": "error"
}
```

### 401 Unauthorized - Invalid Login
```json
{
  "detail": "Incorrect username or password",
  "code": "error"
}
```

---

## Migration Notes

### For Existing Deployments

1. **Before Migration:**
   - Users can still login with email-based approach
   - Note down usernames that will be generated

2. **During Migration:**
   - Usernames auto-generated from emails
   - Organizations created (NTC, FMCG)
   - Existing users assigned to default org

3. **After Migration:**
   - Users must use username for login
   - Email still required and validated
   - Phone optional but can be stored

### Backward Compatibility
- ✅ Email still stored and unique
- ✅ Phone field works as before
- ✅ All existing stores/orders preserved
- ✅ Photos continue to work
- ⚠️ Login method changed from email to username

---

## Testing the Changes

### Test Routes CRUD
```bash
# Create route
curl -X POST http://localhost:8000/api/v1/routes \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name": "Test Route"}'

# List routes
curl http://localhost:8000/api/v1/routes \
  -H "Authorization: Bearer $TOKEN"
```

### Test Organization Isolation
```bash
# Login as NTC admin
TOKEN_NTC=$(curl -X POST http://localhost:8000/api/v1/auth/login \
  -d "username=ntc_admin&password=..." | jq -r '.access_token')

# Try to access FMCG store (should fail)
curl http://localhost:8000/api/v1/stores/1 \
  -H "Authorization: Bearer $TOKEN_NTC"
# Response: 403 Forbidden
```

### Test Username Login
```bash
curl -X POST http://localhost:8000/api/v1/auth/login \
  -d "username=john_doe&password=secure123"
```
