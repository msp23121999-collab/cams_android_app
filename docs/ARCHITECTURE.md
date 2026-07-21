# Architecture

Three pieces: an Android app, a FastAPI backend, and a PostgreSQL database.
Everything lives in this repository.

```
┌─────────────────────────────┐
│  Android app  (app/)        │
│  Kotlin + Jetpack Compose   │
│                             │
│  screens → ViewModel        │
│          → Repository       │
│          → Retrofit         │
└──────────────┬──────────────┘
               │  HTTPS + JWT bearer token
               ▼
┌─────────────────────────────┐
│  Backend  (backend/)        │
│  FastAPI                    │
│                             │
│  api/      routes + auth    │
│  services/ business logic   │
│  db/       models           │
└──────────────┬──────────────┘
               │  SQLAlchemy (async)
               ▼
┌─────────────────────────────┐
│  PostgreSQL                 │
│  schema built by Alembic    │
└─────────────────────────────┘
```

## The Android app

| Folder | Contains |
|---|---|
| `core/network/` | Retrofit service, DTOs, `AuthInterceptor` (attaches the token, refreshes on 401) |
| `core/database/` | Room cache — makes screens load fast. **Not** the real data. |
| `core/navigation/` | Route definitions |
| `core/repository/` | Turns API responses into UI models |
| `core/theme/`, `core/ui/` | Shared styling and widgets |
| `features/<role>/` | One folder per role: `student`, `parent`, `faculty`, `hod`, `principal`, `admin`, plus `auth` and shared feature areas |

Data flows **Screen → ViewModel → Repository → Retrofit → backend**. Screens never call
the network directly.

### Where the backend URL comes from

```
.env  →  build.gradle.kts  →  BuildConfig.API_BASE_URL  →  AppConfig.BASE_URL  →  Retrofit
```

One line in `.env` controls it. Nothing in Kotlin needs editing to repoint the app.

## The backend

| Folder | Contains |
|---|---|
| `app/api/` | Routes. Every endpoint declares the roles allowed to call it. |
| `app/core/` | Config, JWT, password hashing, rate limiting |
| `app/db/` | SQLAlchemy models — one per table |
| `app/schemas/` | Request/response validation |
| `app/services/` | Business logic (fees, leave, attendance) |
| `app/workers/` | Scheduled jobs (backups, daily attendance) |
| `alembic/` | Migrations — **the schema's source of truth** |

### Authentication

Login returns a short-lived access token and a refresh token. The app sends
`Authorization: Bearer <token>` on every call; `AuthInterceptor` transparently refreshes
on a 401 and retries.

Authorisation is enforced **server-side on every endpoint** — the app hiding a button is
never the control. Beyond role checks, endpoints verify ownership: a parent may only read
their linked child, a faculty member only students they teach, advise or mentor.

### Files and uploads

Uploaded documents are **not** served as static files. They go through an authenticated
endpoint (`GET /api/v1/files/{path}`) which checks both the caller's role and their
relationship to the record. Images in the app load through a Coil loader wired to the same
auth interceptor, so they carry the token too.

### Payments

Razorpay. The client never decides whether a payment succeeded: the app sends the gateway
response to the backend, which verifies the signature server-side and only then marks the
fee paid. A partial payment leaves the balance collectable.

## The database

Schema changes happen **only** through Alembic migrations. The application creates nothing
at startup.

```bash
cd backend
alembic upgrade head    # apply
alembic heads           # must print exactly ONE head
```

`database/schema/schema.sql` is a generated, read-only reference so the data model can be
reviewed without running anything.

### Why PostgreSQL matters

PostgreSQL enforces enum membership, foreign-key ordering at table-creation time, and
column types. SQLite accepts all three silently. A schema defect that PostgreSQL rejects
outright can pass unnoticed on SQLite and only appear in production. Develop on PostgreSQL.

## Testing

`backend/tests/` covers the three areas where mistakes are most costly:

| Suite | Protects |
|---|---|
| `test_access_control.py` | Walks every route and fails if any answers an unauthenticated request; plus wrong-role and cross-tenant access |
| `test_schema_integrity.py` | Builds a database from migrations alone and diffs it against the models — missing tables, missing columns, enum drift |
| `test_money.py` | Partial vs full payment, webhook signature verification, parent-child payment ownership |

Each was verified by reintroducing the bug it targets and confirming it fails.
