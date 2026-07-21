# Backend test suite

```bash
pip install -r requirements.txt -r requirements-dev.txt
python -m pytest tests/ -q
```

Tests build their own throwaway SQLite database in a temp directory; they never
touch `cams.db`.

## PostgreSQL-only checks

One check is skipped unless you point it at a real PostgreSQL instance. Run it
before any release — production is PostgreSQL, and several bug classes are
**invisible on SQLite**:

```bash
docker run -d --name cams-pg -e POSTGRES_PASSWORD=verify -e POSTGRES_USER=cams \
  -e POSTGRES_DB=camsverify -p 55432:5432 postgres:16-alpine

TEST_POSTGRES_URL="postgresql://cams:verify@127.0.0.1:55432/camsverify" \
  python -m pytest tests/ -q
```

## What each suite protects

| File | Catches |
|---|---|
| `test_access_control.py` | Any `/api/v1` route reachable **unauthenticated**, wrong-role access, and cross-tenant IDOR (parent reading another family's child, HOD reading another department, faculty roster leaking Aadhaar/income/medical). |
| `test_schema_integrity.py` | Builds a database from `alembic upgrade head` **only** and diffs it against the models — missing tables, missing columns, legacy `NOT NULL` columns that block inserts, and (on PostgreSQL) enum values the code writes but the database rejects. |
| `test_money.py` | Partial payments must not close a fee record; settling the balance must; `due_date` stays present-but-null when nothing is owed; the Razorpay webhook rejects forged signatures **and** accepts valid ones; parents cannot pay against an unlinked child's record. |

## Why the unauthenticated sweep matters

`test_no_api_route_is_reachable_without_a_token` walks every registered route
rather than a hand-written list, so a new endpoint is covered the moment it is
added. When it was first written it immediately found five open endpoints,
including `/auth/debug/users` (dumped the whole user directory) and three
`/auth/debug/fix-*` routes that **reset account passwords to a known value** —
all reachable with no credentials in any environment other than production.

If a route is genuinely public, add it to `PUBLIC_PATHS` **with a comment
explaining why**. That list is the boundary between a deliberate decision and an
accidental data leak.

## Keeping the suite honest

A test that cannot fail is worse than no test. Each suite here was verified by
deliberately reintroducing the bug it targets and confirming it goes red:

* removed the auth dependency from `/auth/debug/users` → access-control sweep failed
* stubbed out the missing-tables repair migration → schema-integrity failed
* reverted the partial-payment fix → two money tests failed

Do the same when you add a test.
