import asyncio
from app.db.session import engine
from sqlalchemy import text

async def get_user():
    async with engine.begin() as conn:
        res = await conn.execute(text("SELECT email, hashed_password FROM users LIMIT 1"))
        user = res.fetchone()
        print(f"User: {user[0]}, PassHash: {user[1]}")

asyncio.run(get_user())
