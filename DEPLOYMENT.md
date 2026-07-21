# CAMS Enterprise — Deployment Guide

This guide gets the CAMS system running from scratch with zero prior context. The system
has two parts:

1. **Backend** — a FastAPI (Python) server backed by PostgreSQL. Location: `D:\cams-law\backend`
2. **Android app** — a Kotlin/Jetpack Compose mobile app. Location: `D:\cams-app-upload`

The Android app talks to the backend over HTTP. Start the backend first, then point the
app at it.

---

## 1. Backend — run from scratch

### 1.1 Prerequisites
- **Python 3.11** (the project is developed and tested on 3.11)
- **PostgreSQL 14+** running and reachable
- (Optional) **Redis** — only needed if you enable background/cache features; the app runs without it

### 1.2 Install dependencies
```bash
cd D:\cams-law\backend
python -m venv .venv
.venv\Scripts\activate           # Windows;  source .venv/bin/activate on macOS/Linux
pip install -r requirements.txt
```

### 1.3 Configure environment variables
All configuration is read from a `.env` file in `D:\cams-law\backend`. A template with every
supported key and inline notes is checked in as **`.env.example`** — copy it and fill in values:

```bash
copy .env.example .env            # Windows;  cp .env.example .env elsewhere
```

**Required** (the server refuses to start without it):
- `JWT_SECRET_KEY` — the signing key for auth tokens. Generate a strong value:
  ```bash
  python -c "import secrets; print(secrets.token_urlsafe(64))"
  ```

**Database** — either set a single `DATABASE_URL`:
```
DATABASE_URL=postgresql+asyncpg://<user>:<password>@<host>:5432/<dbname>
```
…or the individual `DB_HOST` / `DB_PORT` / `DB_NAME` / `DB_USER` / `DB_PASSWORD` / `DB_SSL_MODE`
components (the app assembles the URL from them).

**Optional integrations** (the app runs without them; the related feature simply stays
inactive until configured — never hardcode these, always use env vars):
- `RAZORPAY_KEY_ID` / `RAZORPAY_KEY_SECRET` / `RAZORPAY_WEBHOOK_SECRET` — fee payments. Get
  test-mode keys from the Razorpay dashboard. Until set, payment creation/verification fails
  safely (never marks a fee paid without a verified signature).
- `SMTP_HOST` / `SMTP_PORT` / `SMTP_USER` / `SMTP_PASSWORD` / `SMTP_FROM_EMAIL` / `SMTP_USE_TLS`
  — outbound email for password-reset and email-change flows. Until set, those emails don't send.
- `FRONTEND_BASE_URL` — base URL used to build the reset link inside password-reset emails.
- `GEMINI_API_KEY` / `GROQ_API_KEY` — the AI chatbot. Inactive until set.

**`ENVIRONMENT`** — set to `production` in production. This flag disables all `/auth/debug/*`
developer endpoints and blocks the destructive database-reset path in `scripts/init_dev.py`.

### 1.4 Create the database schema

**Migrations are the only source of schema truth.** The application no longer creates or
alters tables at startup — it assumes the schema is already correct. Run this before
starting the app, and again on every deploy that ships a migration:

```bash
alembic upgrade head
alembic heads          # must print exactly one head
```

The full chain is verified to build a complete, model-matching schema from a genuinely
empty PostgreSQL database, deterministically.

> **Use PostgreSQL in development too.** PostgreSQL enforces enum membership, foreign-key
> ordering at CREATE time, and column types; SQLite silently accepts all three. Developing
> on SQLite lets schema defects reach production undetected because nothing local rejects
> them. `docker run -d -e POSTGRES_PASSWORD=… -p 5432:5432 postgres:16-alpine` is enough.

### 1.4b Verify before deploying
```bash
pip install -r requirements.txt -r requirements-dev.txt
TEST_POSTGRES_URL="postgresql://<user>:<pw>@<host>:<port>/<db>" python -m pytest tests/ -q
```
`tests/test_schema_integrity.py` builds a database from `alembic upgrade head` alone and
diffs it against the models, failing on any missing table, missing column, insert-blocking
legacy `NOT NULL` column, or enum value the code writes that the database would reject.
Setting `TEST_POSTGRES_URL` enables the enum check, which cannot run on SQLite. See
`tests/README.md`.

