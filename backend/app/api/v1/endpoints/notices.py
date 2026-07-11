"""Notices endpoint — read/write notices by audience type."""
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from typing import Optional

from app.core.dependencies import get_db_session, role_required
from app.db.models.user import User, UserRole
from app.db.models.communication import Notice, NoticeAcknowledgement

router = APIRouter()


def _audience_for_role(role: UserRole) -> list[str]:
    """Return audience_type values visible to a given role."""
    mapping = {
        UserRole.STUDENT: ["STUDENT", "ALL"],
        UserRole.PARENT: ["PARENT", "ALL"],
        UserRole.FACULTY: ["FACULTY", "ALL"],
        UserRole.HOD: ["HOD", "FACULTY", "ALL"],
        UserRole.PRINCIPAL: ["PRINCIPAL", "HOD", "FACULTY", "STUDENT", "PARENT", "ALL"],
        UserRole.ADMIN: ["ADMIN", "HOD", "FACULTY", "STUDENT", "PARENT", "ALL"],
    }
    return mapping.get(role, ["ALL"])


@router.get("/")
async def get_notices(
    current_user: User = Depends(role_required([
        UserRole.STUDENT, UserRole.PARENT, UserRole.FACULTY,
        UserRole.HOD, UserRole.PRINCIPAL, UserRole.ADMIN,
    ])),
    db: AsyncSession = Depends(get_db_session),
):
    """Return notices visible to the current user's role."""
    audiences = _audience_for_role(current_user.role)
    q = (
        select(Notice, User)
        .join(User, Notice.created_by == User.id)
        .where(Notice.audience_type.in_(audiences), Notice.is_deleted.is_(False))
        .order_by(Notice.publish_date.desc())
    )
    rows = await db.execute(q)

    result = []
    for notice, creator in rows.all():
        result.append({
            "id": str(notice.id),
            "title": notice.title,
            "body": notice.body,
            "audience_type": notice.audience_type,
            "publish_date": notice.publish_date.isoformat() if notice.publish_date else None,
            "created_by_name": creator.full_name,
            "created_by_role": creator.role.value if hasattr(creator.role, "value") else str(creator.role),
            "category": notice.category,
            "priority": notice.priority,
            "attachment_url": notice.attachment_url,
        })
    return result


@router.get("/received")
async def get_received_notices(
    current_user: User = Depends(role_required([
        UserRole.STUDENT, UserRole.PARENT, UserRole.FACULTY,
        UserRole.HOD, UserRole.PRINCIPAL, UserRole.ADMIN,
    ])),
    db: AsyncSession = Depends(get_db_session),
):
    """Return notices delivered to the current user with read/ack/archive status."""
    q = (
        select(NoticeAcknowledgement, Notice, User)
        .join(Notice, NoticeAcknowledgement.notice_id == Notice.id)
        .join(User, Notice.created_by == User.id)
        .where(NoticeAcknowledgement.user_id == current_user.id, Notice.is_deleted.is_(False))
        .order_by(Notice.publish_date.desc())
    )
    rows = await db.execute(q)

    result = []
    for ack, notice, creator in rows.all():
        result.append({
            "id": str(notice.id),
            "title": notice.title,
            "body": notice.body,
            "audience_type": notice.audience_type,
            "publish_date": notice.publish_date.isoformat() if notice.publish_date else None,
            "expiry_date": notice.expiry_date.isoformat() if notice.expiry_date else None,
            "category": notice.category,
            "priority": notice.priority,
            "publisher_name": creator.full_name,
            "publisher_role": notice.publisher_role or (creator.role.value if hasattr(creator.role, "value") else str(creator.role)),
            "is_read": ack.is_read,
            "is_acknowledged": ack.is_acknowledged,
            "is_archived": ack.is_archived,
            "attachment_url": notice.attachment_url,
        })
    return result


