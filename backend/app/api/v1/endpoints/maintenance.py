# File: app/api/v1/endpoints/maintenance.py




from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import text

from app.db.session import get_async_session

router = APIRouter()

@router.post("/fix_schema", tags=["maintenance"])
async def fix_schema(db: AsyncSession = Depends(get_async_session)):
    """Execute ALTER TABLE statements to synchronize DB schema.
    This endpoint is intended for one‑off maintenance. It adds missing columns
    to `students` and `leaves` tables if they do not already exist.
    """
    queries = [
        "ALTER TABLE leaves ADD COLUMN IF NOT EXISTS app_category VARCHAR(64) DEFAULT 'Leave';",
        "ALTER TABLE leaves ADD COLUMN IF NOT EXISTS session_type VARCHAR(32);",
        "ALTER TABLE leaves ADD COLUMN IF NOT EXISTS priority VARCHAR(32) DEFAULT 'Normal';",
        "ALTER TABLE leaves ADD COLUMN IF NOT EXISTS location_accuracy FLOAT;",
        "ALTER TABLE leaves ADD COLUMN IF NOT EXISTS geo_fence_status VARCHAR(64);",
        "ALTER TABLE leaves ADD COLUMN IF NOT EXISTS device_network_info VARCHAR(256);",
        "ALTER TABLE leaves ADD COLUMN IF NOT EXISTS metadata JSON;",
        "ALTER TABLE students ADD COLUMN IF NOT EXISTS mentor_id VARCHAR(36);",
        "ALTER TABLE students ADD COLUMN IF NOT EXISTS cgpa FLOAT;",
        "ALTER TABLE students ADD COLUMN IF NOT EXISTS skills JSON;",
        "ALTER TABLE students ADD COLUMN IF NOT EXISTS degree_id VARCHAR(36);",
        "ALTER TABLE students ADD COLUMN IF NOT EXISTS quota VARCHAR(64);",
        "ALTER TABLE students ADD COLUMN IF NOT EXISTS full_name VARCHAR(128);",
        "ALTER TABLE students ADD COLUMN IF NOT EXISTS date_of_birth DATE;",
        "ALTER TABLE students ADD COLUMN IF NOT EXISTS gender VARCHAR(16);",
        "ALTER TABLE students ADD COLUMN IF NOT EXISTS blood_group VARCHAR(8);",
        "ALTER TABLE students ADD COLUMN IF NOT EXISTS nationality VARCHAR(64);",
        "ALTER TABLE students ADD COLUMN IF NOT EXISTS mobile_number VARCHAR(20);",
        "ALTER TABLE students ADD COLUMN IF NOT EXISTS current_address VARCHAR(256);",
        "ALTER TABLE students ADD COLUMN IF NOT EXISTS permanent_address VARCHAR(256);",
        "ALTER TABLE students ADD COLUMN IF NOT EXISTS aadhaar_number VARCHAR(20);",
        "ALTER TABLE students ADD COLUMN IF NOT EXISTS passport_number VARCHAR(20);",
        "ALTER TABLE students ADD COLUMN IF NOT EXISTS community_category VARCHAR(64);",
        "ALTER TABLE students ADD COLUMN IF NOT EXISTS religion VARCHAR(64);",
        "ALTER TABLE students ADD COLUMN IF NOT EXISTS emergency_contact_name VARCHAR(128);",
        "ALTER TABLE students ADD COLUMN IF NOT EXISTS emergency_contact_relationship VARCHAR(64);",
        "ALTER TABLE students ADD COLUMN IF NOT EXISTS emergency_contact_number VARCHAR(20);",
        "ALTER TABLE students ADD COLUMN IF NOT EXISTS languages_known JSON;",
        "ALTER TABLE students ADD COLUMN IF NOT EXISTS hobbies_interests JSON;",
        "ALTER TABLE students ADD COLUMN IF NOT EXISTS special_skills JSON;",
        "ALTER TABLE students ADD COLUMN IF NOT EXISTS medical_info VARCHAR(256);",
    ]
    try:
        for q in queries:
            await db.execute(text(q))
        await db.commit()
        return {"status": "success", "message": "Schema synchronized"}
    except Exception as e:
        await db.rollback()
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/test_connection", tags=["maintenance"])
async def test_connection(db: AsyncSession = Depends(get_async_session)):
    """Simple health‑check that verifies DB connectivity."""
    try:
        await db.execute(text("SELECT 1"))
        return {"status": "ok", "message": "Database connection successful"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
