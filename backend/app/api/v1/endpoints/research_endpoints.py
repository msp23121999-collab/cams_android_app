import os
import shutil
import uuid
from datetime import date
from fastapi import APIRouter, Depends, HTTPException, UploadFile, File, Form, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select

from app.core.dependencies import get_current_user, get_db_session, role_required
from app.db.models.user import User, UserRole
from app.db.models.research import ProofStatus
from app.schemas.research_schemas import (
    ResearchPlanCreate, ResearchPlanResponse,
    ProgressUpdateCreate, ProgressUpdateResponse,
    ProofSubmitRequest, ProofResponse,
    VerificationRequest, PrincipalComplianceResponse
)
from app.services.research_service import ResearchService

router = APIRouter()

@router.post("/plan", response_model=ResearchPlanResponse)
async def create_research_plan(
    payload: ResearchPlanCreate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session)
) -> ResearchPlanResponse:
    service = ResearchService(db)
    plan = await service.create_plan(current_user.id, payload.model_dump())
    
    return ResearchPlanResponse(
        id=plan.id,
        faculty_id=plan.faculty_id,
        title=plan.title,
        area=plan.area,
        target_journal_conference=plan.target_journal_conference,
        type=plan.type,
        start_date=plan.start_date,
        expected_completion_date=plan.expected_completion_date,
        objectives=plan.objectives,
        abstract_summary=plan.abstract_summary,
        status=plan.status,
        cycle_start_date=plan.cycle_start_date,
        cycle_due_date=plan.cycle_due_date,
        faculty_name=current_user.full_name,
        latest_progress_percentage=0,
        latest_progress_stage=None
    )

@router.get("/plans", response_model=list[ResearchPlanResponse])
async def get_my_plans(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session)
) -> list[ResearchPlanResponse]:
    service = ResearchService(db)
    plans = await service.get_faculty_plans(current_user.id)
    
    res_list = []
    for p in plans:
        latest_pct = 0
        latest_stage = None
        if p.progress_updates:
            sorted_updates = sorted(p.progress_updates, key=lambda x: (x.progress_date, x.created_at or date.min), reverse=True)
            if sorted_updates:
                latest_pct = sorted_updates[0].percentage_completed
                latest_stage = sorted_updates[0].current_stage
        
        if p.status == "COMPLETED" or p.status == "VERIFIED":
            latest_pct = 100
            latest_stage = "Published"

        res_list.append(
            ResearchPlanResponse(
                id=p.id,
                faculty_id=p.faculty_id,
                title=p.title,
                area=p.area,
                target_journal_conference=p.target_journal_conference,
                type=p.type,
                start_date=p.start_date,
                expected_completion_date=p.expected_completion_date,
                objectives=p.objectives,
                abstract_summary=p.abstract_summary,
                status=p.status,
                cycle_start_date=p.cycle_start_date,
                cycle_due_date=p.cycle_due_date,
                faculty_name=current_user.full_name,
                latest_progress_percentage=latest_pct,
                latest_progress_stage=latest_stage
            )
        )
    return res_list

@router.post("/progress/{plan_id}", response_model=ProgressUpdateResponse)
async def submit_progress_update(
    plan_id: str,
    payload: ProgressUpdateCreate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session)
) -> ProgressUpdateResponse:
    service = ResearchService(db)
    update = await service.submit_progress(plan_id, payload.model_dump())
    
    return ProgressUpdateResponse(
        id=update.id,
        plan_id=update.plan_id,
        progress_date=update.progress_date,
        current_stage=update.current_stage,
        percentage_completed=update.percentage_completed,
        work_completed=update.work_completed,
        remarks=update.remarks
    )

@router.get("/progress/{plan_id}", response_model=list[ProgressUpdateResponse])
async def get_plan_progress_history(
    plan_id: str,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session)
) -> list[ProgressUpdateResponse]:
    service = ResearchService(db)
    updates = await service.get_progress_updates(plan_id)
    
    return [
        ProgressUpdateResponse(
            id=u.id,
            plan_id=u.plan_id,
            progress_date=u.progress_date,
            current_stage=u.current_stage,
            percentage_completed=u.percentage_completed,
            work_completed=u.work_completed,
            remarks=u.remarks
        )
        for u in updates
    ]

