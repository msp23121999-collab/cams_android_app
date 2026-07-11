"""
scripts/init_dev.py — Development database initialisation script.

DANGER ZONE: This script can DROP the entire database schema.

To protect production data, this script requires TWO env vars to be set
before any destructive operation runs:
    RESET_DB=true
    RESET_DB_TOKEN=CONFIRM-WIPE-cams2026-xK9pQ3

Without both, the script runs in SAFE MODE: it only creates missing tables.
"""
import asyncio
import os

from sqlalchemy.ext.asyncio import create_async_engine

from app.core.config import settings
from app.db.base import Base
from app.db.models import *  # noqa: F401,F403 — register all models
from app.db.session import engine
from app.db import models  # noqa: F401

# ── Safety constants ──────────────────────────────────────────────────────────
SAFETY_RESET_TOKEN = "CONFIRM-WIPE-cams2026-xK9pQ3"


def _wipe_authorised() -> bool:
    """Return True only when both safety env vars are correctly set."""
    reset_flag = os.getenv("RESET_DB", "")
    reset_token = os.getenv("RESET_DB_TOKEN", "")
    environment = os.getenv("ENVIRONMENT", "").lower()

    if environment == "production":
        print("[BLOCKED] Database wipe blocked: ENVIRONMENT is 'production'.")
        return False

    if reset_flag != "true":
        print("[SAFE] RESET_DB != 'true' — running in safe create-only mode.")
        return False

    if reset_token != SAFETY_RESET_TOKEN:
        print("[SAFE] RESET_DB_TOKEN does not match — wipe aborted.")
        print("       Set RESET_DB_TOKEN=CONFIRM-WIPE-cams2026-xK9pQ3 to proceed.")
        return False

    return True


async def init_db():
    database_url = os.getenv("DATABASE_URL", settings.DATABASE_URL)
    print("Using DATABASE_URL:", database_url)

    import app.db.models  # noqa: F401 — ensure all models are registered

    async_engine = create_async_engine(database_url, echo=False, future=True)
    async with async_engine.begin() as conn:
        if _wipe_authorised():
            print("WARNING: Database wipe authorised — proceeding with DROP SCHEMA...")
            if "postgresql" in database_url:
                from sqlalchemy import text
                try:
                    await conn.execute(text("""
                        SELECT pg_terminate_backend(pid)
                        FROM pg_stat_activity
                        WHERE datname = current_database()
                          AND pid <> pg_backend_pid();
                    """))
                except Exception as e:
                    print("Warning: Could not terminate other connections:", e)
                await conn.execute(text("DROP SCHEMA public CASCADE;"))
                await conn.execute(text("CREATE SCHEMA public;"))
            else:
                await conn.run_sync(Base.metadata.drop_all)
        else:
            print("[SAFE] Skipping schema wipe — creating missing tables only.")

        # Always create tables (safe — uses checkfirst so existing tables survive)
        await conn.run_sync(lambda c: Base.metadata.create_all(c, checkfirst=True))

    await async_engine.dispose()

    # Run seed (seed.py itself also has a safety check, will skip if data exists)
    from scripts.seed import seed
    await seed()


if __name__ == "__main__":
    asyncio.run(init_db())
