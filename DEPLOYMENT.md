# Deployment guide

Everything here uses free / open-source tooling. Two things get deployed: the
**backend** (FastAPI + PostgreSQL) and the **Android app**. All values below are
the project defaults — change the secrets before any real use.

---

## 1. Prerequisites (all free)

- **Docker + Docker Compose** — the easiest way to run the backend and database.
- Or, without Docker: **Python 3.12** and **PostgreSQL 16**.
- **Android Studio** (Ladybug or newer) with **JDK 17** to build the app.
- Optional: a **Firebase** project (free Spark plan) to enable push notifications.

---

## 2. Backend

### Option A — Docker Compose (recommended)

```bash
cd fmcg-backend
cp .env.example .env          # then edit SECRET_KEY and the admin password
docker compose up --build
```

This starts two services:

| Service   | Image               | Port           |
|-----------|---------------------|----------------|
| `db`      | `postgres:16-alpine`| `5432`         |
| `backend` | built from `Dockerfile` (`python:3.12-slim`) | `8000` |

On start the backend container runs `alembic upgrade head` (applying both
migrations — the initial schema and the device-tokens table) and then serves
`uvicorn` on port 8000. Postgres data persists in the `pgdata` volume; uploaded
media persists in the container's `media/` directory (see §5).

### Option B — Bare metal (no Docker)

```bash
cd fmcg-backend
python -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt

# Point DATABASE_URL at your Postgres, and set a SECRET_KEY:
export DATABASE_URL="postgresql+psycopg://fmcg:fmcg_secret@localhost:5432/fmcg"
export SECRET_KEY="$(python -c 'import secrets; print(secrets.token_hex(32))')"

alembic upgrade head
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

### First admin

On first startup the app seeds one admin from these settings (defaults shown):

```
FIRST_ADMIN_EMAIL=admin@example.com
FIRST_ADMIN_PASSWORD=changeme123
FIRST_ADMIN_NAME=System Admin
```

**Change the password immediately.** Log in via `POST /api/v1/auth/login`, then
create the real users through `POST /api/v1/users` (admin-only). The admin can
create marketing reps and delivery staff, who can then log in from the app.

### Verify it's up

```bash
curl http://localhost:8000/health          # -> {"status": "ok", ...}
```

Interactive API docs (Swagger UI) are served at `http://localhost:8000/docs`.
All application endpoints live under the `/api/v1` prefix.

---

## 3. Configuration reference (`.env`)

| Key | Purpose | Default |
|-----|---------|---------|
| `SECRET_KEY` | JWT signing key — **set this** | (none) |
| `POSTGRES_USER` / `POSTGRES_PASSWORD` / `POSTGRES_DB` | database credentials | `fmcg` / `fmcg_secret` / `fmcg` |
| `POSTGRES_HOST` / `POSTGRES_PORT` | database location | `db` (compose) / `5432` |
| `ACCESS_TOKEN_EXPIRE_MINUTES` | access-token lifetime | `60` |
| `REFRESH_TOKEN_EXPIRE_DAYS` | refresh-token lifetime | (see `.env.example`) |
| `BACKEND_CORS_ORIGINS` | allowed web origins | (see `.env.example`) |
| `FIRST_ADMIN_EMAIL` / `_PASSWORD` / `_NAME` | seeded admin | `admin@example.com` / `changeme123` |
| `MEDIA_DIR` / `MEDIA_URL_PREFIX` | uploaded-file storage / URL | `media` / `/media` |
| `FCM_CREDENTIALS_FILE` | Firebase service-account JSON path (empty = notifications off) | `""` |

---

## 4. Android app

### Point the app at your backend

The backend URL is a `BuildConfig` field in `fmcg-android/app/build.gradle.kts`:

```kotlin
buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8000/\"")
```

- **Emulator:** `http://10.0.2.2:8000/` already works — `10.0.2.2` is the host
  machine as seen from the Android emulator.
- **Physical device:** change it to your computer's LAN IP, e.g.
  `http://192.168.1.20:8000/`, and add that same IP as a `<domain>` in
  `app/src/main/res/xml/network_security_config.xml`. Cleartext HTTP is allowed
  only for these dev hosts; any other host must use HTTPS.

