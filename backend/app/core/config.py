from functools import lru_cache
from typing import List

from pydantic import model_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


import os
from pathlib import Path

_CUR_DIR = Path(__file__).resolve().parent
# config.py is in app/core/ so parent is app/, grandparent is backend/
_BACKEND_DIR = _CUR_DIR.parent.parent
_ENV_FILE = _BACKEND_DIR / ".env"

class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=str(_ENV_FILE), env_file_encoding="utf-8", extra="ignore")

    ENVIRONMENT: str = "development"
    DATABASE_URL: str = "postgresql+asyncpg://postgres:root@localhost:5432/cams"
    REDIS_URL: str = "redis://localhost:6379/0"

    # Individual Database connection parameters for PostgreSQL/SQLite
    DB_HOST: str = ""
    DB_PORT: str = ""
    DB_NAME: str = ""
    DB_USER: str = ""
    DB_PASSWORD: str = ""
    DB_SSL_MODE: str = ""

    @model_validator(mode="after")
    def enforce_production_cookie_security(self):
        """Auth cookies must never travel over plain HTTP in production.

        Forced rather than merely defaulted: there is no legitimate production
        configuration that benefits from insecure session cookies, and relying on
        the operator to set it leaves session hijacking one missed env var away.
        """
        if self.ENVIRONMENT == "production":
            self.COOKIE_SECURE = True
        return self

    @model_validator(mode="before")
    @classmethod
    def assemble_db_connection(cls, data: dict) -> dict:
        if isinstance(data, dict):
            db_host = data.get("DB_HOST") or os.getenv("DB_HOST")
            db_user = data.get("DB_USER") or os.getenv("DB_USER")
            db_password = data.get("DB_PASSWORD") or os.getenv("DB_PASSWORD")
            db_name = data.get("DB_NAME") or os.getenv("DB_NAME")
            db_port = data.get("DB_PORT") or os.getenv("DB_PORT") or "5432"
            db_ssl_mode = data.get("DB_SSL_MODE") or os.getenv("DB_SSL_MODE") or ""

            if db_host and db_user and db_password and db_name:
                ssl_arg = f"?ssl={db_ssl_mode}" if db_ssl_mode else ""
                data["DATABASE_URL"] = f"postgresql+asyncpg://{db_user}:{db_password}@{db_host}:{db_port}/{db_name}{ssl_arg}"
        return data

    JWT_SECRET_KEY: str
    JWT_ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 120
    REFRESH_TOKEN_EXPIRE_DAYS: int = 7

    GEMINI_API_KEY: str = ""
    GROQ_API_KEY: str = ""
    CHATBOT_MODEL: str = "llama-3.1-70b-versatile"
    CHATBOT_MAX_TOKENS: int = 1000

    # Razorpay payment gateway. Empty defaults are safe (payments simply fail
    # to process until configured) — unlike JWT_SECRET_KEY this is not a
    # security hole to leave blank.
    RAZORPAY_KEY_ID: str = ""
    RAZORPAY_KEY_SECRET: str = ""
    # Webhook secret configured in the Razorpay dashboard for the webhook endpoint.
    # Empty default is safe — the webhook handler no-ops (with a warning log) until set.
    RAZORPAY_WEBHOOK_SECRET: str = ""

    # SMTP for password reset / email verification. Emails will fail to send until configured.
    SMTP_HOST: str = ""
    SMTP_PORT: int = 587
    SMTP_USER: str = ""
    SMTP_PASSWORD: str = ""
    SMTP_FROM_EMAIL: str = ""
    SMTP_USE_TLS: bool = True

    # Base URL of the (future) frontend, used to construct email verification /
    # password reset links. Placeholder until a real web frontend exists.
    FRONTEND_BASE_URL: str = "https://app.cams.local"

    # Send auth cookies only over HTTPS. Must be True in production; left False
    # in development so plain-HTTP localhost testing still works.
    COOKIE_SECURE: bool = False

    # --- Push notifications (Firebase Cloud Messaging) ---
    # Optional. With neither set, push is disabled and logged; in-app notifications
    # continue to work. Supply the service-account JSON either inline or by path.
    # These are SECRETS — never commit them.
    FIREBASE_CREDENTIALS_JSON: str = ""
    FIREBASE_CREDENTIALS_FILE: str = ""

    # --- Rate limiting (authentication endpoints only) ---
    # Master switch; set false only for local load testing.
    RATE_LIMIT_ENABLED: bool = True
    # Storage backend for counters. Empty => fall back to REDIS_URL, and if that
    # is unreachable the limiter degrades to per-worker in-memory storage.
    RATE_LIMIT_STORAGE_URI: str = ""
    # Only enable when a trusted reverse proxy sets X-Forwarded-For and strips any
    # client-supplied value. Defaults to False because if nothing trustworthy sets
    # the header, a client can forge it and get a fresh rate-limit bucket per
    # request — i.e. no rate limiting at all. Set to True in production only once
    # the app sits behind your proxy (see DEPLOYMENT.md §7.1/§7.5).
    RATE_LIMIT_TRUST_FORWARDED_FOR: bool = False
    # slowapi limit strings, per client IP.
    # Per-IP login limit. Deliberately generous: an institution NATs its whole
    # campus behind one public IP, so a low value here locks out legitimate
    # students. This exists to stop one host spraying many accounts; the actual
    # brute-force defence is RATE_LIMIT_LOGIN_PER_ACCOUNT below.
    RATE_LIMIT_LOGIN: str = "120/minute"
    # Failed logins tolerated per account before it is temporarily locked.
    # Only failures count, and a successful login resets it.
    RATE_LIMIT_LOGIN_PER_ACCOUNT: str = "10/15minutes"
    RATE_LIMIT_PASSWORD_RESET_REQUEST: str = "5/hour"
    RATE_LIMIT_PASSWORD_RESET_CONFIRM: str = "10/hour"
    RATE_LIMIT_CHANGE_PASSWORD: str = "10/hour"
    RATE_LIMIT_TOKEN_REFRESH: str = "60/minute"

    CORS_ORIGINS: List[str] = [
        "http://localhost:3000",
        "http://localhost:3001",
        "http://localhost:3002",
        "http://localhost:5173",
        "https://localhost:5173",
        "http://localhost:5174",
        "https://localhost:5174",
        "http://localhost:5175",
        "https://localhost:5175",
        "http://localhost:5176",
        "http://localhost:5177",
        "http://localhost:5178",
        "http://localhost:5179",
        "http://localhost:5180",
        "http://localhost:5270",
        "https://localhost:5270",
        "http://localhost:5273",
        "https://localhost:5273",
        "http://localhost:5275",
        "https://localhost:5275",
        "http://localhost:5276",
        "https://localhost:5276",
        "http://127.0.0.1:3000",
        "http://127.0.0.1:3001",
        "http://127.0.0.1:3002",
        "http://127.0.0.1:5173",
        "http://127.0.0.1:5174",
        "http://127.0.0.1:5175",
        "http://127.0.0.1:5176",
        "http://127.0.0.1:5177",
        "http://127.0.0.1:5178",
        "http://127.0.0.1:5179",
        "http://127.0.0.1:5180",
        "http://127.0.0.1:5270",
        "http://127.0.0.1:5273",
        "http://127.0.0.1:5275",
        "http://127.0.0.1:5276",
        "http://students.campus.local",
        "http://staff.campus.local",
        "http://admin.campus.local",
    ]


@lru_cache
def get_settings() -> Settings:
    return Settings()


settings = get_settings()