### 1.5 Seed demo/test data (development only)
`scripts/seed.py` populates a realistic demo dataset (departments, courses, timetables,
faculty, students, parents, marks, fees, etc.) and the standard test accounts:
```bash
python -m scripts.seed
```
> Do **not** run the seed against a production database — it clears and repopulates tables.

### 1.6 Start the server
```bash
python -m uvicorn app.main:app --host 0.0.0.0 --port 8000
```
The API is now served under `http://<host>:8000/api/v1/`. A successful start logs
`Application startup complete.`

---

## 2. Android app — build & run

### 2.1 Prerequisites
- **Android Studio** (Giraffe or newer) with **JDK 11**
- Android SDK: compileSdk/targetSdk **34**, minSdk **24**
- An emulator or a physical device on the same network as the backend

### 2.2 Point the app at your backend
The app reads its API base URL from an **`.env`** file in `D:\cams-app-upload` (injected at
build time by the Secrets Gradle Plugin; template is `.env.example`):

```
# .env  in  D:\cams-app-upload
API_BASE_URL=http://10.0.2.2:8000/api/v1/
```
- `10.0.2.2` is the special alias that lets the **Android emulator** reach `localhost` on the
  host machine. Keep the trailing slash.
- On a **physical device**, replace it with your machine's LAN IP, e.g.
  `http://192.168.1.50:8000/api/v1/`.
- In production, use the real HTTPS API URL.

### 2.3 Build & run
```bash
cd D:\cams-app-upload
gradlew.bat assembleDebug        # or press Run in Android Studio
```
Install the resulting APK on the emulator/device, or just hit **Run** in Android Studio.

To only type-check the Kotlin without a full build:
```bash
gradlew.bat compileDebugKotlin
```

### 2.4 Release build (signing)
The release build type is signed via env vars (never commit the keystore or passwords):
- `KEYSTORE_PATH` (defaults to `my-upload-key.jks` in the project root)
- `STORE_PASSWORD`, `KEY_PASSWORD` (key alias is `upload`)
```bash
gradlew.bat assembleRelease
```

---

## 3. Test accounts

All seeded accounts use the password **`Password@123`**. Emails follow `<name>@cams.local`:

| Role      | Email                  | Notes                                    |
|-----------|------------------------|------------------------------------------|
| Student   | `student@cams.local`   | Also `student2..student5@cams.local`     |
| Parent    | `parent@cams.local`    | Linked to a student; `parent2@cams.local`|
| Faculty   | `faculty@cams.local`   | Also `faculty2`, `faculty3@cams.local`   |
| HOD       | `hod@cams.local`       | Head of the BA LLB department            |
| Principal | `principal@cams.local` | Institution-wide access                  |
| Admin     | `admin@cams.local`     | System administration                    |

Password policy (enforced on change-password and reset-password, client and server):
at least 8 characters including an uppercase letter, a lowercase letter, a number, and a
special character.

---

## 4. Quick smoke test

With the backend running on port 8000:
```bash
# Log in (expect HTTP 200 + access_token)
curl -X POST http://localhost:8000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"hod@cams.local","password":"Password@123"}'
```
Then in the app: pick a role on the Role Selection screen, log in with the matching account,
and confirm the dashboard loads real data.

---

## 5. Notes & known operational details

- **Push notifications (FCM):** the app includes a Firebase messaging service and can display
  incoming notifications, but the backend does not yet register device tokens or send pushes
  (no Firebase Admin SDK integration). Server-originated push is therefore not active; in-app
  notifications (fetched over the API) work normally.
- **Developer endpoints:** `/auth/debug/*` (list users, reset specific test accounts) exist for
  local development and return **404 when `ENVIRONMENT=production`**. Keep `ENVIRONMENT=production`
  set in production so they stay disabled.
- **File uploads** (study materials, class-diary attachments) are validated server-side: an
  allow-list of extensions, a 25 MB size cap, and server-generated filenames (client filenames
  are never used for the stored path, preventing path traversal).
- **Payments** are verified server-side: a fee is only marked paid after Razorpay's signature is
  verified. The client callback alone is never trusted.

