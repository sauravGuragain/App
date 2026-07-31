# FMCG Sales & Delivery Platform

A production-oriented FMCG distribution system built entirely on **free and
open-source technology**. Two deliverables live in one repository:

| Folder          | What it is                                             |
|-----------------|--------------------------------------------------------|
| `fmcg-backend`  | FastAPI + PostgreSQL REST API (Clean, modular layers)  |
| `fmcg-android`  | Kotlin + Jetpack Compose app (Clean Architecture + MVVM)|

No paid APIs are used anywhere: maps are **OpenStreetMap via OSMDroid**,
push is **Firebase Cloud Messaging (free tier)**, everything else is
self-hosted or OSS.

---

## Roles

- **Admin** — manages users, products, stores, orders; assigns deliveries; sees live locations, routes and reports.
- **Marketing Rep** — starts/ends a workday, GPS route is tracked, creates stores, places orders with customer-specific pricing.
- **Delivery Staff** — receives assigned deliveries, navigates, uploads proof photo, marks delivered.

---

## Backend architecture (`fmcg-backend`)

Layered so that a request flows in one direction and each layer has a single job:

```
app/
├── main.py            App factory: CORS, router mount, health check
├── core/              Cross-cutting concerns
│   ├── config.py      Settings from env (pydantic-settings)
│   ├── database.py    Engine, SessionLocal, Base, get_db dependency
│   └── security.py    Password hashing + JWT create/decode
├── models/            SQLAlchemy ORM tables            (Phase 3)
├── schemas/           Pydantic request/response models (Phase 2+)
├── crud/              DB read/write functions per entity
├── services/          Business rules (pricing, assignment, reports)
├── api/v1/
│   ├── router.py      Aggregates every endpoint module
│   └── endpoints/     One module per resource (auth, stores, orders…)
└── utils/             Helpers (geo distance, pagination…)
alembic/               Versioned schema migrations         (Phase 3)
```

**Rule:** `endpoints → services → crud → models`. Endpoints never touch the
ORM directly; services hold the business logic; crud is the only layer that
runs queries.

## Android architecture (`fmcg-android`)

Clean Architecture with three concentric layers plus MVVM in the presentation
layer. `domain` depends on nothing; `data` and `presentation` depend inward.

```
com.fmcg.app/
├── di/                 Hilt modules (network, db, repository bindings)
├── domain/             Pure Kotlin — no Android imports
│   ├── model/          Business entities
│   ├── repository/     Repository interfaces
│   └── usecase/        One class per business action
├── data/
│   ├── local/          Room: entities + DAOs (offline source of truth)
│   ├── remote/         Retrofit APIs + DTOs
│   ├── mapper/         DTO ⇆ entity ⇆ domain conversion
│   ├── repository/     Repository implementations (offline-first)
│   └── sync/           WorkManager sync workers          (Phase 11)
├── service/            Foreground GPS tracking service    (Phase 6)
├── presentation/
│   ├── navigation/     Navigation-Compose graph + routes
│   ├── common/         Theme (M3 light/dark), shared components
│   ├── auth/           Login screen + ViewModel
│   ├── marketing/      Rep dashboard, stores, orders, route
│   ├── delivery/       Delivery list, detail, proof capture
│   └── admin/          Management dashboards + reports
└── util/               Result wrapper, connectivity, permissions
```

**Offline-first flow:** UI observes Room (a `Flow`). The repository writes
locally first and enqueues a sync job; when connectivity returns, WorkManager
pushes/pulls with the backend and resolves conflicts. The UI never blocks on
the network.

---

## Build roadmap (15 phases)

| Phase | Scope | Status |
|------:|-------|--------|
| 1  | System architecture & folder structure | ✅ done |
| 2  | Backend API skeleton & schemas | ✅ done |
| 3  | Database schema & Alembic migrations | ✅ done |
| 4  | JWT authentication & RBAC | ✅ done |
| 5  | Android project setup (theme, nav, DI, network) | ✅ done |
| 6  | GPS tracking (foreground service) | ✅ done |
| 7  | Maps (OSMDroid, tap-to-pin) | ✅ done |
| 8  | Store management | ✅ done |
| 9  | Orders (custom pricing, status workflow) | ✅ done |
| 10 | Delivery module | ✅ done |
| 11 | Offline synchronisation | ✅ done |
| 12 | Reports | ✅ done |
| 13 | Notifications (FCM) | ✅ done |
| 14 | Testing | ✅ done |
| 15 | Deployment guide | ✅ done |

Each phase is implemented completely before the next begins.

---

## Run the backend locally

```bash
cd fmcg-backend
cp .env.example .env          # then edit SECRET_KEY
docker compose up --build     # Postgres + API on http://localhost:8000
# Swagger UI: http://localhost:8000/docs
```

Migrations run automatically in the compose `backend` command. To run them by
hand: `alembic upgrade head` (create a new one with
`alembic revision --autogenerate -m "message"`). Tests run against SQLite with
foreign keys enforced: `pytest`.

Or without Docker:

```bash
cd fmcg-backend
python -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
# start a local Postgres, set POSTGRES_HOST=localhost in .env
uvicorn app.main:app --reload
```

## Open the Android app

Open `fmcg-android` in Android Studio (Ladybug or newer), let Gradle sync, and
run on an emulator. The debug build points at `http://10.0.2.2:8000/`, which is
the host machine's `localhost` from inside the emulator.

## Enable push notifications (optional)

FCM ships wired but inactive so the app builds without a Firebase account. To turn it on:
1. Create a Firebase project and add an Android app with package `com.fmcg.app`.
2. Download `google-services.json` into `fmcg-android/app/` (a template is provided).
3. Uncomment `alias(libs.plugins.google.services)` in `app/build.gradle.kts`.
4. On the backend, set `FCM_CREDENTIALS_FILE` to a service-account JSON path.

Without these, token registration and sends are safe no-ops.

## Tests

- **Backend** — `cd fmcg-backend && pytest` (30 tests: end-to-end API + RBAC + auth, order pricing/workflow units, geo, security, media upload, reports, notifications).
- **Android** — `cd fmcg-android && ./gradlew test` runs JVM unit tests for enums, `LoginViewModel`, and `OrderCreateViewModel` using fake repositories (`app/src/test`).
