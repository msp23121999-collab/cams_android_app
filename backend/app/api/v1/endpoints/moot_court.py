"""Moot Court Memorial draft endpoints.

Student-facing, self-scoped CRUD for drafting moot court memorials.
Mounted at /students/moot-court.
"""
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select

from app.core.dependencies import get_db_session, role_required
from app.db.models.user import User, UserRole
from app.db.models.moot_court import MootCourtMemorial
from app.db.repositories.student_repository import StudentRepository
from app.schemas.moot_court import (
    MootCourtMemorialResponse,
    MootCourtMemorialCreateRequest,
    MootCourtMemorialUpdateRequest,
)

router = APIRouter()


async def _get_student_or_404(current_user: User, db: AsyncSession):
    student_repo = StudentRepository(db)
    student = await student_repo.get_student_by_user_id(current_user.id)
    if not student:
        raise HTTPException(status_code=404, detail="Student profile not found")
    return student


@router.get("/memorials", response_model=list[MootCourtMemorialResponse])
async def list_my_memorials(
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session),
):
    student = await _get_student_or_404(current_user, db)
    rows = await db.execute(
        select(MootCourtMemorial)
        .where(MootCourtMemorial.student_id == student.id, MootCourtMemorial.is_deleted.is_(False))
        .order_by(MootCourtMemorial.updated_at.desc())
    )
    return rows.scalars().all()


@router.post("/memorials", response_model=MootCourtMemorialResponse)
async def create_memorial(
    payload: MootCourtMemorialCreateRequest,
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session),
):
    student = await _get_student_or_404(current_user, db)
    memorial = MootCourtMemorial(
        student_id=student.id,
        title=payload.title,
        case_name=payload.case_name,
        content=payload.content,
        status=payload.status or "draft",
    )
    db.add(memorial)
    await db.commit()
    await db.refresh(memorial)
    return memorial


@router.put("/memorials/{memorial_id}", response_model=MootCourtMemorialResponse)
async def update_memorial(
    memorial_id: str,
    payload: MootCourtMemorialUpdateRequest,
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session),
):
    student = await _get_student_or_404(current_user, db)
    res = await db.execute(
        select(MootCourtMemorial).where(
            MootCourtMemorial.id == memorial_id, MootCourtMemorial.is_deleted.is_(False)
        )
    )
    memorial = res.scalar_one_or_none()
    if not memorial:
        raise HTTPException(status_code=404, detail="Memorial not found")
    if memorial.student_id != student.id:
        raise HTTPException(status_code=403, detail="You do not have permission to modify this memorial")

    if payload.title is not None:
        memorial.title = payload.title
    if payload.case_name is not None:
        memorial.case_name = payload.case_name
    if payload.content is not None:
        memorial.content = payload.content
    if payload.status is not None:
        memorial.status = payload.status

    await db.commit()
    await db.refresh(memorial)
    return memorial


@router.delete("/memorials/{memorial_id}")
async def delete_memorial(
    memorial_id: str,
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session),
):
    student = await _get_student_or_404(current_user, db)
    res = await db.execute(
        select(MootCourtMemorial).where(
            MootCourtMemorial.id == memorial_id, MootCourtMemorial.is_deleted.is_(False)
        )
    )
    memorial = res.scalar_one_or_none()
    if not memorial:
        raise HTTPException(status_code=404, detail="Memorial not found")
    if memorial.student_id != student.id:
        raise HTTPException(status_code=403, detail="You do not have permission to delete this memorial")

    from datetime import datetime, timezone
    memorial.is_deleted = True
    memorial.deleted_at = datetime.now(timezone.utc)
    await db.commit()
    return {"detail": "Memorial deleted"}
