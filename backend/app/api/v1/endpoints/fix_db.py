from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import text, select
from app.core.dependencies import get_db_session
from app.db.models.user import User

router = APIRouter()

@router.get("/temp_users")
async def get_temp_users():
    try:
        import psycopg2
        conn = psycopg2.connect(
            host="43.205.177.13",
            port=5432,
            dbname="lawcollege",
            user="cams",
            password="cams@2026",
            connect_timeout=10,
        )
        cur = conn.cursor()
        cur.execute("SELECT email, role, is_active, full_name FROM users;")
        rows = cur.fetchall()
        users = [{"email": r[0], "role": r[1], "is_active": r[2], "full_name": r[3]} for r in rows]
        cur.close()
        conn.close()
        return {"status": "success", "count": len(users), "users": users}
    except Exception as e:
        return {"status": "error", "message": str(e)}


@router.get("/verify_user")
async def verify_user(email: str, psw: str, db: AsyncSession = Depends(get_db_session)):
    try:
        res = await db.execute(select(User).where(User.email == email))
        user = res.scalar_one_or_none()
        if not user:
            return {"status": "error", "message": "User not found"}
        from app.core.security import verify_password
        pwd_ok = verify_password(psw, user.hashed_password)
        return {
            "status": "success",
            "email": user.email,
            "role": user.role.value,
            "is_active": user.is_active,
            "is_deleted": user.is_deleted,
            "pwd_ok": pwd_ok,
            "hash": user.hashed_password
        }
    except Exception as e:
        return {"status": "error", "message": str(e)}


@router.get("/test_login")
async def test_login(email: str, psw: str, db: AsyncSession = Depends(get_db_session)):
    try:
        from app.services.auth_service import AuthService
        service = AuthService(db)
        user, access_token, refresh_token = await service.authenticate(email, psw)
        return {
            "status": "success",
            "email": user.email,
            "role": user.role.value,
            "access_token": access_token
        }
    except Exception as e:
        return {"status": "error", "message": str(e), "type": str(type(e))}


@router.get("/trigger_seed")
async def trigger_seed(db: AsyncSession = Depends(get_db_session)):
    try:
        from scripts.seed import seed
        await seed()
        return {"status": "success", "message": "Database successfully seeded"}
    except Exception as e:
        return {"status": "error", "message": str(e)}


@router.get("/db_info")
async def db_info(db: AsyncSession = Depends(get_db_session)):
    """
    Safe read-only diagnostic: returns row counts for all major tables.
    Replaces the former /reset_db endpoint which was REMOVED because it
    ran `DROP SCHEMA public CASCADE` without authentication, risking data loss.
    """
    tables = [
        "users", "students", "departments", "courses", "sections",
        "attendance", "marks", "leaves", "grievances", "notices",
        "fee_records", "salary", "faculty_profiles", "faculty_research",
        "study_materials", "assignments", "timetable",
    ]
    counts = {}
    for table in tables:
        try:
            result = await db.execute(text(f"SELECT COUNT(*) FROM {table}"))
            counts[table] = result.scalar()
        except Exception:
            counts[table] = "error"
    return {"status": "ok", "table_counts": counts}


@router.get("/fix_db")
async def fix_db(db: AsyncSession = Depends(get_db_session)):
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
    ]
    results = []
    for q in queries:
        try:
            await db.execute(text(q))
            await db.commit()
            results.append({"query": q, "status": "success"})
        except Exception as e:
            await db.rollback()
            results.append({"query": q, "status": "error", "message": str(e)})

    return {"status": "done", "results": results}
