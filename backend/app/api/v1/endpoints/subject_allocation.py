import logging
from typing import List
from datetime import date
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, delete, or_

from app.core.dependencies import get_db_session, role_required
from app.db.models.user import User, UserRole
from app.db.models.academic import Department, Course, Section, AcademicYear, Degree, SubjectAllocation, Timetable, TimetableApproval, ApprovalStatus
from app.db.models.faculty import FacultyProfile, FacultyWorkload
from app.schemas.subject_allocation import SubjectAllocationCreate, SubjectAllocationResponse, AcademicSetupResponse, FacultyWorkloadInfo, SubjectInfo, AllocationHistoryResponse

logger = logging.getLogger(__name__)
router = APIRouter()

async def _get_default_dept_id(db: AsyncSession) -> str | None:
    # 1. Try to find department with active academic cohorts
    stmt = (
        select(Department.id)
        .join(Degree, Degree.dept_id == Department.id)
        .join(AcademicYear, AcademicYear.degree_id == Degree.id)
        .where(AcademicYear.is_active.is_(True), AcademicYear.is_deleted.is_(False))
        .limit(1)
    )
    res = await db.execute(stmt)
    dept_id = res.scalar()
    if dept_id:
        return dept_id

    # 2. Try to find L2 department (default BA LLB)
    stmt = select(Department.id).where(Department.code == "L2").limit(1)
    res = await db.execute(stmt)
    dept_id = res.scalar()
    if dept_id:
        return dept_id

    # 3. Fallback to any department
    stmt = select(Department.id).limit(1)
    res = await db.execute(stmt)
    return res.scalar()