### Build, install, test

```bash
cd fmcg-android
./gradlew assembleDebug        # builds app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug         # install to a connected device/emulator
./gradlew test                 # JVM unit tests
```

Requirements: JDK 17, `minSdk 24`, `targetSdk 35`, application id `com.fmcg.app`.

---

## 5. Media files

Uploaded delivery-proof / store photos are stored on disk under `MEDIA_DIR`
(default `media/`) and served from `MEDIA_URL_PREFIX` (`/media`). Under Docker,
add a volume so they survive container rebuilds, and in production serve
`/media` directly from your reverse proxy or move to object storage (S3 /
Backblaze B2 / Cloudflare R2) — the endpoint returns a relative URL the app
resolves against `BASE_URL`.

---

## 6. Enable push notifications (optional)

Shipped wired but inactive so the app builds with no Firebase account.

1. Create a Firebase project; add an Android app with package `com.fmcg.app`.
2. Download `google-services.json` into `fmcg-android/app/` (template provided).
3. Uncomment `alias(libs.plugins.google.services)` in `app/build.gradle.kts`.
4. Backend: set `FCM_CREDENTIALS_FILE` to a downloaded service-account JSON path.

With those in place, drivers are notified on delivery assignment and reps on
delivery completion. Without them, token registration and sends are safe no-ops.

---

## 7. Running the test suites

```bash
cd fmcg-backend && pytest            # 30 tests
cd fmcg-android && ./gradlew test    # ViewModel / enum unit tests
```

---

## 8. Production notes

- **App server:** run uvicorn with multiple workers or behind Gunicorn, e.g.
  `gunicorn app.main:app -k uvicorn.workers.UvicornWorker -w 4`. FastAPI is
  async and handles many concurrent users per worker; scale workers to CPU
  cores and put several instances behind a load balancer if needed.
- **HTTPS:** terminate TLS at a reverse proxy (Caddy gives automatic Let's
  Encrypt certificates for free; Nginx works too) and proxy to port 8000. Then
  set the app's `BASE_URL` to your `https://` domain — no cleartext exception
  needed.
- **Database:** use managed or self-hosted Postgres; schedule `pg_dump` backups
  and keep the `pgdata` volume on durable storage.
- **Free hosting options:** a small VPS (Oracle Cloud always-free, a low-tier
  Hetzner/DigitalOcean droplet) running Docker Compose, or a free-tier PaaS
  (Render / Railway / Fly.io) for the backend plus their free Postgres.
- **Secrets:** never commit `.env`; rotate `SECRET_KEY` and the admin password.

---

## 9. Security checklist before go-live

- [ ] Change `SECRET_KEY` to a strong random value.
- [ ] Change the seeded admin password (and email).
- [ ] Set real `BACKEND_CORS_ORIGINS` (don't leave it wide open).
- [ ] Serve over HTTPS and switch `BASE_URL` to `https://`.
- [ ] Restrict Postgres network access; use a strong DB password.
- [ ] Keep short access-token TTLs (logout is client-side; see limitations).

---

## 10. Known limitations (tracked during the build)

- **Offline order for an offline-created store** isn't supported — an order
  can't reference a store that hasn't synced and been assigned a server id.
- **Order and delivery reads are network-only** — only *writes* go through the
  offline outbox; only store reads are fully offline-capable.
- **"Sales by area" and "visit frequency"** aren't modelled — stores have a
  free-text address (no structured area) and there's no Visit entity. The
  closest shipped report is *sales by store*.
- **No server-side logout / token revocation** — JWTs are stateless; mitigate
  with short access-token lifetimes.
- **Android is not compiled in this environment** — it's verified structurally
  (package/import/symbol/duplicate resolution). A real Gradle build in Android
  Studio is the final check.
- **FCM is inactive** until a real `google-services.json` and service-account
  credentials are supplied (§6).

---

## 11. Push to GitHub

```bash
cd fmcg-platform
git init && git add . && git commit -m "FMCG platform"
git branch -M main
git remote add origin https://github.com/<you>/<repo>.git
git push -u origin main
```

A `.gitignore` is included (Python caches, `.env`, `media/`, Gradle/IDE files,
`google-services.json`).
