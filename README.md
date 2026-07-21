# CAMS Enterprise — Mobile App

A campus management system for a law college. This repository is **self-contained**:
the Android app, the backend that serves it, and the database reference are all here.

```
cams-mobile-app/
├── app/          Android app  (Kotlin + Jetpack Compose)
├── backend/      API server   (FastAPI + PostgreSQL)
├── database/     Schema reference and seed data
└── docs/         Architecture and configuration reference
```

Six roles are supported: **Student, Parent, Faculty, HOD, Principal, Admin.**

---

## Quick start

### 1. Backend

```bash
cd backend
cp .env.example .env                 # then set JWT_SECRET_KEY and DATABASE_URL

python -m venv .venv
.venv\Scripts\activate               # Windows;  source .venv/bin/activate elsewhere
pip install -r requirements.txt

alembic upgrade head                 # build the database schema
python -m scripts.seed               # create test accounts (development only)

python -m uvicorn app.main:app --host 0.0.0.0 --port 8000
```

The API is now at `http://localhost:8000/api/v1/`.

Prefer Docker:

```bash
cd backend
docker compose up -d db
docker compose run --rm backend alembic upgrade head
docker compose run --rm backend python -m scripts.seed
docker compose up backend
```

### 2. Android app

```bash
cp .env.example .env                             # set API_BASE_URL
cp local.properties.example local.properties     # set sdk.dir
```

Open the project root in Android Studio and press **Run**, or:

```bash
gradlew.bat assembleDebug
```

### 3. Log in

All seeded accounts use the password `Password@123`:

| Role | Email |
|---|---|
| Student | `student@cams.local` |
| Parent | `parent@cams.local` |
| Faculty | `faculty@cams.local` |
| HOD | `hod@cams.local` |
| Principal | `principal@cams.local` |
| Admin | `admin@cams.local` |

---

## How the pieces connect

```
  app/.env
  API_BASE_URL ──────────────► backend  (port 8000, serves /api/v1/)
                                  │
                               backend/.env
                               DATABASE_URL ──────► PostgreSQL
```

Change where the app points by editing **one line** in `.env` — no Kotlin changes needed.
See [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) for the full picture.

---

## Things worth knowing before you change anything

**The database schema is owned entirely by migrations.** The application creates
nothing at startup. Run `alembic upgrade head` before starting the app, and on
every deploy that ships a migration. `alembic heads` must always print exactly one head.

**Use PostgreSQL, not SQLite.** PostgreSQL enforces enum values, foreign-key ordering
and column types; SQLite silently accepts all three. Developing on SQLite lets schema
defects reach production undetected. `backend/docker-compose.yml` gives you a correct
PostgreSQL in one command.

**Run the tests before you push.** They are not decorative — each one locks in a
specific failure and has been verified to fail when that fix is reverted:

```bash
cd backend
pip install -r requirements.txt -r requirements-dev.txt
TEST_POSTGRES_URL="postgresql://user:pw@localhost:5432/cams" python -m pytest tests/ -q
```

`tests/test_access_control.py` walks **every** registered route and fails if any of them
answers an unauthenticated request, so new endpoints are covered automatically.

**Never commit secrets.** `.env`, `local.properties` and `*.jks` are gitignored.
Every `.example` file contains placeholders only.

---

## Documentation

| File | What it covers |
|---|---|
| [`DEPLOYMENT.md`](DEPLOYMENT.md) | Full setup, environment variables, production checklist |
| [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) | How app, backend and database fit together |
| [`docs/ENV_VARS.md`](docs/ENV_VARS.md) | Every configuration variable explained |
| [`backend/README.md`](backend/README.md) | Backend layout and workflows |
| [`database/README.md`](database/README.md) | Schema reference and seed data |

---

## A note on the web app

A separate CAMS **web** application exists and was used as the reference for the database
schema and API contract during development. It is **not part of this repository** and must
never be copied into it. This repository contains only the Android app, its backend, and
the database files.