@router.post("/")
async def create_notice(
    payload: dict,
    current_user: User = Depends(role_required([
        UserRole.ADMIN, UserRole.PRINCIPAL, UserRole.HOD,
    ])),
    db: AsyncSession = Depends(get_db_session),
):
    """Admin/Principal/HOD creates a notice."""
    from datetime import date

    title = payload.get("title", "").strip()
    body = payload.get("body", "").strip()
    audience_type = payload.get("audience_type", "ALL")

    if not title or not body:
        raise HTTPException(status_code=400, detail="Title and body are required")

    notice = Notice(
        created_by=current_user.id,
        title=title,
        body=body,
        audience_type=audience_type,
        publish_date=date.today(),
        publisher_role=current_user.role.value if hasattr(current_user.role, "value") else str(current_user.role),
        priority=payload.get("priority", "Medium"),
        category=payload.get("category", "General Information"),
        status="Active"
    )
    db.add(notice)
    await db.flush()
    
    # Run notice delivery auto-population
    await deliver_notice(db, notice)
    
    await db.commit()
    await db.refresh(notice)
    return {"id": str(notice.id), "title": notice.title, "audience_type": notice.audience_type}


@router.delete("/{notice_id}")
async def delete_notice(
    notice_id: str,
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.PRINCIPAL, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session),
):
    """Admin/Principal/HOD deletes (soft-deletes) a notice."""
    res = await db.execute(
        select(Notice).where(Notice.id == notice_id, Notice.is_deleted.is_(False))
    )
    notice = res.scalar_one_or_none()
    if not notice:
        raise HTTPException(status_code=404, detail="Notice not found")

    notice.is_deleted = True
    await db.commit()
    return {"status": "deleted", "id": notice_id}


