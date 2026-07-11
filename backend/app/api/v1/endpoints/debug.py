# File: app/api/v1/endpoints/debug.py

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from app.db.session import get_async_session
from app.db.repositories.auth_repository import AuthRepository

router = APIRouter()

@router.get("/debug/user/{email}", tags=["debug"])
async def get_user_info(email: str, db: AsyncSession = Depends(get_async_session)):
    """Return basic user info for debugging (excluding password)."""
    repo = AuthRepository(db)
    user = await repo.get_user_by_email(email)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return {
        "id": str(user.id),
        "email": user.email,
        "phone": user.phone,
        "is_deleted": user.is_deleted,
        "role": user.role.value,
        "full_name": user.full_name,
    }