@router.post("/proof/{plan_id}", response_model=ProofResponse)
async def upload_proof(
    plan_id: str,
    publication_date: str = Form(...),
    journal_name: str = Form(...),
    issn_isbn: str = Form(...),
    doi_number: str | None = Form(None),
    publication_link: str | None = Form(None),
    file: UploadFile = File(...),
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session)
) -> ProofResponse:
    # Validate file extension
    ext = os.path.splitext(file.filename)[1].lower()
    if ext not in [".pdf", ".jpg", ".jpeg", ".png", ".doc", ".docx", ".zip"]:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Unsupported file format. Please upload PDF, Word document, Image, or Zip archive."
        )

    # Ensure uploads dir exists
    upload_dir = os.path.join("app", "static", "uploads", "research")
    os.makedirs(upload_dir, exist_ok=True)

    # Save proof file
    filename = f"proof_{uuid.uuid4()}{ext}"
    filepath = os.path.join(upload_dir, filename)
    with open(filepath, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)
    proof_file_url = f"/static/uploads/research/{filename}"

    # Parse date
    try:
        pub_date = date.fromisoformat(publication_date)
    except ValueError:
        raise HTTPException(status_code=400, detail="Invalid date format. Use YYYY-MM-DD.")

    proof_data = {
        "publication_date": pub_date,
        "journal_name": journal_name,
        "issn_isbn": issn_isbn,
        "doi_number": doi_number,
        "publication_link": publication_link,
        "proof_file_url": proof_file_url
    }

    service = ResearchService(db)
    proof = await service.submit_proof(plan_id, proof_data)

    return ProofResponse(
        id=proof.id,
        plan_id=proof.plan_id,
        publication_date=proof.publication_date,
        journal_name=proof.journal_name,
        issn_isbn=proof.issn_isbn,
        doi_number=proof.doi_number,
        publication_link=proof.publication_link,
        proof_file_url=proof.proof_file_url,
        status=proof.status,
        remarks=proof.remarks
    )

@router.get("/hod/monitoring", response_model=list[dict])
async def hod_monitoring(
    current_user: User = Depends(role_required([UserRole.HOD, UserRole.ADMIN, UserRole.SUPER_ADMIN])),
    db: AsyncSession = Depends(get_db_session)
) -> list[dict]:
    service = ResearchService(db)
    return await service.get_hod_monitoring(current_user.id)

@router.get("/hod/pending-proofs", response_model=list[dict])
async def hod_pending_proofs(
    current_user: User = Depends(role_required([UserRole.HOD, UserRole.ADMIN, UserRole.SUPER_ADMIN])),
    db: AsyncSession = Depends(get_db_session)
) -> list[dict]:
    service = ResearchService(db)
    return await service.get_hod_pending_proofs(current_user.id)

@router.post("/hod/verify/{proof_id}", response_model=ProofResponse)
async def hod_verify_proof(
    proof_id: str,
    payload: VerificationRequest,
    current_user: User = Depends(role_required([UserRole.HOD, UserRole.ADMIN, UserRole.SUPER_ADMIN])),
    db: AsyncSession = Depends(get_db_session)
) -> ProofResponse:
    service = ResearchService(db)
    proof = await service.verify_proof(proof_id, current_user.id, payload.status, payload.remarks)
    
    return ProofResponse(
        id=proof.id,
        plan_id=proof.plan_id,
        publication_date=proof.publication_date,
        journal_name=proof.journal_name,
        issn_isbn=proof.issn_isbn,
        doi_number=proof.doi_number,
        publication_link=proof.publication_link,
        proof_file_url=proof.proof_file_url,
        status=proof.status,
        remarks=proof.remarks
    )

@router.get("/principal/compliance", response_model=PrincipalComplianceResponse)
async def principal_compliance(
    current_user: User = Depends(role_required([UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.SUPER_ADMIN])),
    db: AsyncSession = Depends(get_db_session)
) -> PrincipalComplianceResponse:
    service = ResearchService(db)
    data = await service.get_principal_compliance()
    return PrincipalComplianceResponse(**data)

@router.post("/cron/check-deadlines")
async def trigger_compliance_cron(
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.SUPER_ADMIN, UserRole.PRINCIPAL, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
):
    service = ResearchService(db)
    flagged = await service.run_compliance_cron()
    return {"detail": "Compliance evaluation finished.", "flagged_faculties_count": flagged}

@router.delete("/plan/{plan_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_research_plan(
    plan_id: str,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session)
):
    service = ResearchService(db)
    await service.delete_plan(plan_id, current_user.id)
    return None


