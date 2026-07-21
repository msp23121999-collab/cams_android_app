"""Hall ticket endpoints.

Student-facing endpoints are mounted at /students/hall-tickets (matching what
the existing Android app already calls). Admin generation/upload endpoints
are mounted separately at /admin/hall-tickets.
"""
import os
import uuid

from fastapi import APIRouter, Depends, HTTPException, UploadFile, File
from fastapi.responses import FileResponse
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select

from app.core.dependencies import get_db_session, role_required
from app.db.models.user import User, UserRole
from app.db.models.student import Student
from app.db.models.hall_ticket import HallTicket
from app.db.repositories.student_repository import StudentRepository
from app.schemas.hall_ticket import HallTicketResponse, GenerateHallTicketsRequest

student_router = APIRouter()
admin_router = APIRouter()

_BACKEND_DIR = os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))
UPLOADS_DIR = os.path.join(_BACKEND_DIR, "static", "uploads", "hall-tickets")


def _to_response(ticket: HallTicket, student_name: str | None = None) -> HallTicketResponse:
    return HallTicketResponse(
        id=ticket.id,
        student_id=ticket.student_id,
        exam_id=ticket.exam_id,
        exam_name=ticket.exam_name,
        is_eligible=ticket.is_eligible,
        ineligibility_reason=ticket.ineligibility_reason,
        is_issued=ticket.is_issued,
        file_url=ticket.file_url,
        issued_at=ticket.issued_at,
        student_signature_url=ticket.student_signature_url,
        principal_signature_url=ticket.principal_signature_url,
        coe_signature_url=ticket.coe_signature_url,
        student_name=student_name,
        exam_center=ticket.exam_center,
        exam_date=ticket.exam_date,
    )


# ─────────────────────────────────────────────────────────
#  STUDENT ENDPOINTS — mounted at /students/hall-tickets
# ─────────────────────────────────────────────────────────

@student_router.get("/", response_model=list[HallTicketResponse])
async def list_my_hall_tickets(
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session),
):
    student_repo = StudentRepository(db)
    student = await student_repo.get_student_by_user_id(current_user.id)
    if not student:
        raise HTTPException(status_code=404, detail="Student profile not found")

    rows = await db.execute(
        select(HallTicket).where(HallTicket.student_id == student.id, HallTicket.is_deleted.is_(False))
    )
    tickets = rows.scalars().all()
    return [_to_response(t, student_name=current_user.full_name) for t in tickets]


@student_router.get("/{ticket_id}/download")
async def download_hall_ticket(
    ticket_id: str,
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session),
):
    student_repo = StudentRepository(db)
    student = await student_repo.get_student_by_user_id(current_user.id)
    if not student:
        raise HTTPException(status_code=404, detail="Student profile not found")

    ticket_res = await db.execute(
        select(HallTicket).where(HallTicket.id == ticket_id, HallTicket.is_deleted.is_(False))
    )
    ticket = ticket_res.scalar_one_or_none()
    if not ticket:
        raise HTTPException(status_code=404, detail="Hall ticket not found")
    if ticket.student_id != student.id:
        raise HTTPException(status_code=403, detail="You do not have permission to access this hall ticket")
    if not ticket.is_issued or not ticket.file_url:
        raise HTTPException(status_code=404, detail="Hall ticket has not been issued yet")

    filename = os.path.basename(ticket.file_url)
    full_path = os.path.join(UPLOADS_DIR, filename)
    if not os.path.isfile(full_path):
        raise HTTPException(status_code=404, detail="Hall ticket file not found on server")

    return FileResponse(full_path, filename=f"hall_ticket_{ticket.id}.pdf")


# ─────────────────────────────────────────────────────────
#  ADMIN ENDPOINTS — mounted at /admin/hall-tickets
# ─────────────────────────────────────────────────────────

@admin_router.get("/", response_model=list[HallTicketResponse])
async def list_all_hall_tickets(
    exam_name: str | None = None,
    is_issued: bool | None = None,
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session),
):
    """Admin/Principal: list every hall ticket, newest first, with the student's
    name resolved so the Exam Management screen can show a usable list."""
    query = select(HallTicket).where(HallTicket.is_deleted.is_(False))
    if exam_name:
        query = query.where(HallTicket.exam_name == exam_name)
    if is_issued is not None:
        query = query.where(HallTicket.is_issued.is_(is_issued))
    query = query.order_by(HallTicket.created_at.desc())

    tickets = (await db.execute(query)).scalars().all()
    if not tickets:
        return []

    # Resolve student names in bulk (no N+1).
    student_ids = {t.student_id for t in tickets}
    rows = (await db.execute(
        select(Student.id, User.full_name)
        .join(User, Student.user_id == User.id)
        .where(Student.id.in_(student_ids))
    )).all()
    name_by_student_id = {sid: name for sid, name in rows}

    return [_to_response(t, student_name=name_by_student_id.get(t.student_id)) for t in tickets]


@admin_router.post("/generate", response_model=list[HallTicketResponse])
async def generate_hall_tickets(
    payload: GenerateHallTicketsRequest,
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session),
):
    if not payload.student_ids:
        raise HTTPException(status_code=400, detail="student_ids is required")

    students_res = await db.execute(
        select(Student).where(Student.id.in_(payload.student_ids), Student.is_deleted.is_(False))
    )
    found_students = students_res.scalars().all()
    found_ids = {s.id for s in found_students}
    missing = set(payload.student_ids) - found_ids
    if missing:
        raise HTTPException(status_code=404, detail=f"Unknown student_ids: {sorted(missing)}")

    created = []
    for student_id in payload.student_ids:
        ticket = HallTicket(
            student_id=student_id,
            exam_id=payload.exam_id,
            exam_name=payload.exam_name,
            is_eligible=payload.is_eligible,
            ineligibility_reason=payload.ineligibility_reason,
            is_issued=False,
            exam_center=payload.exam_center,
            exam_date=payload.exam_date,
        )
        db.add(ticket)
        created.append(ticket)

    await db.commit()
    for t in created:
        await db.refresh(t)

    return [_to_response(t) for t in created]


@admin_router.post("/{ticket_id}/upload", response_model=HallTicketResponse)
async def upload_hall_ticket_pdf(
    ticket_id: str,
    file: UploadFile = File(...),
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session),
):
    ticket_res = await db.execute(
        select(HallTicket).where(HallTicket.id == ticket_id, HallTicket.is_deleted.is_(False))
    )
    ticket = ticket_res.scalar_one_or_none()
    if not ticket:
        raise HTTPException(status_code=404, detail="Hall ticket not found")

    os.makedirs(UPLOADS_DIR, exist_ok=True)
    ext = os.path.splitext(file.filename)[1] if file.filename else ".pdf"
    safe_filename = f"hallticket_{ticket.id}_{uuid.uuid4().hex}{ext}"
    full_path = os.path.join(UPLOADS_DIR, safe_filename)

    with open(full_path, "wb") as f:
        f.write(await file.read())

    from datetime import datetime, timezone
    ticket.file_url = f"/api/v1/files/hall-tickets/{safe_filename}"
    ticket.is_issued = True
    ticket.issued_at = datetime.now(timezone.utc)
    await db.commit()
    await db.refresh(ticket)

    return _to_response(ticket)