async def deliver_notice(db: AsyncSession, notice: Notice):
    """Query users matching notice targets and populate notice_acknowledgements table."""
    from app.db.models.student import Student, ParentStudentMap
    from app.db.models.academic import Department, Degree, SubjectAllocation, AcademicYear
    from app.db.models.communication import NoticeAcknowledgement
    from datetime import datetime
    
    # 1. Gather all active users
    users_q = select(User).where(User.is_active.is_(True), User.is_deleted.is_(False))
    res = await db.execute(users_q)
    all_users = res.scalars().all()

    # 2. Parse selected audience types (comma-separated, e.g. "HOD,FACULTY")
    target_roles = []
    has_all = False
    if notice.audience_types:
        parts = [p.strip().upper() for p in notice.audience_types.split(",")]
        for p in parts:
            if p == "ALL" or p == "ALL USERS":
                has_all = True
            elif p == "HOD":
                target_roles.append(UserRole.HOD)
            elif p == "FACULTY":
                target_roles.append(UserRole.FACULTY)
            elif p == "STUDENTS" or p == "STUDENT":
                target_roles.append(UserRole.STUDENT)
            elif p == "PARENT" or p == "PARENTS":
                target_roles.append(UserRole.PARENT)
    else:
        # Fallback to single audience_type
        aud = notice.audience_type.upper() if notice.audience_type else "ALL"
        if aud == "ALL":
            has_all = True
        elif "HOD" in aud:
            target_roles.append(UserRole.HOD)
        elif "FACULTY" in aud:
            target_roles.append(UserRole.FACULTY)
        elif "STUDENT" in aud:
            target_roles.append(UserRole.STUDENT)
        elif "PARENT" in aud:
            target_roles.append(UserRole.PARENT)

    # Filter candidate users by role (if not "ALL")
    candidate_users = all_users
    if not has_all and target_roles:
        candidate_users = [u for u in all_users if u.role in target_roles]

    # 3. Load students mapping to optimize student checks
    student_ids = [u.id for u in candidate_users if u.role == UserRole.STUDENT]
    student_profiles = {}
    if student_ids:
        stud_q = select(Student).where(Student.user_id.in_(student_ids), Student.is_deleted.is_(False))
        stud_res = await db.execute(stud_q)
        for s in stud_res.scalars().all():
            student_profiles[s.user_id] = s

    # 4. Load allocated faculty if batch_id is set
    allocated_faculty_ids = set()
    if notice.batch_id:
        alloc_q = (
            select(SubjectAllocation.faculty_id)
            .join(AcademicYear, SubjectAllocation.academic_year_id == AcademicYear.id)
            .where(AcademicYear.batch == notice.batch_id, SubjectAllocation.is_active.is_(True))
        )
        alloc_res = await db.execute(alloc_q)
        allocated_faculty_ids = set(alloc_res.scalars().all())

    # 5. Apply Refactored Targeting Filters
    filtered_users = []
    for u in candidate_users:
        # --- HOD Target Logic ---
        if u.role == UserRole.HOD:
            # HOD is responsible for the entire department (all batches)
            if notice.department_id:
                if u.department_id == notice.department_id:
                    filtered_users.append(u)
            else:
                filtered_users.append(u)

        # --- Faculty Target Logic ---
        elif u.role == UserRole.FACULTY:
            # Must match department if department_id is set
            if notice.department_id and u.department_id != notice.department_id:
                continue
            # Must match batch if batch_id is set (faculty handling that batch)
            if notice.batch_id and u.id not in allocated_faculty_ids:
                continue
            filtered_users.append(u)

        # --- Student Target Logic ---
        elif u.role == UserRole.STUDENT:
            sp = student_profiles.get(u.id)
            if not sp:
                continue
            # Must match department if department_id is set
            if notice.department_id and sp.department_id != notice.department_id:
                continue
            # Must match batch if batch_id is set
            if notice.batch_id:
                starting_year = str(sp.batch_year)
                if starting_year not in notice.batch_id:
                    continue
            filtered_users.append(u)

        # --- Other Roles Target Logic ---
        else:
            filtered_users.append(u)

    # 6. Parent logic: parents receive notice if notice targets parent AND child matches criteria (dept/batch if set)
    final_user_ids = []
    other_users = [u for u in filtered_users if u.role != UserRole.PARENT]
    parent_users = [u for u in filtered_users if u.role == UserRole.PARENT]

    for u in other_users:
        final_user_ids.append(u.id)

    if parent_users:
        parent_ids = [u.id for u in parent_users]
        pm_q = select(ParentStudentMap).where(ParentStudentMap.parent_id.in_(parent_ids), ParentStudentMap.is_deleted.is_(False))
        pm_res = await db.execute(pm_q)
        parent_mappings = pm_res.scalars().all()
        
        child_student_ids = [pm.student_id for pm in parent_mappings]
        if child_student_ids:
            child_stud_q = select(Student).where(Student.id.in_(child_student_ids), Student.is_deleted.is_(False))
            child_stud_res = await db.execute(child_stud_q)
            child_student_profiles = {s.id: s for s in child_stud_res.scalars().all()}
            
            for pm in parent_mappings:
                child_sp = child_student_profiles.get(pm.student_id)
                if not child_sp:
                    continue
                # If department filter is set, child must match
                if notice.department_id and child_sp.department_id != notice.department_id:
                    continue
                # If batch filter is set, child must match
                if notice.batch_id:
                    starting_year = str(child_sp.batch_year)
                    if starting_year not in notice.batch_id:
                        continue
                final_user_ids.append(pm.parent_id)

    final_user_ids = list(set(final_user_ids))

    # 7. Create NoticeAcknowledgement records
    for uid in final_user_ids:
        exist_res = await db.execute(
            select(NoticeAcknowledgement).where(
                NoticeAcknowledgement.notice_id == notice.id,
                NoticeAcknowledgement.user_id == uid
            )
        )
        if exist_res.scalar_one_or_none():
            continue
            
        ack = NoticeAcknowledgement(
            notice_id=notice.id,
            user_id=uid,
            is_read=False,
            is_acknowledged=False,
            status="DELIVERED"
        )
        db.add(ack)
    await db.flush()