---

## 6. Production checklist

Before pointing this at real users/money, confirm each of the following. None of these are
optional for a production deployment.

- [ ] **`ENVIRONMENT=production` is set.** This is the single most important production
  setting — several protections key off it. With it set, the app automatically:
  - disables the Swagger UI (`/api/docs`) **and** the raw schema (`/api/openapi.json`)
  - disables `/auth/debug/*` and `/students/temp-debug-users` (404)
  - blocks the destructive reset path in `scripts/init_dev.py`
  - drops the permissive CORS regex (see below)
  - forces `Secure` on auth cookies, so sessions never travel over plain HTTP

  Verified behaviour with `ENVIRONMENT=production`: `/api/docs` → 404, `/api/openapi.json`
  → 404, `/students/temp-debug-users` → denied, `COOKIE_SECURE` → `True`.

  Defence in depth: the `/auth/debug/*` routes and `/students/temp-debug-users` now also
  require `SUPER_ADMIN` independently of this flag, so a deployment that forgets to set
  `ENVIRONMENT` does not expose the user directory or the password-reset helpers.
- [x] **CORS.** *(Fixed in code.)* `app/main.py` previously set `allow_origin_regex=".*"`
  unconditionally alongside `allow_credentials=True`, which matched *any* origin and silently
  defeated the `CORS_ORIGINS` allow-list. The regex is now applied only outside production
  (so LAN/emulator testing still works); under `ENVIRONMENT=production` it is `None` and the
  allow-list is authoritative.
  **You must still set `CORS_ORIGINS`** to your real production origin(s) — it currently ships
  as a ~40-entry list of localhost/dev origins. Set it in `.env` to the web app domain and
  nothing else. Harmless-but-untidy localhost entries are not an attack vector, but a stale
  list is how a genuinely wrong origin gets added later without anyone noticing.
- [ ] **Don't use `entrypoint.sh` as-is for a production container.** It runs
  `python -m scripts.seed` (clears and repopulates data) and starts uvicorn with `--reload` on
  every start. For production, run `alembic upgrade head` (migrations only, no seed) and start
  uvicorn without `--reload`, e.g.:
  ```bash
  alembic upgrade head
  python -m uvicorn app.main:app --host 0.0.0.0 --port 8000 --workers 4
  ```
- [x] **Migration chain builds a complete schema from empty.** *(Resolved.)* The chain is
  verified end to end against an empty PostgreSQL 16 database: `alembic upgrade head`
  succeeds, reports a single head, and produces a schema that matches the models exactly —
  no missing tables, no missing columns, no enum drift — deterministically across repeated
  runs. Startup no longer calls `create_all` or issues ad-hoc `ALTER TABLE` statements, so
  migrations are the only thing shaping the schema.
  `tests/test_schema_integrity.py` enforces this permanently; run the suite (§1.4b) before
  every release so any future gap fails in CI rather than on deploy.
- [x] **`GET /test_connection`** (`app/api/v1/endpoints/maintenance.py`) is intentionally
  unauthenticated so load balancers and uptime probes can call it. *(Fixed in code:)* it
  previously returned the raw driver exception text on failure, which can disclose the database
  host, user, and other connection details to an anonymous caller. It now logs the exception
  server-side and returns a bare `503 "Database unavailable"`. Restrict it at the reverse proxy
  as well if you don't want any unauthenticated endpoint exposed.
- [x] **`POST /fix_schema` removed.** *(Resolved.)* It executed raw, unguarded `ALTER TABLE`
  statements to paper over schema drift. The drift it existed to patch is fixed properly in
  migrations, so the endpoint has been deleted rather than left gated.
- [x] **Uploads are served with authentication and ownership checks.** *(Resolved.)*
  `app/static/uploads` is no longer mounted publicly. Files are served only by
  `GET /api/v1/files/{path}`, which requires a token and then authorises per category —
  students see their own documents, parents their linked child's, faculty only students on
  their roster, staff more broadly. Legacy `/uploads/...` and `/static/uploads/...` URLs
  redirect there and inherit the same checks. Unclassifiable legacy files are staff-only.
  Only genuinely public assets (logos, campus imagery) remain on the open `/static` mount.
