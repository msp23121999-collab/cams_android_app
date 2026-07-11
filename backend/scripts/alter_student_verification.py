import asyncio
import os
import sys
from sqlalchemy import text
from sqlalchemy.ext.asyncio import create_async_engine

backend_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
if backend_dir not in sys.path:
    sys.path.append(backend_dir)

from app.core.config import settings

async def alter_student_table():
    database_url = os.getenv("DATABASE_URL", settings.DATABASE_URL)
    print("Connecting to DATABASE_URL:", database_url)
    
    async_engine = create_async_engine(database_url, echo=False, future=True)
    async with async_engine.begin() as conn:
        # Get existing columns in students table
        result = await conn.execute(text("""
            SELECT column_name 
            FROM information_schema.columns 
            WHERE table_name = 'students';
        """))
        existing_columns = {row[0] for row in result.fetchall()}
        print("Existing columns in 'students' table:", existing_columns)
        
        # Dictionary of columns to add with their types
        cols_to_add = {
            "verification_status": "VARCHAR(64) DEFAULT 'DRAFT'",
            "staff_remarks": "VARCHAR(500)",
            "hod_remarks": "VARCHAR(500)",
            "document_aadhaar_url": "VARCHAR(1000)",
            "document_community_url": "VARCHAR(1000)",
            "document_tc_url": "VARCHAR(1000)",
            "document_other_url": "VARCHAR(1000)",
            "edit_request_status": "VARCHAR(64)",
            "edit_request_reason": "VARCHAR(1000)"
        }
        
        for col_name, col_type in cols_to_add.items():
            if col_name not in existing_columns:
                print(f"Adding column '{col_name}' to 'students' table...")
                try:
                    await conn.execute(text(f"ALTER TABLE students ADD COLUMN {col_name} {col_type};"))
                    print(f"Successfully added column '{col_name}'.")
                except Exception as e:
                    print(f"Error adding column '{col_name}': {e}")
            else:
                print(f"Column '{col_name}' already exists.")
                
    await async_engine.dispose()
    print("Schema update complete.")

if __name__ == "__main__":
    if sys.platform == "win32":
        asyncio.set_event_loop_policy(asyncio.WindowsSelectorEventLoopPolicy())
    asyncio.run(alter_student_table())
