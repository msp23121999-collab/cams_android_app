# Backend

FastAPI + PostgreSQL. Serves the Android app at `/api/v1/`.

## Setup

```bash
cp .env.example .env          # set JWT_SECRET_KEY and DATABASE_URL

python -m venv .venv
.venv\Scripts\activate        # Windows;  source .venv/bin/activate elsewhere
pip install -r requirements.txt

alembic upgrade head          # build the schema
python -m scripts.seed        # test accounts (development only)

python -m uvicorn app.main:app --host 0.0.0.0 --port 8000
```

Or with Docker:

```bash
docker compose up -d db
docker compose run --rm backend alembic upgrade head
docker compose run --rm backend python -m scripts.seed
docker compose up backend
```

## Layout

| Folder | Contains |
|---|---|
| `app/api/` | Route files. Every endpoint declares which roles may call it. |
| `app/core/` | Config, JWT, password hashing, rate limiting |
| `app/db/` | SQLAlchemy models — one per table |
| `app/schemas/` | Request/response validation |
| `app/services/` | Business logic (fees, leave, attendance) |
| `app/workers/` | Scheduled jobs |
| `alembic/versions/` | Migrations — the schema's source of truth |
| `scripts/seed.py` | Creates the test accounts |
| `tests/` | Regression suite |

## Schema changes

The application creates **nothing** at startup. Migrations are the only thing that shapes
the database.

```bash
alembic revision -m "what you changed"   # create
alembic upgrade head                     # apply
alembic heads                            # must print exactly ONE head
```

If `alembic heads` prints more than one, the chain has branched and must be merged before
deploying — a branched chain applies migrations in an unpredictable order.

## Testing

```bash
pip install -r requirements.txt -r requirements-dev.txt
python -m pytest tests/ -q
```

Some checks only run against PostgreSQL, because SQLite cannot express the failure:

```bash
docker run -d --name cams-pg -e POSTGRES_PASSWORD=verify -e POSTGRES_USER=cams \
  -e POSTGRES_DB=camsverify -p 55432:5432 postgres:16-alpine

TEST_POSTGRES_URL="postgresql://cams:verify@127.0.0.1:55432/camsverify" \
  python -m pytest tests/ -q
```

Run this before every release. See `tests/README.md`.

## Development notes

**Use PostgreSQL locally.** PostgreSQL enforces enum membership, foreign-key ordering and
column types; SQLite silently accepts all three. Developing on SQLite lets schema defects
reach production undetected.

**Authorisation is server-side, always.** Every endpoint checks the caller's role, and
endpoints touching student records also check the relationship — a parent may only read
their linked child, a faculty member only students they teach, advise or mentor.

**Uploads are not public.** Documents are served by `GET /api/v1/files/{path}`, which
authenticates the caller and checks ownership before returning the file.

**Payments are verified server-side.** The client never decides whether a payment
succeeded; the backend verifies the Razorpay signature and only then marks a fee paid.
