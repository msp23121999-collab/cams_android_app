"""Internship Drives — real SQLAlchemy-backed storage (previously flat JSON
files with zero authentication on any route)."""
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select

from app.core.dependencies import get_db_session, get_current_user, role_required
from app.db.models.user import User, UserRole
from app.db.models.internship import InternshipDrive, InternshipApplication
from app.db.repositories.student_repository import StudentRepository
from app.db.models.student import Student
from app.db.models.internship import PartnerCompany
from app.schemas.internship import (
    InternshipDriveResponse,
    InternshipDriveCreateRequest,
    InternshipApplicationRequest,
    InternshipApplicationResponse,
    InternshipApplicationReviewRequest,
    PartnerCompanyResponse,
    PartnerCompanyCreateRequest,
)

router = APIRouter()


@router.get("", response_model=list[InternshipDriveResponse])
async def get_drives(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session),
):
    """Any authenticated user can view active internship drives."""
    rows = await db.execute(
        select(InternshipDrive).where(InternshipDrive.is_deleted.is_(False)).order_by(InternshipDrive.created_at.desc())
    )
    return list(rows.scalars().all())


@router.post("", response_model=InternshipDriveResponse)
async def create_drive(
    payload: InternshipDriveCreateRequest,
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session),
):
    """Create a new internship drive. Admin/Principal only — previously this
    endpoint had NO authentication and replaced the entire drives list."""
    drive = InternshipDrive(
        company_name=payload.company_name,
        role=payload.role,
        package=payload.package,
        drive_date=payload.drive_date,
        status=payload.status,
        description=payload.description,
    )
    db.add(drive)
    await db.commit()
    await db.refresh(drive)
    return drive


@router.post("/apply", response_model=InternshipApplicationResponse)
async def apply_drive(
    payload: InternshipApplicationRequest,
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session),
):
    """Apply for an internship drive. Student identity is resolved server-side
    from the authenticated user — never trust a client-supplied student id."""
    student_repo = StudentRepository(db)
    student = await student_repo.get_student_by_user_id(current_user.id)
    if not student:
        raise HTTPException(status_code=404, detail="Student profile not found")

    drive_res = await db.execute(
        select(InternshipDrive).where(InternshipDrive.id == payload.drive_id, InternshipDrive.is_deleted.is_(False))
    )
    if not drive_res.scalar_one_or_none():
        raise HTTPException(status_code=404, detail="Internship drive not found")

    existing_res = await db.execute(
        select(InternshipApplication).where(
            InternshipApplication.drive_id == payload.drive_id,
            InternshipApplication.student_id == student.id,
            InternshipApplication.is_deleted.is_(False),
        )
    )
    if existing_res.scalar_one_or_none():
        raise HTTPException(status_code=400, detail="You have already applied to this drive")

    application = InternshipApplication(drive_id=payload.drive_id, student_id=student.id, status="Applied")
    db.add(application)
    await db.commit()
    await db.refresh(application)
    return application


async def _enrich_application(db: AsyncSession, application: InternshipApplication) -> InternshipApplicationResponse:
    student = await db.get(Student, application.student_id)
    student_name = None
    roll_no = student.roll_no if student else None
    if student:
        user = await db.get(User, student.user_id)
        student_name = user.full_name if user else None
    drive = await db.get(InternshipDrive, application.drive_id)
    return InternshipApplicationResponse(
        id=application.id,
        drive_id=application.drive_id,
        student_id=application.student_id,
        status=application.status,
        student_name=student_name,
        roll_no=roll_no,
        company_name=drive.company_name if drive else None,
        role=drive.role if drive else None,
    )


@router.get("/applications", response_model=list[InternshipApplicationResponse])
async def get_applications(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session),
):
    """Students see only their own applications; staff (faculty/HOD/principal/admin) see all.
    Previously this endpoint had NO authentication and returned everything."""
    if current_user.role == UserRole.STUDENT:
        student_repo = StudentRepository(db)
        student = await student_repo.get_student_by_user_id(current_user.id)
        if not student:
            raise HTTPException(status_code=404, detail="Student profile not found")
        rows = await db.execute(
            select(InternshipApplication).where(
                InternshipApplication.student_id == student.id,
                InternshipApplication.is_deleted.is_(False),
            )
        )
        return [await _enrich_application(db, a) for a in rows.scalars().all()]

    if current_user.role in (UserRole.ADMIN, UserRole.PRINCIPAL, UserRole.HOD, UserRole.FACULTY):
        rows = await db.execute(select(InternshipApplication).where(InternshipApplication.is_deleted.is_(False)))
        return [await _enrich_application(db, a) for a in rows.scalars().all()]

    raise HTTPException(status_code=403, detail="Insufficient permissions")


@router.patch("/applications/{application_id}", response_model=InternshipApplicationResponse)
async def review_application(
    application_id: str,
    payload: InternshipApplicationReviewRequest,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL, UserRole.ADMIN])),
    db: AsyncSession = Depends(get_db_session),
):
    """Faculty/staff review (shortlist/select/reject) a student's internship application."""
    allowed_statuses = {"Applied", "Shortlisted", "Selected", "Rejected"}
    if payload.status not in allowed_statuses:
        raise HTTPException(status_code=400, detail=f"status must be one of {sorted(allowed_statuses)}")

    application = await db.get(InternshipApplication, application_id)
    if not application or application.is_deleted:
        raise HTTPException(status_code=404, detail="Application not found")

    application.status = payload.status
    await db.commit()
    await db.refresh(application)
    return await _enrich_application(db, application)


@router.get("/partners", response_model=list[PartnerCompanyResponse])
async def list_partners(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session),
):
    """Any authenticated user can view the partner company directory."""
    rows = await db.execute(
        select(PartnerCompany).where(PartnerCompany.is_deleted.is_(False)).order_by(PartnerCompany.name)
    )
    return list(rows.scalars().all())


@router.post("/partners", response_model=PartnerCompanyResponse)
async def create_partner(
    payload: PartnerCompanyCreateRequest,
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.PRINCIPAL, UserRole.HOD, UserRole.FACULTY])),
    db: AsyncSession = Depends(get_db_session),
):
    partner = PartnerCompany(
        name=payload.name,
        industry=payload.industry,
        status=payload.status,
        contact_email=payload.contact_email,
        contact_phone=payload.contact_phone,
        notes=payload.notes,
    )
    db.add(partner)
    await db.commit()
    await db.refresh(partner)
    return partner


@router.put("/partners/{partner_id}", response_model=PartnerCompanyResponse)
async def update_partner(
    partner_id: str,
    payload: PartnerCompanyCreateRequest,
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.PRINCIPAL, UserRole.HOD, UserRole.FACULTY])),
    db: AsyncSession = Depends(get_db_session),
):
    partner = await db.get(PartnerCompany, partner_id)
    if not partner or partner.is_deleted:
        raise HTTPException(status_code=404, detail="Partner not found")
    partner.name = payload.name
    partner.industry = payload.industry
    partner.status = payload.status
    partner.contact_email = payload.contact_email
    partner.contact_phone = payload.contact_phone
    partner.notes = payload.notes
    await db.commit()
    await db.refresh(partner)
    return partner


@router.delete("/partners/{partner_id}")
async def delete_partner(
    partner_id: str,
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session),
):
    partner = await db.get(PartnerCompany, partner_id)
    if not partner or partner.is_deleted:
        raise HTTPException(status_code=404, detail="Partner not found")
    partner.is_deleted = True
    await db.commit()
    return {"ok": True}
