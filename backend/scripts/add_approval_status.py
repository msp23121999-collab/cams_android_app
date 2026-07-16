import asyncio
from sqlalchemy.ext.asyncio import create_async_engine
from sqlalchemy import text

async def main():
    engine = create_async_engine('postgresql+asyncpg://cams:cams%402026@43.205.177.13:5432/lawcollege')
    async with engine.connect() as conn:
        print("Adding approval_status column to faculty_profiles...")
        try:
            await conn.execute(text("ALTER TABLE faculty_profiles ADD COLUMN IF NOT EXISTS approval_status VARCHAR(64) DEFAULT 'PENDING_PRINCIPAL'"))
            await conn.execute(text("UPDATE faculty_profiles SET approval_status = 'APPROVED' WHERE approval_status IS NULL"))
            await conn.commit()
            print("Successfully added approval_status and updated existing rows!")
        except Exception as e:
            print("Error: ", e)

if __name__ == '__main__':
    asyncio.run(main())