@router.post("/{notice_id}/read")
async def read_notice(
    notice_id: str,
    current_user: User = Depends(role_required([
        UserRole.STUDENT, UserRole.PARENT, UserRole.FACULTY,
        UserRole.HOD, UserRole.PRINCIPAL, UserRole.ADMIN,
    ])),
    db: AsyncSession = Depends(get_db_session),
):
    """Mark a notice as read by the current user."""
    from app.db.models.communication import NoticeAcknowledgement
    from datetime import datetime
    
    res = await db.execute(
        select(NoticeAcknowledgement).where(
            NoticeAcknowledgement.notice_id == notice_id,
            NoticeAcknowledgement.user_id == current_user.id
        )
    )
    ack = res.scalar_one_or_none()
    if not ack:
        # Fallback: create notice acknowledgement if missing
        ack = NoticeAcknowledgement(
            notice_id=notice_id,
            user_id=current_user.id,
            is_read=True,
            read_at=datetime.utcnow(),
            is_acknowledged=False,
            status="DELIVERED"
        )
        db.add(ack)
    else:
        ack.is_read = True
        ack.read_at = datetime.utcnow()
        
    await db.commit()
    return {"status": "success", "notice_id": notice_id, "is_read": True}


@router.post("/{notice_id}/acknowledge")
async def acknowledge_notice(
    notice_id: str,
    current_user: User = Depends(role_required([
        UserRole.STUDENT, UserRole.PARENT, UserRole.FACULTY,
        UserRole.HOD, UserRole.PRINCIPAL, UserRole.ADMIN,
    ])),
    db: AsyncSession = Depends(get_db_session),
):
    """Mark a notice as acknowledged by the current user."""
    from app.db.models.communication import NoticeAcknowledgement
    from datetime import datetime
    
    res = await db.execute(
        select(NoticeAcknowledgement).where(
            NoticeAcknowledgement.notice_id == notice_id,
            NoticeAcknowledgement.user_id == current_user.id
        )
    )
    ack = res.scalar_one_or_none()
    if not ack:
        ack = NoticeAcknowledgement(
            notice_id=notice_id,
            user_id=current_user.id,
            is_read=True,
            read_at=datetime.utcnow(),
            is_acknowledged=True,
            acknowledged_at=datetime.utcnow(),
            status="DELIVERED"
        )
        db.add(ack)
    else:
        ack.is_read = True
        if not ack.read_at:
            ack.read_at = datetime.utcnow()
        ack.is_acknowledged = True
        ack.acknowledged_at = datetime.utcnow()
        
    await db.commit()
    return {"status": "success", "notice_id": notice_id, "is_acknowledged": True}


@router.post("/{notice_id}/archive")
async def archive_notice(
    notice_id: str,
    current_user: User = Depends(role_required([
        UserRole.STUDENT, UserRole.PARENT, UserRole.FACULTY,
        UserRole.HOD, UserRole.PRINCIPAL, UserRole.ADMIN,
    ])),
    db: AsyncSession = Depends(get_db_session),
):
    """Mark a notice as archived by the current user."""
    from app.db.models.communication import NoticeAcknowledgement
    from datetime import datetime
    
    res = await db.execute(
        select(NoticeAcknowledgement).where(
            NoticeAcknowledgement.notice_id == notice_id,
            NoticeAcknowledgement.user_id == current_user.id
        )
    )
    ack = res.scalar_one_or_none()
    if not ack:
        ack = NoticeAcknowledgement(
            notice_id=notice_id,
            user_id=current_user.id,
            is_read=True,
            read_at=datetime.utcnow(),
            is_archived=True,
            archived_at=datetime.utcnow(),
            status="DELIVERED"
        )
        db.add(ack)
    else:
        ack.is_archived = True
        ack.archived_at = datetime.utcnow()
        
    await db.commit()
    return {"status": "success", "notice_id": notice_id, "is_archived": True}
