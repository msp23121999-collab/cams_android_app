import asyncio
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from sqlalchemy import text
from app.db.session import engine

async def drop():
    async with engine.begin() as conn:
        await conn.execute(text("DROP TABLE IF EXISTS attendance CASCADE;"))
    print("Dropped attendance table successfully.")

if __name__ == "__main__":
    asyncio.run(drop())
