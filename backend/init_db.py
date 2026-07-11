"""
init_db.py — Safe table-creation script.

IMPORTANT: The `drop_all` call has been REMOVED from this file.
Running this script will ONLY create missing tables, never drop existing ones.

To wipe and reseed the database, you must:
  1. Set env var  RESET_DB=true
  2. Set env var  RESET_DB_TOKEN=CONFIRM-WIPE-cams2026-xK9pQ3
  3. Run: py scripts/init_dev.py
"""
import asyncio
from app.db.base import Base
from app.db import models  # noqa: F401 — registers all models
from app.db.session import engine


async def init_models():
    async with engine.begin() as conn:
        # checkfirst=True means: only CREATE tables that don't already exist.
        # Existing tables with live data are NEVER touched.
        await conn.run_sync(lambda sync_conn: Base.metadata.create_all(sync_conn, checkfirst=True))
    print("[OK] Tables created (existing tables preserved).")


if __name__ == "__main__":
    asyncio.run(init_models())
