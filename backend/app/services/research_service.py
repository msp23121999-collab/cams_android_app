from datetime import date, timedelta
from fastapi import HTTPException, status
from sqlalchemy import select, and_, or_, func
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import selectinload

from app.db.models.research import ResearchPlan, ResearchProgressUpdate, PublicationProof, ResearchVerification, ResearchPlanStatus, ProofStatus
from app.db.models.user import User, UserRole
from app.db.models.faculty import FacultyProfile
from app.db.models.academic import Department
from app.db.models.communication import Notification

def get_current_cycle_dates(target_date: date) -> tuple[date, date]:
    """Calculate the 3-month window for the given date (standard academic quarters)."""
    year = target_date.year
    if target_date.month in [1, 2, 3]:
        return date(year, 1, 1), date(year, 3, 31)
    elif target_date.month in [4, 5, 6]:
        return date(year, 4, 1), date(year, 6, 30)
    elif target_date.month in [7, 8, 9]:
        return date(year, 7, 1), date(year, 9, 30)
    else:
        return date(year, 10, 1), date(year, 12, 31)

class ResearchService:
    def __init__(self, db: AsyncSession) -> None:
        self.db = db

    async def create_plan(self, faculty_id: str, plan_data: dict) -> ResearchPlan:
        # Determine 3-month cycle dates
        cycle_start, cycle_due = get_current_cycle_dates(date.today())

        plan = ResearchPlan(
            faculty_id=faculty_id,
            title=plan_data["title"],
            area=plan_data["area"],
            target_journal_conference=plan_data["target_journal_conference"],
            type=plan_data["type"],
            start_date=plan_data["start_date"],
            expected_completion_date=plan_data["expected_completion_date"],
            objectives=plan_data["objectives"],
            abstract_summary=plan_data["abstract_summary"],
            status=plan_data.get("status", ResearchPlanStatus.DRAFT),
            cycle_start_date=cycle_start,
            cycle_due_date=cycle_due
        )
        self.db.add(plan)
        
        # Send Notification to HOD if plan is submitted
        if plan.status == ResearchPlanStatus.SUBMITTED:
            # Find HOD for this faculty's department
            user_stmt = select(User).where(User.id == faculty_id)
            user_res = await self.db.execute(user_stmt)
            user = user_res.scalar_one_or_none()
            
            if user and user.department_id:
                hod_stmt = select(User).where(User.department_id == user.department_id, User.role == UserRole.HOD)
                hod_res = await self.db.execute(hod_stmt)
                hod = hod_res.scalar_one_or_none()
                if hod:
                    notif = Notification(
                        user_id=hod.id,
                        type="info",
                        message=f"New research plan submitted by {user.full_name}: '{plan.title}'",
                        is_read=False,
                        sent_via="In-App"
                    )
                    self.db.add(notif)

        await self.db.commit()
        await self.db.refresh(plan)
        return plan

    async def get_faculty_plans(self, faculty_id: str) -> list[ResearchPlan]:
        stmt = select(ResearchPlan).options(
            selectinload(ResearchPlan.progress_updates)
        ).where(
            ResearchPlan.faculty_id == faculty_id,
            ResearchPlan.is_deleted.is_(False)
        ).order_by(ResearchPlan.created_at.desc())
        res = await self.db.execute(stmt)
        return list(res.scalars().all())

    async def get_plan_details(self, plan_id: str) -> ResearchPlan:
        stmt = select(ResearchPlan).where(
            ResearchPlan.id == plan_id,
            ResearchPlan.is_deleted.is_(False)
        )
        res = await self.db.execute(stmt)
        plan = res.scalar_one_or_none()
        if not plan:
            raise HTTPException(status_code=404, detail="Research plan not found")
        return plan

    async def submit_progress(self, plan_id: str, update_data: dict) -> ResearchProgressUpdate:
        plan = await self.get_plan_details(plan_id)
        
        update = ResearchProgressUpdate(
            plan_id=plan_id,
            progress_date=date.today(),
            current_stage=update_data["current_stage"],
            percentage_completed=update_data["percentage_completed"],
            work_completed=update_data["work_completed"],
            remarks=update_data.get("remarks")
        )
        self.db.add(update)
        
        # HOD Notification
        fac_stmt = select(User).where(User.id == plan.faculty_id)
        fac_res = await self.db.execute(fac_stmt)
        fac = fac_res.scalar_one_or_none()
        if fac and fac.department_id:
            hod_stmt = select(User).where(User.department_id == fac.department_id, User.role == UserRole.HOD)
            hod_res = await self.db.execute(hod_stmt)
            hod = hod_res.scalar_one_or_none()
            if hod:
                notif = Notification(
                    user_id=hod.id,
                    type="info",
                    message=f"Progress update submitted by {fac.full_name} ({update.percentage_completed}% completed): '{plan.title}'",
                    is_read=False,
                    sent_via="In-App"
                )
                self.db.add(notif)

        await self.db.commit()
        await self.db.refresh(update)
        return update

    async def get_progress_updates(self, plan_id: str) -> list[ResearchProgressUpdate]:
        stmt = select(ResearchProgressUpdate).where(
            ResearchProgressUpdate.plan_id == plan_id,
            ResearchProgressUpdate.is_deleted.is_(False)
        ).order_by(ResearchProgressUpdate.progress_date.desc())
        res = await self.db.execute(stmt)
        return list(res.scalars().all())

    async def submit_proof(self, plan_id: str, proof_data: dict) -> PublicationProof:
        plan = await self.get_plan_details(plan_id)
        
        proof = PublicationProof(
            plan_id=plan_id,
            publication_date=proof_data["publication_date"],
            journal_name=proof_data["journal_name"],
            issn_isbn=proof_data["issn_isbn"],
            doi_number=proof_data.get("doi_number"),
            publication_link=proof_data.get("publication_link"),
            proof_file_url=proof_data["proof_file_url"],
            status=ProofStatus.PENDING_VERIFICATION
        )
        self.db.add(proof)

        # Notify HOD
        fac_stmt = select(User).where(User.id == plan.faculty_id)
        fac_res = await self.db.execute(fac_stmt)
        fac = fac_res.scalar_one_or_none()
        if fac and fac.department_id:
            hod_stmt = select(User).where(User.department_id == fac.department_id, User.role == UserRole.HOD)
            hod_res = await self.db.execute(hod_stmt)
            hod = hod_res.scalar_one_or_none()
            if hod:
                notif = Notification(
                    user_id=hod.id,
                    type="info",
                    message=f"Publication proof uploaded by {fac.full_name}: '{plan.title}'",
                    is_read=False,
                    sent_via="In-App"
                )
                self.db.add(notif)

        await self.db.commit()
        await self.db.refresh(proof)
        return proof

    async def get_hod_monitoring(self, hod_id: str) -> list[dict]:
        hod_stmt = select(User).where(User.id == hod_id)
        hod_res = await self.db.execute(hod_stmt)
        hod = hod_res.scalar_one_or_none()
        if not hod:
            return []

        # Find all departments supervised by this HOD
        dept_stmt = select(Department.id).where(Department.hod_id == hod_id)
        dept_res = await self.db.execute(dept_stmt)
        dept_ids = [row[0] for row in dept_res.all()]
        if hod.department_id:
            dept_ids.append(hod.department_id)
        dept_ids = list(set(dept_ids))

        if not dept_ids:
            return []

        stmt = select(ResearchPlan).join(User, User.id == ResearchPlan.faculty_id).where(
            User.department_id.in_(dept_ids),
            User.role == UserRole.FACULTY,
            ResearchPlan.status != ResearchPlanStatus.DRAFT,
            ResearchPlan.is_deleted.is_(False)
        ).options(selectinload(ResearchPlan.faculty))
        
        res = await self.db.execute(stmt)
        plans = res.scalars().all()
        
        results = []
        for p in plans:
            results.append({
                "id": p.id,
                "faculty_name": p.faculty.full_name,
                "title": p.title,
                "type": p.type,
                "area": p.area,
                "target_journal_conference": p.target_journal_conference,
                "expected_completion_date": p.expected_completion_date,
                "cycle_due_date": p.cycle_due_date,
                "status": p.status,
                "days_overdue": max(0, (date.today() - p.cycle_due_date).days) if date.today() > p.cycle_due_date and p.status != ResearchPlanStatus.COMPLETED else 0
            })
        return results

    async def get_hod_pending_proofs(self, hod_id: str) -> list[dict]:
        hod_stmt = select(User).where(User.id == hod_id)
        hod_res = await self.db.execute(hod_stmt)
        hod = hod_res.scalar_one_or_none()
        if not hod:
            return []

        # Find all departments supervised by this HOD
        dept_stmt = select(Department.id).where(Department.hod_id == hod_id)
        dept_res = await self.db.execute(dept_stmt)
        dept_ids = [row[0] for row in dept_res.all()]
        if hod.department_id:
            dept_ids.append(hod.department_id)
        dept_ids = list(set(dept_ids))

        if not dept_ids:
            return []

        stmt = select(PublicationProof).join(ResearchPlan).join(User, User.id == ResearchPlan.faculty_id).where(
            User.department_id.in_(dept_ids),
            PublicationProof.status == ProofStatus.PENDING_VERIFICATION,
            PublicationProof.is_deleted.is_(False)
        ).options(selectinload(PublicationProof.plan).selectinload(ResearchPlan.faculty))

        res = await self.db.execute(stmt)
        proofs = res.scalars().all()
        
        return [
            {
                "proof_id": pr.id,
                "plan_id": pr.plan_id,
                "faculty_name": pr.plan.faculty.full_name,
                "title": pr.plan.title,
                "journal_name": pr.journal_name,
                "issn_isbn": pr.issn_isbn,
                "publication_date": pr.publication_date,
                "proof_file_url": pr.proof_file_url,
                "doi_number": pr.doi_number,
                "publication_link": pr.publication_link
            }
            for pr in proofs
        ]

    async def verify_proof(self, proof_id: str, verifier_id: str, status_val: ProofStatus, remarks: str | None = None) -> PublicationProof:
        stmt = select(PublicationProof).where(PublicationProof.id == proof_id).options(selectinload(PublicationProof.plan))
        res = await self.db.execute(stmt)
        proof = res.scalar_one_or_none()
        if not proof:
            raise HTTPException(status_code=404, detail="Proof submission not found")

        proof.status = status_val
        proof.remarks = remarks

        verification = ResearchVerification(
            proof_id=proof_id,
            verified_by=verifier_id,
            status=status_val,
            remarks=remarks
        )
        self.db.add(verification)

        # Update parent plan status if verified
        if status_val == ProofStatus.VERIFIED:
            proof.plan.status = ResearchPlanStatus.COMPLETED
            # Also update faculty compliance status on user/profile if desired
            prof_stmt = select(FacultyProfile).where(FacultyProfile.user_id == proof.plan.faculty_id)
            prof_res = await self.db.execute(prof_stmt)
            profile = prof_res.scalar_one_or_none()
            if profile:
                profile.employment_status = "Active"  # reset pending status

        # Send notification to Faculty
        notif = Notification(
            user_id=proof.plan.faculty_id,
            type="info",
            message=f"Your publication proof for '{proof.plan.title}' has been {status_val}.",
            is_read=False,
            sent_via="In-App"
        )
        self.db.add(notif)

        await self.db.commit()
        await self.db.refresh(proof)
        return proof

    async def get_principal_compliance(self) -> dict:
        # Get active faculties
        facs_stmt = select(User).where(User.role == UserRole.FACULTY, User.is_deleted.is_(False)).options(selectinload(User.department))
        facs_res = await self.db.execute(facs_stmt)
        faculties = facs_res.scalars().all()
        
        completed_count = 0
        pending_count = 0
        overdue_count = 0
        
        dept_wise_map = {}
        overdue_faculty_list = []

        cycle_start, cycle_due = get_current_cycle_dates(date.today())

        for fac in faculties:
            dept_name = fac.department.name if fac.department else "General"
            if dept_name not in dept_wise_map:
                dept_wise_map[dept_name] = {"completed": 0, "pending": 0, "overdue": 0}
            
            # Check if has completed publication in current cycle
            comp_stmt = select(ResearchPlan).where(
                ResearchPlan.faculty_id == fac.id,
                ResearchPlan.status == ResearchPlanStatus.COMPLETED,
                ResearchPlan.cycle_start_date == cycle_start,
                ResearchPlan.is_deleted.is_(False)
            )
            comp_res = await self.db.execute(comp_stmt)
            has_completed = comp_res.scalars().first() is not None
            
            if has_completed:
                completed_count += 1
                dept_wise_map[dept_name]["completed"] += 1
            else:
                # Check expected status
                if date.today() > cycle_due:
                    overdue_count += 1
                    dept_wise_map[dept_name]["overdue"] += 1
                    
                    # Get latest plan if any
                    latest_plan_stmt = select(ResearchPlan).where(
                        ResearchPlan.faculty_id == fac.id,
                        ResearchPlan.cycle_start_date == cycle_start,
                        ResearchPlan.is_deleted.is_(False)
                    ).order_by(ResearchPlan.created_at.desc())
                    latest_plan_res = await self.db.execute(latest_plan_stmt)
                    latest_plan = latest_plan_res.scalars().first()
                    
                    overdue_faculty_list.append({
                        "faculty_name": fac.full_name,
                        "faculty_id": fac.id,
                        "department": dept_name,
                        "publication_title": latest_plan.title if latest_plan else "No Plan Submitted",
                        "due_date": cycle_due.strftime("%d-%m-%Y"),
                        "days_overdue": (date.today() - cycle_due).days
                    })
                else:
                    pending_count += 1
                    dept_wise_map[dept_name]["pending"] += 1

        dept_wise = [
            {
                "department_name": k,
                "completed": v["completed"],
                "pending": v["pending"],
                "overdue": v["overdue"]
            }
            for k, v in dept_wise_map.items()
        ]

        return {
            "completed_count": completed_count,
            "pending_count": pending_count,
            "overdue_count": overdue_count,
            "department_wise": dept_wise,
            "overdue_faculty_list": overdue_faculty_list
        }

    async def run_compliance_cron(self) -> int:
        """Evaluate deadline compliance for all active faculty members and notify/flag overdue status."""
        facs_stmt = select(User).where(User.role == UserRole.FACULTY, User.is_deleted.is_(False))
        facs_res = await self.db.execute(facs_stmt)
        faculties = facs_res.scalars().all()
        
        cycle_start, cycle_due = get_current_cycle_dates(date.today())
        flagged_count = 0

        # Scan each faculty member
        for fac in faculties:
            # Check if has completed publication in current cycle
            comp_stmt = select(ResearchPlan).where(
                ResearchPlan.faculty_id == fac.id,
                ResearchPlan.status == ResearchPlanStatus.COMPLETED,
                ResearchPlan.cycle_start_date == cycle_start,
                ResearchPlan.is_deleted.is_(False)
            )
            comp_res = await self.db.execute(comp_stmt)
            has_completed = comp_res.scalars().first() is not None
            
            # If overdue
            if not has_completed and date.today() > cycle_due:
                prof_stmt = select(FacultyProfile).where(FacultyProfile.user_id == fac.id)
                prof_res = await self.db.execute(prof_stmt)
                profile = prof_res.scalar_one_or_none()
                
                if profile and profile.employment_status != "Pending Publication Submission":
                    profile.employment_status = "Pending Publication Submission"
                    self.db.add(profile)
                    flagged_count += 1
                    
                    # Notify Faculty
                    notif_fac = Notification(
                        user_id=fac.id,
                        type="warning",
                        message=f"Alert: You have missed the publication cycle deadline of {cycle_due}. Compliance status set to Pending Publication Submission.",
                        is_read=False,
                        sent_via="In-App"
                    )
                    self.db.add(notif_fac)

                    # Notify Principal
                    pr_stmt = select(User).where(User.role == UserRole.PRINCIPAL)
                    pr_res = await self.db.execute(pr_stmt)
                    pr = pr_res.scalars().first()
                    if pr:
                        notif_pr = Notification(
                            user_id=pr.id,
                            type="warning",
                            message=f"Faculty {fac.full_name} is overdue in research compliance. Cycle deadline {cycle_due} missed.",
                            is_read=False,
                            sent_via="In-App"
                        )
                        self.db.add(notif_pr)

        await self.db.commit()
        return flagged_count

    async def delete_plan(self, plan_id: str, faculty_id: str) -> None:
        plan = await self.get_plan_details(plan_id)
        if plan.faculty_id != faculty_id:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Not authorized to delete this plan"
            )
        if plan.status == ResearchPlanStatus.COMPLETED:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Cannot delete an approved/completed publication plan"
            )
        
        plan.is_deleted = True
        from datetime import datetime, UTC
        plan.deleted_at = datetime.now(UTC)
        await self.db.commit()


