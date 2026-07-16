import asyncio
from sqlalchemy.ext.asyncio import create_async_engine
from sqlalchemy import text
from app.core.security import hash_password

async def main():
    engine = create_async_engine('postgresql+asyncpg://cams:cams%402026@43.205.177.13:5432/lawcollege')
    new_hash = hash_password('Password@123')
    async with engine.begin() as conn:
        await conn.execute(
            text("""
                UPDATE users 
                SET hashed_password = :h 
                WHERE email IN ('faculty@cams.local', 'faculty2@cams.local', 'faculty3@cams.local', 'hod@cams.local')
            """),
            {'h': new_hash}
        )
    print('Password reset successful for Faculty and HOD!')

asyncio.run(main())
