import asyncio
import os
from sqlalchemy.ext.asyncio import create_async_engine
from sqlalchemy import text

# Add parent dir to path if needed, or just import using relative/absolute
import sys
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from app.core.config import settings

async def alter_table():
    database_url = os.getenv("DATABASE_URL", settings.DATABASE_URL)
    print("Using DATABASE_URL:", database_url)
    engine = create_async_engine(database_url, echo=True)
    async with engine.begin() as conn:
        await conn.execute(text("ALTER TABLE departments ADD COLUMN IF NOT EXISTS intake INTEGER DEFAULT 60;"))
        await conn.execute(text("ALTER TABLE departments ADD COLUMN IF NOT EXISTS affiliation_code VARCHAR(255);"))
    await engine.dispose()
    print("Columns added successfully!")

if __name__ == "__main__":
    asyncio.run(alter_table())