- [x] **Authentication endpoints are rate limited.** *(Resolved.)* Login, password reset and
  token refresh are throttled. Limits are keyed per account as well as per IP: an
  institution NATs its whole campus behind one address, so an IP-only limit strict enough to
  stop brute force would lock out legitimate students. Failed logins consume the per-account
  budget, a successful login clears it. Set `RATE_LIMIT_TRUST_FORWARDED_FOR=true` **only**
  once the app is behind a proxy that strips client-supplied `X-Forwarded-For`; otherwise a
  client can forge the header and bypass the limit entirely (it defaults to `false`).
- [ ] **Static/uploads directory permissions.** `app/main.py` serves `app/static` and
  `app/static/uploads` directly via `StaticFiles`, created with `os.makedirs(..., exist_ok=True)`
  on startup. Confirm the deploy process/container gives the app process write access to that
  path and that it persists across restarts/redeploys (mount a volume if containerized) — files
  aren't stored anywhere else (no S3/cloud storage is wired in).
- [ ] **Secrets.** `JWT_SECRET_KEY` is required at startup with no default — generate a unique
  strong value per environment (never reuse the dev key). `RAZORPAY_KEY_SECRET`,
  `RAZORPAY_WEBHOOK_SECRET`, `SMTP_PASSWORD`, and DB credentials must come from your deployment
  platform's secret store, never committed to `.env` in version control.
- [ ] **No Dockerfile/docker-compose currently exists in this repo**, despite `alembic.ini`'s
  default DB URL implying a `postgres` compose hostname. If you're containerizing, you'll need
  to author these — this checklist assumes a bare-metal/VM deployment using the steps in
  Section 1 until then.

---

## 7. Running it in production

Section 1 gets the server up for development. This section covers what a real deployment
additionally needs. None of this is wired up in the repo today — it's operator work.

### 7.1 TLS / reverse proxy
Uvicorn should not face the internet directly. Terminate TLS at nginx/Caddy/ALB and proxy to
uvicorn on localhost. Required because JWTs and Razorpay payloads must never cross the network
in plaintext. Make sure the proxy forwards `X-Forwarded-For`/`X-Forwarded-Proto` and that you
run uvicorn with `--proxy-headers --forwarded-allow-ips=<proxy ip>` so client IPs in logs and
any rate limiting are accurate rather than showing the proxy's address.

### 7.2 Process management
Run uvicorn under a supervisor that restarts it on crash and on boot (systemd unit, or your
platform's equivalent). Use multiple workers sized to the box (`--workers`, commonly
`2 × CPU + 1`), and **never** `--reload` in production. Note the app's startup handler calls
`Base.metadata.create_all(checkfirst=True)`; with several workers starting simultaneously this
runs concurrently — another reason to apply migrations as a separate step *before* starting the
app rather than relying on startup-time table creation.

### 7.3 Backups
Postgres holds attendance, marks, fee, and payment records — losing it is unrecoverable.
Schedule regular `pg_dump` backups, store them off-box, and **verify a restore into a scratch
database at least once** before go-live; an unrestored backup is a guess, not a backup. Also
back up `app/static/uploads`, which lives on local disk and is not in the database.

### 7.4 Logging & monitoring
The app logs to stdout. Ship those logs somewhere durable and searchable. Several fixes in this
codebase deliberately log a warning instead of fabricating data (e.g. a timetable row whose
course or faculty reference no longer resolves) — those warnings are how you find data-integrity
gaps, so they need to be visible rather than discarded. Alert at minimum on: process restarts,
5xx rate, failed payment verifications, and health-check failures.

### 7.5 Rate limiting
There is no rate limiting in the application today. At the proxy layer, throttle at least
`POST /api/v1/auth/login` and the password-reset endpoints to blunt credential stuffing.

### 7.6 Rollback
Before any deploy: take a fresh database backup, and note the current Alembic revision
(`alembic current`). To roll back, redeploy the previous application version and, only if that
release included a migration, `alembic downgrade <previous_revision>`. Check the specific
migration's `downgrade()` before running it — a downgrade that drops a column discards the data
in it, so for destructive migrations restoring the backup is safer than downgrading.