@router.get("/setup", response_model=AcademicSetupResponse)
async def get_academic_setup(
    department_id: str | None = None,
    current_user: User = Depends(role_required([UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
):
    dept_id = department_id or current_user.department_id or await _get_default_dept_id(db)
    if not dept_id:
        raise HTTPException(status_code=404, detail="No departments found")
        
    dept = await db.get(Department, dept_id)
    if not dept:
        raise HTTPException(status_code=404, detail="Department not found")

    # Fetch degrees belonging to this department
    degrees_res = await db.execute(
        select(Degree).where(Degree.dept_id == dept_id, Degree.is_deleted.is_(False))
    )
    degrees = degrees_res.scalars().all()
    degree_ids = [d.id for d in degrees]
    degree_map = {d.id: d for d in degrees}

    if not degree_ids:
        raise HTTPException(status_code=404, detail="No degrees found for the department")

    # Fetch active academic years for these degrees (ongoing available classes only)
    ay_res = await db.execute(
        select(AcademicYear)
        .where(
            AcademicYear.degree_id.in_(degree_ids),
            AcademicYear.is_active.is_(True),
            AcademicYear.is_deleted.is_(False)
        )
    )
    active_ays = ay_res.scalars().all()
    if not active_ays:
        raise HTTPException(status_code=404, detail="No active academic cohorts found for this department")

    active_ays = sorted(active_ays, key=lambda x: (x.name, x.batch))

    # Default context
    academic_year = active_ays[0]
    degree = degree_map.get(academic_year.degree_id)

    sections_res = await db.execute(
        select(Section)
        .join(Course, Section.course_id == Course.id)
        .where(Course.dept_id == dept.id)
    )
    sections_db = sections_res.scalars().all()
    section_names = sorted(list(set([s.section_name for s in sections_db])))

    batches_data = []
    for ay in active_ays:
        deg = degree_map.get(ay.degree_id)
        deg_name = deg.name if deg else "Degree"
        batches_data.append({
            "id": ay.id,
            "name": f"{deg_name} - Batch {ay.batch}",
            "semester": ay.current_semester,
            "degree_id": ay.degree_id,
            "batch": ay.batch
        })

    return AcademicSetupResponse(
        academic_year=academic_year.name,
        academic_year_id=academic_year.id,
        department=dept.name,
        department_id=dept.id,
        course=dept.course_name or "N/A",
        semester=academic_year.current_semester,
        degree=degree.name if degree else "N/A",
        sections=[{"id": f"sec_{name}", "name": name} for name in section_names] if section_names else [{"id": "sec_A", "name": "A"}],
        batches=batches_data
    )

@router.get("/subjects", response_model=List[SubjectInfo])
async def get_subjects(
    department_id: str | None = None,
    current_user: User = Depends(role_required([UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
):
    dept_id = department_id or current_user.department_id or await _get_default_dept_id(db)
    dept = await db.get(Department, dept_id) if dept_id else None

    courses_res = await db.execute(
        select(Course).where(
            Course.dept_id == dept_id,
            Course.is_deleted.is_(False)
        ).order_by(Course.semester.asc())
    )
    courses = courses_res.scalars().all()
    
    subjects = []
    for c in courses:
        subjects.append(SubjectInfo(
            id=c.id,
            course=dept.course_name or "N/A" if dept else "N/A",
            semester=c.semester,
            subject_code=c.code,
            subject_name=c.name,
            subject_type="Core",
            credits=c.credits,
            hours_per_week=c.credits + 1,
            degree_id=c.degree_id
        ))
    return subjects

@router.get("/faculty", response_model=List[FacultyWorkloadInfo])
async def get_faculty_info(
    department_id: str | None = None,
    current_user: User = Depends(role_required([UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
):
    dept_id = department_id or current_user.department_id or await _get_default_dept_id(db)
    
    ay_res = await db.execute(select(AcademicYear).where(AcademicYear.is_active.is_(True)))
    active_ays = ay_res.scalars().all()
    active_ay_ids = [ay.id for ay in active_ays]
    
    current_semester = 1
    if active_ays:
        current_semester = active_ays[0].current_semester

    faculty_res = await db.execute(
        select(User, FacultyProfile)
        .outerjoin(FacultyProfile, User.id == FacultyProfile.user_id)
        .where(
            User.role.in_([UserRole.FACULTY, UserRole.HOD]),
            User.is_active.is_(True),
            User.department_id == dept_id
        )
    )
    
    faculty_list = []
    for user, profile in faculty_res.all():
        designation = profile.designation if profile else "Faculty"
        specialization = profile.specialization if profile else None
        
        wl_res = await db.execute(
            select(FacultyWorkload).where(
                FacultyWorkload.faculty_id == user.id,
                FacultyWorkload.semester == current_semester
            )
        )
        wl = wl_res.scalars().first()
        base_workload = wl.teaching_hours if wl else 0
        
        # Determine assigned subjects from allocations table (limited to active AY & active semester)
        allocations_res = await db.execute(
            select(SubjectAllocation, Course, Section)
            .join(Course, SubjectAllocation.course_id == Course.id)
            .join(Section, SubjectAllocation.section_id == Section.id)
            .join(AcademicYear, SubjectAllocation.academic_year_id == AcademicYear.id)
            .where(
                SubjectAllocation.faculty_id == user.id,
                SubjectAllocation.is_deleted.is_(False),
                AcademicYear.is_active.is_(True),
                AcademicYear.is_deleted.is_(False),
                SubjectAllocation.semester == AcademicYear.current_semester
            )
        )
        
        assigned_subs = set()
        for alloc, course, section in allocations_res.all():
            assigned_subs.add(f"{course.name} (Sec {section.section_name})")
            
        # Determine assigned subjects from timetable table to merge/fallback (limited to active AY & active semester)
        tt_res = await db.execute(
            select(Course.name, Section.section_name)
            .select_from(Timetable)
            .join(Section, Timetable.section_id == Section.id)
            .join(Course, Timetable.subject_id == Course.id)
            .join(AcademicYear, Course.degree_id == AcademicYear.degree_id)
            .where(
                Timetable.faculty_id == user.id,
                Timetable.is_deleted.is_(False),
                AcademicYear.is_active.is_(True),
                AcademicYear.is_deleted.is_(False),
                Course.semester == AcademicYear.current_semester
            )
        )
        for course_name, section_name in tt_res.all():
            assigned_subs.add(f"{course_name} (Sec {section_name})")
            
        faculty_list.append(FacultyWorkloadInfo(
            id=user.id,
            name=user.full_name,
            designation=designation,
            specialization=specialization,
            current_workload_hours=base_workload,
            max_workload_hours=18,
            assigned_subjects=list(assigned_subs)
        ))
    return faculty_list

@router.post("/allocate")
async def allocate_subjects(
    allocations: List[SubjectAllocationCreate],
    department_id: str | None = None,
    current_user: User = Depends(role_required([UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
):
    ay_res = await db.execute(select(AcademicYear).where(AcademicYear.is_active.is_(True)))
    active_ays = ay_res.scalars().all()
    ay = active_ays[0] if active_ays else None
    if not ay:
        raise HTTPException(status_code=400, detail="No active academic year")
        
    active_ay_ids = [a.id for a in active_ays]
    
    dept_id = department_id or current_user.department_id
    if not dept_id:
        if allocations:
            first_course_id = allocations[0].course_id
            course_res = await db.execute(select(Course.dept_id).where(Course.id == first_course_id))
            dept_id = course_res.scalar()
        if not dept_id:
            dept_id = await _get_default_dept_id(db)

    await db.execute(
        delete(SubjectAllocation).where(
            SubjectAllocation.department_id == dept_id,
            SubjectAllocation.academic_year_id.in_(active_ay_ids)
        )
    )

    for alloc in allocations:
        section_name = alloc.section_id.replace("sec_", "")
        sec_res = await db.execute(
            select(Section).where(
                Section.course_id == alloc.course_id,
                Section.section_name == section_name
            )
        )
        sec = sec_res.scalars().first()
        if not sec:
            continue

        new_alloc = SubjectAllocation(
            academic_year_id=ay.id,
            course_id=alloc.course_id,
            section_id=sec.id,
            faculty_id=alloc.faculty_id,
            department_id=dept_id,
            semester=ay.current_semester,
            is_active=True,
            allocated_by=current_user.id
        )
        db.add(new_alloc)
        
        # Sync with Timetable slots
        timetable_stmt = select(Timetable).where(
            Timetable.subject_id == alloc.course_id,
            Timetable.section_id == sec.id,
            Timetable.is_deleted.is_(False)
        )
        timetable_res = await db.execute(timetable_stmt)
        slots = timetable_res.scalars().all()
        for slot in slots:
            slot.faculty_id = alloc.faculty_id
            
            # Find or create TimetableApproval record
            app_stmt = select(TimetableApproval).where(TimetableApproval.timetable_id == slot.id)
            app_res = await db.execute(app_stmt)
            app = app_res.scalar_one_or_none()
            if app:
                app.status = ApprovalStatus.PENDING
                app.comments = "FACULTY_SYNC_PENDING"
            else:
                new_app = TimetableApproval(
                    timetable_id=slot.id,
                    status=ApprovalStatus.PENDING,
                    comments="FACULTY_SYNC_PENDING"
                )
                db.add(new_app)
        
    await db.commit()
    return {"message": "Allocations saved successfully"}

@router.get("/allocations", response_model=List[SubjectAllocationResponse])
async def get_allocations(
    department_id: str | None = None,
    current_user: User = Depends(role_required([UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
):
    ay_res = await db.execute(select(AcademicYear).where(AcademicYear.is_active.is_(True)))
    active_ays = ay_res.scalars().all()
    if not active_ays:
        return []
    active_ay_ids = [ay.id for ay in active_ays]

    dept_id = department_id or current_user.department_id or await _get_default_dept_id(db)

    # 1. Fetch from subject_allocations (limited to active AY & active semester)
    res = await db.execute(
        select(SubjectAllocation, Section)
        .join(Section, SubjectAllocation.section_id == Section.id)
        .join(AcademicYear, SubjectAllocation.academic_year_id == AcademicYear.id)
        .where(
            SubjectAllocation.department_id == dept_id,
            SubjectAllocation.academic_year_id.in_(active_ay_ids),
            SubjectAllocation.is_deleted.is_(False),
            AcademicYear.is_active.is_(True),
            AcademicYear.is_deleted.is_(False),
            SubjectAllocation.semester == AcademicYear.current_semester
        )
    )
    allocations = {}
    for alloc, section in res.all():
        key = (alloc.course_id, f"sec_{section.section_name}")
        allocations[key] = {
            "id": alloc.id,
            "academic_year_id": alloc.academic_year_id,
            "department_id": alloc.department_id,
            "course_id": alloc.course_id,
            "faculty_id": alloc.faculty_id,
            "section_id": f"sec_{section.section_name}",
            "allocated_by_id": alloc.allocated_by,
            "allocated_date": alloc.created_at.date() if alloc.created_at else None
        }

    # 2. Fetch from timetable to merge/fallback (limited to active AY & active semester)
    timetable_res = await db.execute(
        select(Timetable.subject_id, Section.section_name, Timetable.faculty_id, Timetable.section_id)
        .join(Section, Timetable.section_id == Section.id)
        .join(Course, Timetable.subject_id == Course.id)
        .join(AcademicYear, Course.degree_id == AcademicYear.degree_id)
        .where(
            Course.dept_id == dept_id,
            Timetable.is_deleted.is_(False),
            AcademicYear.is_active.is_(True),
            AcademicYear.is_deleted.is_(False),
            Course.semester == AcademicYear.current_semester
        )
    )
    for subject_id, section_name, faculty_id, section_uuid in timetable_res.all():
        key = (subject_id, f"sec_{section_name}")
        if key not in allocations:
            allocations[key] = {
                "id": f"tt_{subject_id}_{section_uuid}",
                "academic_year_id": active_ay_ids[0],
                "department_id": dept_id,
                "course_id": subject_id,
                "faculty_id": faculty_id,
                "section_id": f"sec_{section_name}",
                "allocated_by_id": None,
                "allocated_date": None
            }
            
    return list(allocations.values())

@router.get("/history", response_model=List[AllocationHistoryResponse])
async def get_history(
    department_id: str | None = None,
    current_user: User = Depends(role_required([UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
):
    dept_id = department_id or current_user.department_id or await _get_default_dept_id(db)

    res = await db.execute(
        select(SubjectAllocation, AcademicYear, Course, User, Section)
        .join(AcademicYear, SubjectAllocation.academic_year_id == AcademicYear.id)
        .join(Course, SubjectAllocation.course_id == Course.id)
        .join(User, SubjectAllocation.faculty_id == User.id)
        .join(Section, SubjectAllocation.section_id == Section.id)
        .where(
            SubjectAllocation.department_id == dept_id,
            SubjectAllocation.is_deleted.is_(False),
            or_(
                AcademicYear.is_active.is_(False),
                SubjectAllocation.semester < AcademicYear.current_semester
            )
        )
        .order_by(SubjectAllocation.created_at.desc())
        .limit(50)
    )
    
    history = []
    for alloc, ay, course, faculty, section in res.all():
        history.append(AllocationHistoryResponse(
            id=alloc.id,
            academic_year=ay.name,
            semester=course.semester,
            course=course.name, # using course name as subject here
            subject_name=f"{course.name} (Sec {section.section_name})",
            faculty_name=faculty.full_name,
            allocated_date=alloc.created_at.isoformat() if alloc.created_at else "N/A",
            allocated_by="HOD"
        ))
    return history

@router.get("/my-subjects")
async def get_my_subjects(
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
):
    from app.db.models.academic import SubjectAllocation, Course, Section, AcademicYear, Degree
    
    stmt = (
        select(SubjectAllocation, Course, Section, AcademicYear, Degree)
        .join(Course, SubjectAllocation.course_id == Course.id)
        .join(Section, SubjectAllocation.section_id == Section.id)
        .join(AcademicYear, SubjectAllocation.academic_year_id == AcademicYear.id)
        .outerjoin(Degree, Course.degree_id == Degree.id)
        .where(
            SubjectAllocation.faculty_id == current_user.id,
            SubjectAllocation.is_deleted.is_(False),
            Course.is_deleted.is_(False),
            Section.is_deleted.is_(False),
            AcademicYear.is_deleted.is_(False)
        )
    )
    res = await db.execute(stmt)
    results = res.all()
    
    my_subjects = []
    for alloc, course, section, ay, degree in results:
        sem = course.semester or 1
        year = (sem + 1) // 2
        
        my_subjects.append({
            "subject_name": course.name,
            "subject_code": course.code,
            "year": year,
            "semester": sem,
            "section": section.section_name,
            "batch": ay.batch,
            "degree_code": degree.code if degree else None
        })
        
    return my_subjects
