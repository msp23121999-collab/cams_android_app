# File: app/api/v1/endpoints/maintenance.py




import logging

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import text

from app.core.config import settings
from app.core.dependencies import role_required
from app.db.models.user import User, UserRole
from app.db.session import get_async_session

logger = logging.getLogger(__name__)

router = APIRouter()

# NOTE: POST /fix_schema was removed. It executed raw, unguarded ALTER TABLE
# statements to paper over schema drift between the models and the database.
# The drift it existed to patch is now fixed properly: migrations c9f21a7de401
# and d4e77b1c9a02 backfill the six tables and fifty columns that no migration
# had ever created, and app startup no longer calls create_all. Schema changes
# belong in Alembic migrations, not in a privileged runtime DDL endpoint.

@router.get("/test_connection", tags=["maintenance"])
async def test_connection(db: AsyncSession = Depends(get_async_session)):
    """Simple health-check that verifies DB connectivity.

    Intentionally unauthenticated so load balancers / uptime probes can call it.
    The driver error is logged but never returned: it can contain the database
    host, user, and other connection details.
    """
    try:
        await db.execute(text("SELECT 1"))
        return {"status": "ok", "message": "Database connection successful"}
    except Exception:
        logger.exception("Health check failed: database connectivity error")
        raise HTTPException(status_code=503, detail="Database unavailable")
