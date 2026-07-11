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

    JWT_SECRET_KEY: str = "change-me-in-production"
    JWT_ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 120
    REFRESH_TOKEN_EXPIRE_DAYS: int = 7

    GEMINI_API_KEY: str = ""
    GROQ_API_KEY: str = ""
    CHATBOT_MODEL: str = "llama-3.1-70b-versatile"
    CHATBOT_MAX_TOKENS: int = 1000

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
