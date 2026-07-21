# Database

## Where the real schema lives

**`../backend/alembic/versions/`** — the migration chain is the single source of truth.
The application creates nothing at startup; the schema is whatever the migrations build.

```bash
cd ../backend
alembic upgrade head     # build/update the schema
alembic heads            # must print exactly ONE head
```

Everything in this folder is **reference material**. Nothing here is applied to a database.

## Contents

| Path | What it is |
|---|---|
| `schema/schema.sql` | Full table structure (99 tables), generated **from this project's own migrations**. Read it to understand the data model without running anything. Do not apply it directly. |
| `seed-data/` | Starter data used to populate a fresh database the first time it runs. Contains no real student data. |

## Test accounts

Created by `../backend/scripts/seed.py`:

```bash
cd ../backend
python -m scripts.seed
```

All seeded accounts use the password `Password@123`:

| Role | Email |
|---|---|
| Student | `student@cams.local` (also `student2`–`student5`) |
| Parent | `parent@cams.local`, `parent2@cams.local` |
| Faculty | `faculty@cams.local`, `faculty2`, `faculty3` |
| HOD | `hod@cams.local` |
| Principal | `principal@cams.local` |
| Admin | `admin@cams.local` |

> Never run the seed against a production database — it repopulates tables.

## About the `seed-data/*.json` files

These began as flat files the application read and wrote directly. That does not survive a
container redeploy (the filesystem is replaced), so submitted papers and parent inquiries
were lost on every deploy, and concurrent writes overwrote each other.

They now live in the database (`local_json_db` table). These files are kept only as the
**initial content** used to populate that table the first time it is read.

## Use PostgreSQL, not SQLite

PostgreSQL enforces enum values, foreign-key ordering and column types. SQLite silently
accepts all three. Developing on SQLite lets schema problems reach production undetected,
because nothing locally rejects them.

```
DATABASE_URL=postgresql+asyncpg://user:password@localhost:5432/cams
```

`backend/docker-compose.yml` starts a correctly configured PostgreSQL for you.
