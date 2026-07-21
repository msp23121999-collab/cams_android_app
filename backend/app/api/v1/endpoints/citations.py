"""Saved Citations endpoints.

Student-facing, self-scoped CRUD for bookmarking case citations.
Mounted at /students/citations.
"""
from datetime import datetime, timezone

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select

from app.core.dependencies import get_db_session, role_required
from app.db.models.user import User, UserRole
from app.db.models.citation import SavedCitation
from app.db.repositories.student_repository import StudentRepository
from app.schemas.citation import SavedCitationResponse, SavedCitationCreateRequest

router = APIRouter()


async def _get_student_or_404(current_user: User, db: AsyncSession):
    student_repo = StudentRepository(db)
    student = await student_repo.get_student_by_user_id(current_user.id)
    if not student:
        raise HTTPException(status_code=404, detail="Student profile not found")
    return student


@router.get("", response_model=list[SavedCitationResponse])
async def list_my_citations(
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session),
):
    student = await _get_student_or_404(current_user, db)
    rows = await db.execute(
        select(SavedCitation)
        .where(SavedCitation.student_id == student.id, SavedCitation.is_deleted.is_(False))
        .order_by(SavedCitation.created_at.desc())
    )
    return rows.scalars().all()


@router.post("", response_model=SavedCitationResponse)
async def create_citation(
    payload: SavedCitationCreateRequest,
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session),
):
    student = await _get_student_or_404(current_user, db)
    citation = SavedCitation(
        student_id=student.id,
        case_name=payload.case_name,
        citation_text=payload.citation_text,
        note=payload.note,
    )
    db.add(citation)
    await db.commit()
    await db.refresh(citation)
    return citation


@router.delete("/{citation_id}")
async def delete_citation(
    citation_id: str,
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session),
):
    student = await _get_student_or_404(current_user, db)
    res = await db.execute(
        select(SavedCitation).where(SavedCitation.id == citation_id, SavedCitation.is_deleted.is_(False))
    )
    citation = res.scalar_one_or_none()
    if not citation:
        raise HTTPException(status_code=404, detail="Citation not found")
    if citation.student_id != student.id:
        raise HTTPException(status_code=403, detail="You do not have permission to delete this citation")

    citation.is_deleted = True
    citation.deleted_at = datetime.now(timezone.utc)
    await db.commit()
    return {"detail": "Citation deleted"}
