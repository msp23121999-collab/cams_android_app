# Environment variables

Every setting across the backend and the Android app.
**All values shown are placeholders. Never commit real secrets.**

Templates: `backend/.env.example` and `.env.example` / `local.properties.example` at the root.

---

## Backend — `backend/.env`

### Required

| Variable | Purpose | Example |
|---|---|---|
| `JWT_SECRET_KEY` | Signs auth tokens. **The server will not start without it.** Generate a unique value per environment: `python -c "import secrets; print(secrets.token_urlsafe(64))"` | `REPLACE_WITH_GENERATED_SECRET` |
| `DATABASE_URL` | Database connection. Use PostgreSQL. | `postgresql+asyncpg://user:pass@localhost:5432/cams` |

### Environment

| Variable | Purpose | Default |
|---|---|---|
| `ENVIRONMENT` | `development` or `production`. Setting `production` disables API docs, disables all debug endpoints, forces `Secure` cookies, and enforces the CORS allow-list. | `development` |

### Database (alternative to `DATABASE_URL`)

Set these and the URL is assembled automatically.

| Variable | Example |
|---|---|
| `DB_HOST` | `localhost` |
| `DB_PORT` | `5432` |
| `DB_NAME` | `cams` |
| `DB_USER` | `REPLACE_USER` |
| `DB_PASSWORD` | `REPLACE_PASSWORD` |
| `DB_SSL_MODE` | `require` |

### Authentication

| Variable | Purpose | Default |
|---|---|---|
| `JWT_ALGORITHM` | Token signing algorithm | `HS256` |
| `ACCESS_TOKEN_EXPIRE_MINUTES` | Access token lifetime | `120` |
| `REFRESH_TOKEN_EXPIRE_DAYS` | Refresh token lifetime | `7` |
| `COOKIE_SECURE` | Send auth cookies over HTTPS only. **Forced to `true` when `ENVIRONMENT=production`.** | `false` |

### Payments — Razorpay (optional)

Until set, payment creation returns `503`. A fee is only ever marked paid after the
signature is verified server-side; the client callback alone is never trusted.

| Variable | Purpose |
|---|---|
| `RAZORPAY_KEY_ID` | Public key from the Razorpay dashboard |
| `RAZORPAY_KEY_SECRET` | **Secret** — from your deployment platform's secret store |
| `RAZORPAY_WEBHOOK_SECRET` | **Secret** — verifies incoming webhooks |

### Email (optional)

Used for password-reset and email-change flows. Until set, those emails simply do not send.

| Variable | Example |
|---|---|
| `SMTP_HOST` | `smtp.gmail.com` |
| `SMTP_PORT` | `587` |
| `SMTP_USER` | `noreply@your-college.edu` |
| `SMTP_PASSWORD` | **Secret** |
| `SMTP_FROM_EMAIL` | `noreply@your-college.edu` |
| `SMTP_USE_TLS` | `true` |
| `FRONTEND_BASE_URL` | Base URL for links inside those emails |

### AI chatbot (optional)

| Variable | Default |
|---|---|
| `GEMINI_API_KEY` | *(secret, empty)* |
| `GROQ_API_KEY` | *(secret, empty)* |
| `CHATBOT_MODEL` | `llama-3.1-70b-versatile` |
| `CHATBOT_MAX_TOKENS` | `1000` |

### Push notifications — Firebase (optional)

Server-originated push. With neither set, push is disabled and logged; in-app
notifications continue to work. Supply the service-account JSON one way or the other.

| Variable | Purpose |
|---|---|
| `FIREBASE_CREDENTIALS_JSON` | **Secret** — the service-account JSON inline, as a string |
| `FIREBASE_CREDENTIALS_FILE` | **Secret** — path to the service-account JSON file |

The Android app also needs `google-services.json` from the same Firebase project placed
in `app/`. It is **not** in this repository and must be obtained from the Firebase console.

### Rate limiting

| Variable | Purpose | Default |
|---|---|---|
| `RATE_LIMIT_ENABLED` | Master switch | `true` |
| `RATE_LIMIT_TRUST_FORWARDED_FOR` | Read the client IP from `X-Forwarded-For`. **Only enable behind a proxy that strips any client-supplied value** — otherwise a client can forge the header and bypass the limit entirely. | `false` |
| `RATE_LIMIT_LOGIN` | Per-IP login limit. Deliberately generous: a campus NATs all its users behind one address, so a strict per-IP limit locks out legitimate students. | `120/minute` |
| `RATE_LIMIT_LOGIN_PER_ACCOUNT` | The real brute-force defence. Only failed logins count; a successful login clears it. | `10/15minutes` |
| `RATE_LIMIT_PASSWORD_RESET_REQUEST` | | `5/hour` |
| `RATE_LIMIT_PASSWORD_RESET_CONFIRM` | | `10/hour` |
| `RATE_LIMIT_CHANGE_PASSWORD` | | `10/hour` |
| `RATE_LIMIT_TOKEN_REFRESH` | | `60/minute` |
| `RATE_LIMIT_STORAGE_URI` | Counter storage. Falls back to `REDIS_URL`, then in-memory. | *(empty)* |

### Other

| Variable | Purpose | Default |
|---|---|---|
| `REDIS_URL` | Optional cache / rate-limit storage. The app runs fine without it. | `redis://localhost:6379/0` |
| `CORS_ORIGINS` | Exact origins allowed to call the API. In production this list is authoritative — set it to your real domain and nothing else. | `["http://localhost:3000"]` |

---

## Android app — `.env` (project root)

| Variable | Purpose |
|---|---|
| `API_BASE_URL` | Where the backend is. **Keep the trailing slash.** |

| Setup | Value |
|---|---|
| Emulator | `http://10.0.2.2:8000/api/v1/` — `10.0.2.2` means "the computer running the emulator" |
| Physical device | `http://192.168.1.50:8000/api/v1/` — your computer's LAN IP; phone on the same Wi-Fi |
| Production | `https://api.your-college.edu/api/v1/` |

Read at build time as `BuildConfig.API_BASE_URL`, then `AppConfig.BASE_URL`, then Retrofit.
Changing it requires no Kotlin changes.

---

## Android app — `local.properties` (project root)

| Variable | Purpose |
|---|---|
| `sdk.dir` | Path to your Android SDK. Android Studio usually writes this for you. |
| `KEYSTORE_PATH` | Release signing only. **The keystore file itself must never be committed.** |
| `STORE_PASSWORD` | **Secret** — release signing only |
| `KEY_PASSWORD` | **Secret** — release signing only |

Debug builds need none of the signing values.

---

## Secrets checklist before deploying

- [ ] `JWT_SECRET_KEY` is unique to this environment — never reused from development
- [ ] `RAZORPAY_KEY_SECRET`, `RAZORPAY_WEBHOOK_SECRET`, `SMTP_PASSWORD` and database
      credentials come from your platform's secret store, not a committed file
- [ ] `ENVIRONMENT=production`
- [ ] `CORS_ORIGINS` lists only your real domain
- [ ] `RATE_LIMIT_TRUST_FORWARDED_FOR=true` **only** if a trusted proxy sits in front
- [ ] No `.env`, `local.properties` or `*.jks` file is committed
