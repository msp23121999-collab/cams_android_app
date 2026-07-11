import sys
import asyncio


# Trigger reload comment updated - fixing student login
from fastapi import FastAPI
from contextlib import asynccontextmanager
from fastapi.middleware.cors import CORSMiddleware

from app.api.v1.router import api_router
from app.core.config import settings
from app.core.exceptions import register_exception_handlers

from fastapi.staticfiles import StaticFiles
import os


def create_app() -> FastAPI:
    @asynccontextmanager
    async def lifespan(app: FastAPI):
        from app.db.base import Base
        from app.db import models
        from app.db.session import engine
        from sqlalchemy import text
        async with engine.begin() as conn:
            await conn.run_sync(lambda sync_conn: Base.metadata.create_all(sync_conn, checkfirst=True))
            
            if conn.dialect.name != 'sqlite':
                # Migration to add verification columns to students table
                res = await conn.execute(text("""
                    SELECT column_name 
                    FROM information_schema.columns 
                    WHERE table_name = 'students';
                """))
                existing = {row[0] for row in res.fetchall()}
                cols = {
                    "verification_status": "VARCHAR(64) DEFAULT 'DRAFT'",
                    "staff_remarks": "VARCHAR(500)",
                    "hod_remarks": "VARCHAR(500)",
                    "document_aadhaar_url": "VARCHAR(1000)",
                    "document_community_url": "VARCHAR(1000)",
                    "document_tc_url": "VARCHAR(1000)",
                    "document_other_url": "VARCHAR(1000)",
                    "edit_request_status": "VARCHAR(64)",
                    "edit_request_reason": "VARCHAR(1000)",
                    "mentor_id": "VARCHAR(64) REFERENCES users(id)"
                }
                for col, col_type in cols.items():
                    if col not in existing:
                        try:
                            await conn.execute(text(f"ALTER TABLE students ADD COLUMN {col} {col_type};"))
                            print(f"[Migration] Added column {col} to students table.")
                        except Exception as e:
                            print(f"[Migration] Error adding {col}: {e}")
    
                # Migration to add faculty_id column to faculty_profiles table
                res_fac = await conn.execute(text("""
                    SELECT column_name 
                    FROM information_schema.columns 
                    WHERE table_name = 'faculty_profiles';
                """))
                existing_fac = {row[0] for row in res_fac.fetchall()}
                if "faculty_id" not in existing_fac:
                    try:
                        await conn.execute(text("ALTER TABLE faculty_profiles ADD COLUMN faculty_id VARCHAR(64);"))
                        print("[Migration] Added column faculty_id to faculty_profiles table.")
                        await conn.execute(text("UPDATE faculty_profiles SET faculty_id = UPPER(SUBSTR(user_id, 1, 8)) WHERE faculty_id IS NULL;"))
                        print("[Migration] Initialized existing faculty_id values.")
                    except Exception as e:
                        print(f"[Migration] Error migrating faculty_profiles: {e}")
    
                if "community" not in existing_fac:
                    try:
                        await conn.execute(text("ALTER TABLE faculty_profiles ADD COLUMN community VARCHAR(64);"))
                        print("[Migration] Added column community to faculty_profiles table.")
                    except Exception as e:
                        print(f"[Migration] Error adding community column: {e}")
    
                # Migration to add degree_id column to academic_years table
                res_ay = await conn.execute(text("""
                    SELECT column_name 
                    FROM information_schema.columns 
                    WHERE table_name = 'academic_years';
                """))
                existing_ay = {row[0] for row in res_ay.fetchall()}
                if "degree_id" not in existing_ay:
                    try:
                        await conn.execute(text("ALTER TABLE academic_years ADD COLUMN degree_id VARCHAR(36) REFERENCES degrees(id);"))
                        print("[Migration] Added column degree_id to academic_years table.")
                        
                        # Fill default degree if rows exist
                        await conn.execute(text("UPDATE academic_years SET degree_id = (SELECT id FROM degrees LIMIT 1) WHERE degree_id IS NULL;"))
                        print("[Migration] Initialized existing degree_id values.")
                        
                        # Set NOT NULL constraint
                        await conn.execute(text("ALTER TABLE academic_years ALTER COLUMN degree_id SET NOT NULL;"))
                        print("[Migration] Applied NOT NULL constraint to degree_id in academic_years.")
                    except Exception as e:
                        print(f"[Migration] Error migrating academic_years: {e}")
    
                # Migration to add multi-level leave columns to leaves table
                res_lv = await conn.execute(text("""
                    SELECT column_name 
                    FROM information_schema.columns 
                    WHERE table_name = 'leaves';
                """))
                existing_lv = {row[0] for row in res_lv.fetchall()}
                lv_cols = {
                    "hod_status": "VARCHAR(32)",
                    "hod_action_by": "VARCHAR(36)",
                    "hod_action_date": "TIMESTAMP",
                    "hod_remarks": "VARCHAR(1024)",
                    "principal_action_by": "VARCHAR(36)",
                    "principal_action_date": "TIMESTAMP",
                    "principal_remarks": "VARCHAR(1024)"
                }
                for col, col_type in lv_cols.items():
                    if col not in existing_lv:
                        try:
                            await conn.execute(text(f"ALTER TABLE leaves ADD COLUMN {col} {col_type};"))
                            print(f"[Migration] Added column {col} to leaves table.")
                        except Exception as e:
                            print(f"[Migration] Error adding {col} to leaves: {e}")
    
                # Replicate SQLite migration: migrate PENDING faculty leaves to PENDING_HOD
                try:
                    await conn.execute(text("""
                        UPDATE leaves
                        SET status = 'PENDING_HOD'
                        WHERE status = 'PENDING'
                          AND user_id IN (
                              SELECT id FROM users WHERE role IN ('FACULTY', 'HOD')
                          );
                    """))
                    print(f"[Migration] Migrated legacy PENDING leaves to PENDING_HOD.")
                except Exception as e:
                    print(f"[Migration] Error migrating legacy leave statuses: {e}")
    
                # Migration to add approval columns to study_materials table
                res_sm = await conn.execute(text("""
                    SELECT column_name 
                    FROM information_schema.columns 
                    WHERE table_name = 'study_materials';
                """))
                existing_sm = {row[0] for row in res_sm.fetchall()}
                sm_cols = {
                    "approved_by": "VARCHAR(36) REFERENCES users(id)",
                    "approved_date": "VARCHAR(64)",
                    "rejected_by": "VARCHAR(36) REFERENCES users(id)",
                    "rejected_date": "VARCHAR(64)",
                    "rejection_remarks": "VARCHAR(2048)"
                }
                for col, col_type in sm_cols.items():
                    if col not in existing_sm:
                        try:
                            await conn.execute(text(f"ALTER TABLE study_materials ADD COLUMN {col} {col_type};"))
                            print(f"[Migration] Added column {col} to study_materials table.")
                        except Exception as e:
                            print(f"[Migration] Error adding {col} to study_materials: {e}")
    
                # Migration to add target/event columns to notices table
                res_nt = await conn.execute(text("""
                    SELECT column_name 
                    FROM information_schema.columns 
                    WHERE table_name = 'notices';
                """))
                existing_nt = {row[0] for row in res_nt.fetchall()}
                nt_cols = {
                    "event_date": "DATE",
                    "audience_types": "VARCHAR(512)",
                    "degree_id": "VARCHAR(36) REFERENCES degrees(id)",
                    "batch_id": "VARCHAR(64)",
                    "department_id": "VARCHAR(36) REFERENCES departments(id)",
                    "attachment_url": "VARCHAR(512)"
                }
                for col, col_type in nt_cols.items():
                    if col not in existing_nt:
                        try:
                            await conn.execute(text(f"ALTER TABLE notices ADD COLUMN {col} {col_type};"))
                            print(f"[Migration] Added column {col} to notices table.")
                        except Exception as e:
                            print(f"[Migration] Error adding {col} to notices: {e}")
    
                # Migration to add message columns to internal_marks table
                res_im = await conn.execute(text("""
                    SELECT column_name 
                    FROM information_schema.columns 
                    WHERE table_name = 'internal_marks';
                """))
                existing_im = {row[0] for row in res_im.fetchall()}
                im_cols = {
                    "hod_message": "VARCHAR",
                    "faculty_reply": "VARCHAR",
                    "is_message_visible_to_student": "BOOLEAN DEFAULT FALSE"
                }
                for col, col_type in im_cols.items():
                    if col not in existing_im:
                        try:
                            await conn.execute(text(f"ALTER TABLE internal_marks ADD COLUMN {col} {col_type};"))
                            print(f"[Migration] Added column {col} to internal_marks table.")
                        except Exception as e:
                            print(f"[Migration] Error adding {col} to internal_marks: {e}")
    
        from apscheduler.schedulers.asyncio import AsyncIOScheduler
        from app.services.backup_service import BackupService
        from app.db.session import AsyncSessionLocal
        
        scheduler = AsyncIOScheduler()
        
        async def run_auto_backup():
            async with AsyncSessionLocal() as db:
                service = BackupService(db)
                config = await service.get_config()
                if config.auto_backup_enabled:
                    # Auto backup is incremental
                    try:
                        await service.create_backup(trigger_type="SCHEDULED", is_incremental=True)
                    except Exception:
                        pass  # Fail silently in schedule, service logs status in history

        async with AsyncSessionLocal() as db:
            service = BackupService(db)
            config = await service.get_config()
            schedule_time = config.schedule_time

        try:
            hour, minute = map(int, schedule_time.split(":"))
        except Exception:
            hour, minute = 21, 0

        scheduler.add_job(
            run_auto_backup,
            "cron",
            hour=hour,
            minute=minute,
            id="daily_backup",
            replace_existing=True
        )
        async def run_daily_staff_attendance():
            from app.services.staff_attendance_service import StaffAttendanceService
            from datetime import date
            async with AsyncSessionLocal() as db:
                service = StaffAttendanceService(db)
                try:
                    await service.initialize_daily_attendance(date.today())
                except Exception:
                    pass

        scheduler.add_job(
            run_daily_staff_attendance,
            "cron",
            hour=0,
            minute=1,
            id="daily_staff_attendance",
            replace_existing=True
        )
        scheduler.start()
        app.state.scheduler = scheduler
        
        yield
        
        scheduler = getattr(app.state, "scheduler", None)
        if scheduler:
            scheduler.shutdown()

    app = FastAPI(
        title="CAMS API",
        docs_url="/api/docs" if settings.ENVIRONMENT != "production" else None,
        openapi_url="/api/openapi.json",
        version="1.0.0",
        lifespan=lifespan,
    )

    # Ensure static directory exists
    static_dir = os.path.join(os.path.dirname(__file__), "static")
    os.makedirs(static_dir, exist_ok=True)
    os.makedirs(os.path.join(static_dir, "uploads"), exist_ok=True)

    app.mount("/static", StaticFiles(directory=static_dir), name="static")
    app.mount("/uploads", StaticFiles(directory=os.path.join(static_dir, "uploads")), name="uploads")

    app.add_middleware(
        CORSMiddleware,
        allow_origins=settings.CORS_ORIGINS,
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
        allow_origin_regex=".*",
    )

    register_exception_handlers(app)
    app.include_router(api_router, prefix="/api/v1")
    from app.api.v1.endpoints.dashboard import router as dashboard_router
    app.include_router(dashboard_router, prefix="/api/dashboard")



    return app


app = create_app()
