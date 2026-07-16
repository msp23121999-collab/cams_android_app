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
            "mentor_id": "VARCHAR(36) REFERENCES users(id)",
            "cgpa": "DOUBLE PRECISION",
            "skills": "JSONB",
            "regulation_id": "VARCHAR(36) REFERENCES regulations(id)",
            "quota": "VARCHAR(64)",
            "full_name": "VARCHAR(128)",
            "date_of_birth": "DATE",
            "gender": "VARCHAR(16)",
            "blood_group": "VARCHAR(8)",
            "nationality": "VARCHAR(64)",
            "mobile_number": "VARCHAR(20)",
            "current_address": "VARCHAR(256)",
            "permanent_address": "VARCHAR(256)",
            "aadhaar_number": "VARCHAR(20)",
            "passport_number": "VARCHAR(20)",
            "community_category": "VARCHAR(64)",
            "religion": "VARCHAR(64)",
            "emergency_contact_name": "VARCHAR(128)",
            "emergency_contact_relationship": "VARCHAR(64)",
            "emergency_contact_number": "VARCHAR(20)",
            "languages_known": "JSONB",
            "hobbies_interests": "JSONB",
            "special_skills": "JSONB",
            "medical_info": "VARCHAR(256)"
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
