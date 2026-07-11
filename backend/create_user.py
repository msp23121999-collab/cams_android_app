import asyncio
from app.db.session import engine
from app.core.security import hash_password
from sqlalchemy import text
import uuid
from datetime import datetime, timezone

async def insert_user():
    async with engine.begin() as conn:
        pwd = hash_password("password123")
        uid = str(uuid.uuid4())
        await conn.execute(text("""
            INSERT INTO users (id, email, full_name, hashed_password, role, is_active, created_at, updated_at, is_deleted)
            VALUES (:id, :email, :full_name, :pwd, :role, :is_active, :created_at, :updated_at, :is_deleted)
        """), {
            "id": uid,
            "email": "teststudent@campus.local",
            "full_name": "Test Student",
            "pwd": pwd,
            "role": "STUDENT",
            "is_active": True,
            "created_at": datetime.now(timezone.utc),
            "updated_at": datetime.now(timezone.utc),
            "is_deleted": False
        })
        print("User inserted!")

asyncio.run(insert_user())
