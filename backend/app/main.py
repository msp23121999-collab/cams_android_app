import sys
import asyncio


# Trigger reload comment updated - fixing student login
from fastapi import FastAPI, Request
from fastapi.responses import RedirectResponse
from slowapi.errors import RateLimitExceeded
from slowapi.middleware import SlowAPIMiddleware

from app.core.rate_limit import limiter, rate_limit_exceeded_handler
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
        # Schema is owned entirely by Alembic migrations; run `alembic upgrade head`
        # before starting the app. A previous version created tables here with
        # Base.metadata.create_all() and then patched columns with hand-written
        # ALTER TABLE statements. That masked real gaps in the migration chain (six
        # tables and fifty columns were never created by any migration) and raced
        # across uvicorn workers, so it has been removed. See migrations
        # c9f21a7de401 and d4e77b1c9a02, which backfill what was missing.
    
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
        # Kept in step with docs_url: publishing the full schema in production
        # would negate disabling the Swagger UI.
        openapi_url="/api/openapi.json" if settings.ENVIRONMENT != "production" else None,
        version="1.0.0",
        lifespan=lifespan,
    )

    # Ensure static directory exists
    static_dir = os.path.join(os.path.dirname(__file__), "static")
    os.makedirs(static_dir, exist_ok=True)
    os.makedirs(os.path.join(static_dir, "uploads"), exist_ok=True)

    # Uploads hold personal documents (ID proofs, community/TC certificates,
    # medical info) and must never be served unauthenticated. They are served
    # only by the auth-gated GET /api/v1/files/{path} endpoint. Legacy
    # /uploads/... and /static/uploads/... URLs stored in the database are
    # redirected there (the redirect target still enforces authn + ownership).
    async def _redirect_legacy_upload(request: Request) -> RedirectResponse:
        return RedirectResponse(
            url=f"/api/v1/files/{request.path_params.get('file_path', '')}",
            status_code=307,
        )

    app.add_route("/uploads/{file_path:path}", _redirect_legacy_upload, methods=["GET", "HEAD"])
    app.add_route("/static/uploads/{file_path:path}", _redirect_legacy_upload, methods=["GET", "HEAD"])

    # Genuinely public assets only (logos, campus-life imagery used on
    # pre-login screens). Registered after the uploads routes above so those
    # take precedence.
    app.mount("/static", StaticFiles(directory=static_dir), name="static")

    # Rate limiting (authentication endpoints only) — see app/core/rate_limit.py
    app.state.limiter = limiter
    app.add_exception_handler(RateLimitExceeded, rate_limit_exceeded_handler)
    app.add_middleware(SlowAPIMiddleware)

    app.add_middleware(
        CORSMiddleware,
        allow_origins=settings.CORS_ORIGINS,
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
        # Outside production, allow any origin so LAN/emulator/device testing works.
        # In production the CORS_ORIGINS allow-list is authoritative — a catch-all
        # regex here would silently defeat it while allow_credentials=True.
        allow_origin_regex=None if settings.ENVIRONMENT == "production" else ".*",
    )

    register_exception_handlers(app)
    app.include_router(api_router, prefix="/api/v1")
    from app.api.v1.endpoints.dashboard import router as dashboard_router
    app.include_router(dashboard_router, prefix="/api/dashboard")



    return app


app = create_app()
