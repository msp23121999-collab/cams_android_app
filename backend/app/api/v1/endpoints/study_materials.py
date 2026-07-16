import os
import json
import uuid
import shutil
from datetime import datetime
from typing import List, Dict, Any, Optional
from fastapi import APIRouter, Depends, HTTPException, Request, status, UploadFile, File
from pydantic import BaseModel
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.dependencies import get_current_user, role_required, get_db_session
from app.db.models.user import User, UserRole
from app.db.models.study_material import StudyMaterial
from app.db.models.academic import Course, Section
from app.db.models.audit import AuditLog

router = APIRouter()

DB_FILE = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(__file__)))), "study_materials_db.json")

def load_db() -> Dict[str, Any]:
    if not os.path.exists(DB_FILE):
        initial_db = {
            "bookmarks": {},
            "favorites": {},
            "downloads": [],
            "student_notifications": []
        }
        with open(DB_FILE, "w") as f:
            json.dump(initial_db, f, indent=4)
        return initial_db
        
    with open(DB_FILE, "r") as f:
        data = json.load(f)
    if "bookmarks" not in data:
        data["bookmarks"] = {}
    if "favorites" not in data:
        data["favorites"] = {}
    if "downloads" not in data:
        data["downloads"] = []
    if "student_notifications" not in data:
        data["student_notifications"] = []
    return data

def save_db(db: Dict[str, Any]) -> None:
    with open(DB_FILE, "w") as f:
        json.dump(db, f, indent=4)

# Pydantic Schemas
class UploadMaterialRequest(BaseModel):
    title: str
    description: str
    subject: str
    unit: str
    topic: str
    category: str
    keywords: List[str]
    file_url: str
    file_format: str
    status: str  # "Draft" or "Pending Approval"

class EditMaterialRequest(BaseModel):
    title: str
    description: str
    subject: str
    unit: str
    topic: str
    category: str
    keywords: List[str]
    file_url: str
    file_format: str
    status: str
    change_summary: str

class PrincipalReviewRequest(BaseModel):
    status: str  # "Approved" or "Rejected"
    remarks: str

# API Router endpoints
@router.get("/my-materials")
async def get_my_materials(
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL, UserRole.SUPER_ADMIN])),
    db: AsyncSession = Depends(get_db_session)
):
    q = (
        select(StudyMaterial, Course.name, Course.semester, Section.section_name)
        .outerjoin(Section, StudyMaterial.section_id == Section.id)
        .outerjoin(Course, Section.course_id == Course.id)
        .where(
            StudyMaterial.is_deleted.is_(False)
        )
    )
    if current_user.role in [UserRole.FACULTY, UserRole.HOD]:
        q = q.where(StudyMaterial.faculty_id == current_user.id)
        
    res = await db.execute(q)
    rows = res.all()
    
    result = []
    for m, course_name, semester, section_name in rows:
        stat_lbl = "Approved" if m.status == "APPROVED" else ("Rejected" if m.status == "REJECTED" else ("Draft" if m.status == "DRAFT" else "Pending Approval"))
        result.append({
            "id": m.id,
            "title": m.title,
            "description": m.comments or "",
            "subject": course_name or "Constitutional Law",
            "unit": "Unit I",
            "topic": m.title,
            "category": m.type,
            "keywords": [],
            "file_url": m.file_url,
            "file_format": m.type,
            "status": stat_lbl,
            "version": 1,
            "uploaded_date": m.created_at.isoformat(),
            "last_updated_date": m.updated_at.isoformat(),
            "faculty_id": m.faculty_id,
            "faculty_name": current_user.full_name,
            "semester": str(semester) if semester else "I",
            "versions": [
                {
                    "version": 1,
                    "title": m.title,
                    "description": m.comments or "",
                    "file_url": m.file_url,
                    "file_format": m.type,
                    "uploaded_date": m.created_at.isoformat(),
                    "updated_by": current_user.full_name,
                    "change_summary": "Initial Upload"
                }
            ]
        })
    return sorted(result, key=lambda x: x["last_updated_date"], reverse=True)

@router.post("/upload")
async def upload_material(
    payload: UploadMaterialRequest,
    request: Request,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
):
    # Find matching section_id based on Course name
    sec_q = select(Section).join(Course, Section.course_id == Course.id).where(Course.name == payload.subject)
    res = await db.execute(sec_q)
    section = res.scalars().first()
    if section:
        section_id = section.id
    else:
        # Fallback to any section if none found
        res_f = await db.execute(select(Section))
        fallback_sec = res_f.scalars().first()
        if not fallback_sec:
            raise HTTPException(status_code=400, detail="No sections available in system database.")
        section_id = fallback_sec.id

    status_val = "APPROVED" if payload.status == "Approved" else ("REJECTED" if payload.status == "Rejected" else ("DRAFT" if payload.status == "Draft" else "PENDING"))
    
    new_material = StudyMaterial(
        section_id=section_id,
        faculty_id=current_user.id,
        title=payload.title,
        type=payload.category,
        file_url=payload.file_url,
        is_verified=(status_val == "APPROVED"),
        status=status_val,
        comments=payload.description
    )
    db.add(new_material)
    await db.flush()
    
    # Log to AuditLog
    audit_entry = AuditLog(
        user_id=current_user.id,
        action="SUBMITTED",
        entity="StudyMaterial",
        entity_id=new_material.id,
        timestamp=datetime.now()
    )
    db.add(audit_entry)
    
    # Notify Principal & HOD
    if status_val == "PENDING":
        from app.services.notification_service import NotificationService
        notif_service = NotificationService(db)
        
        principal_q = await db.execute(select(User).where(User.role == UserRole.PRINCIPAL))
        for principal in principal_q.scalars().all():
            await notif_service.send_notification(
                user_id=principal.id,
                type_val="material_upload",
                message=f"Faculty {current_user.full_name} has uploaded a new study material '{new_material.title}' for approval."
            )
            
        if current_user.department_id:
            from app.db.models.academic import Department
            dept_q = await db.execute(select(Department).where(Department.id == current_user.department_id))
            dept = dept_q.scalars().first()
            if dept and dept.hod_id:
                await notif_service.send_notification(
                    user_id=dept.hod_id,
                    type_val="material_upload",
                    message=f"Faculty {current_user.full_name} has uploaded a new study material '{new_material.title}' for approval."
                )
                
    await db.commit()
    
    # Return formatted for frontend
    return {
        "id": new_material.id,
        "title": new_material.title,
        "description": new_material.comments or "",
        "subject": payload.subject,
        "unit": payload.unit,
        "topic": payload.topic,
        "category": payload.category,
        "keywords": payload.keywords,
        "file_url": new_material.file_url,
        "file_format": payload.file_format,
        "status": payload.status,
        "version": 1,
        "uploaded_date": datetime.now().isoformat(),
        "last_updated_date": datetime.now().isoformat(),
        "faculty_id": current_user.id,
        "faculty_name": current_user.full_name
    }

@router.post("/edit/{material_id}")
async def edit_material(
    material_id: str,
    payload: EditMaterialRequest,
    request: Request,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
):
    q = select(StudyMaterial).where(StudyMaterial.id == material_id)
    res = await db.execute(q)
    material = res.scalar_one_or_none()
    if not material:
        raise HTTPException(status_code=404, detail="Study material not found")
        
    sec_q = select(Section).join(Course, Section.course_id == Course.id).where(Course.name == payload.subject)
    res_s = await db.execute(sec_q)
    section = res_s.scalars().first()
    if section:
        material.section_id = section.id
        
    material.title = payload.title
    material.type = payload.category
    material.file_url = payload.file_url
    material.comments = payload.description
    
    status_val = "APPROVED" if payload.status == "Approved" else ("REJECTED" if payload.status == "Rejected" else ("DRAFT" if payload.status == "Draft" else "PENDING"))
    material.status = status_val
    material.is_verified = (status_val == "APPROVED")
    
    db.add(material)
    
    audit_entry = AuditLog(
        user_id=current_user.id,
        action="UPDATED",
        entity="StudyMaterial",
        entity_id=material.id,
        timestamp=datetime.now()
    )
    db.add(audit_entry)
    
    await db.commit()
    
    return {
        "id": material.id,
        "title": material.title,
        "description": material.comments or "",
        "subject": payload.subject,
        "unit": payload.unit,
        "topic": payload.topic,
        "category": payload.category,
        "keywords": payload.keywords,
        "file_url": material.file_url,
        "file_format": payload.file_format,
        "status": payload.status,
        "version": 2,
        "uploaded_date": material.created_at.isoformat(),
        "last_updated_date": datetime.now().isoformat(),
        "faculty_id": current_user.id,
        "faculty_name": current_user.full_name
    }

@router.post("/archive/{material_id}")
async def archive_material(
    material_id: str,
    request: Request,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
):
    q = select(StudyMaterial).where(StudyMaterial.id == material_id)
    res = await db.execute(q)
    material = res.scalar_one_or_none()
    if not material:
        raise HTTPException(status_code=404, detail="Study material not found")
        
    material.is_deleted = True
    db.add(material)
    
    audit_entry = AuditLog(
        user_id=current_user.id,
        action="ARCHIVED",
        entity="StudyMaterial",
        entity_id=material.id,
        timestamp=datetime.now()
    )
    db.add(audit_entry)
    
    await db.commit()
    return {
        "id": material.id,
        "status": "Archived"
    }

@router.post("/restore-version/{material_id}/{ver_num}")
async def restore_version(
    material_id: str,
    ver_num: int,
    request: Request,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
):
    # Versioning is simulated for mock frontend display compat
    return {"status": "success"}

# Principal Routes
@router.get("/principal/pending")
async def get_principal_pending(
    current_user: User = Depends(role_required([UserRole.PRINCIPAL, UserRole.SUPER_ADMIN])),
    db: AsyncSession = Depends(get_db_session)
):
    q = (
        select(StudyMaterial, User.full_name, Course.name)
        .outerjoin(User, StudyMaterial.faculty_id == User.id)
        .outerjoin(Section, StudyMaterial.section_id == Section.id)
        .outerjoin(Course, Section.course_id == Course.id)
        .where(
            StudyMaterial.status == "PENDING",
            StudyMaterial.is_deleted.is_(False)
        )
    )
    res = await db.execute(q)
    rows = res.all()
    result = []
    for m, faculty_name, course_name in rows:
        result.append({
            "id": m.id,
            "title": m.title,
            "description": m.comments or "",
            "subject": course_name or "Constitutional Law",
            "unit": "Unit I",
            "topic": m.title,
            "category": m.type,
            "keywords": [],
            "file_url": m.file_url,
            "file_format": m.type,
            "status": "Pending Approval",
            "version": 1,
            "uploaded_date": m.created_at.isoformat(),
            "last_updated_date": m.updated_at.isoformat(),
            "faculty_id": m.faculty_id,
            "faculty_name": faculty_name or "Faculty Member"
        })
    return result

@router.post("/principal/review/{material_id}")
async def principal_review_material(
    material_id: str,
    payload: PrincipalReviewRequest,
    request: Request,
    current_user: User = Depends(role_required([UserRole.PRINCIPAL, UserRole.SUPER_ADMIN])),
    db: AsyncSession = Depends(get_db_session)
):
    q = select(StudyMaterial).where(StudyMaterial.id == material_id)
    res = await db.execute(q)
    material = res.scalar_one_or_none()
    if not material:
        raise HTTPException(status_code=404, detail="Study material not found")
        
    status_val = "APPROVED" if payload.status == "Approved" else "REJECTED"
    material.status = status_val
    material.is_verified = (status_val == "APPROVED")
    material.comments = payload.remarks
    db.add(material)
    
    audit_entry = AuditLog(
        user_id=current_user.id,
        action=f"VERIFY_{status_val}",
        entity="StudyMaterial",
        entity_id=material.id,
        timestamp=datetime.now()
    )
    db.add(audit_entry)
    
    # Notify faculty and HOD
    from app.services.notification_service import NotificationService
    notif_service = NotificationService(db)
    
    status_str = "approved" if status_val == "APPROVED" else "rejected"
    notif_type = "material_approval" if status_val == "APPROVED" else "material_rejection"
    
    # 1. Notify faculty
    await notif_service.send_notification(
        user_id=material.faculty_id,
        type_val=notif_type,
        message=f"Your study material '{material.title}' has been {status_str} by Principal."
    )
    
    # 2. Notify HOD
    fac_q = await db.execute(select(User).where(User.id == material.faculty_id))
    faculty_member = fac_q.scalar_one_or_none()
    if faculty_member and faculty_member.department_id:
        from app.db.models.academic import Department
        dept_q = await db.execute(select(Department).where(Department.id == faculty_member.department_id))
        dept = dept_q.scalars().first()
        if dept and dept.hod_id:
            await notif_service.send_notification(
                user_id=dept.hod_id,
                type_val=notif_type,
                message=f"Study material '{material.title}' uploaded by {faculty_member.full_name} has been {status_str} by Principal."
            )
            
    # 3. Notify Students and Parents if approved
    if status_val == "APPROVED" and material.section_id:
        from app.db.models.student import Student, ParentStudentMap
        students_q = await db.execute(select(Student).where(Student.section_id == material.section_id, Student.is_deleted.is_(False)))
        for student in students_q.scalars().all():
            await notif_service.send_notification(
                user_id=student.user_id,
                type_val="new_study_material",
                message=f"A new study material '{material.title}' has been published for your section."
            )
            pm_q = await db.execute(select(ParentStudentMap).where(ParentStudentMap.student_id == student.id, ParentStudentMap.is_deleted.is_(False)))
            for pm in pm_q.scalars().all():
                await notif_service.send_notification(
                    user_id=pm.parent_id,
                    type_val="new_study_material",
                    message=f"A new study material '{material.title}' has been published for your child's section."
                )

    await db.commit()
    return {
        "id": material.id,
        "status": payload.status
    }

# Student Routes
@router.get("/student/approved")
async def get_student_approved(db: AsyncSession = Depends(get_db_session)):
    q = (
        select(StudyMaterial, User.full_name, Course.name)
        .outerjoin(User, StudyMaterial.faculty_id == User.id)
        .outerjoin(Section, StudyMaterial.section_id == Section.id)
        .outerjoin(Course, Section.course_id == Course.id)
        .where(
            StudyMaterial.status == "APPROVED",
            StudyMaterial.is_deleted.is_(False)
        )
    )
    res = await db.execute(q)
    rows = res.all()
    
    # Deduplicate by material ID to prevent same material appearing multiple times
    # (can happen when a material is associated with multiple sections of the same course)
    seen_ids: set = set()
    result = []
    for m, faculty_name, course_name in rows:
        if m.id in seen_ids:
            continue
        seen_ids.add(m.id)
        result.append({
            "id": m.id,
            "title": m.title,
            "description": m.comments or "",
            "subject": course_name or "Constitutional Law",
            "unit": "Unit I",
            "topic": m.title,
            "category": m.type,
            "keywords": [],
            "file_url": m.file_url,
            "file_format": m.type,
            "faculty_name": faculty_name or "Faculty Member",
            "uploaded_date": m.created_at.isoformat()
        })
    return sorted(result, key=lambda x: x["uploaded_date"], reverse=True)

# Notification & Audit Logs Fetch
@router.get("/notifications")
async def get_notifications(
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL, UserRole.SUPER_ADMIN]))
):
    # Dummy list of notifications for compatibility
    return []

@router.post("/notifications/read/{notif_id}")
async def mark_notification_read(
    notif_id: str,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL, UserRole.SUPER_ADMIN]))
):
    return {"detail": "Notification marked as read"}

@router.get("/audit-logs")
async def get_audit_logs(
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL, UserRole.SUPER_ADMIN])),
    db: AsyncSession = Depends(get_db_session)
):
    q = select(AuditLog).where(AuditLog.entity == "StudyMaterial", AuditLog.is_deleted.is_(False)).order_by(AuditLog.timestamp.desc())
    if current_user.role not in [UserRole.PRINCIPAL, UserRole.SUPER_ADMIN]:
        q = q.where(AuditLog.user_id == current_user.id)
    res = await db.execute(q)
    logs = res.scalars().all()
    
    result = []
    for log in logs:
        u_q = select(User).where(User.id == log.user_id)
        u_res = await db.execute(u_q)
        u = u_res.scalar_one_or_none()
        user_name = u.full_name if u else "System"
        
        result.append({
            "id": log.id,
            "user_id": log.user_id,
            "user_name": user_name,
            "role": "FACULTY",
            "action": log.action,
            "timestamp": log.timestamp.isoformat(),
            "ip_address": "127.0.0.1",
            "remarks": f"Action {log.action} logged"
        })
    return result

@router.get("/reports")
async def get_reports_data(
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL, UserRole.SUPER_ADMIN])),
    db: AsyncSession = Depends(get_db_session)
):
    q = (
        select(StudyMaterial, Course.name)
        .outerjoin(Section, StudyMaterial.section_id == Section.id)
        .outerjoin(Course, Section.course_id == Course.id)
        .where(
            StudyMaterial.is_deleted.is_(False)
        )
    )
    if current_user.role in [UserRole.FACULTY, UserRole.HOD]:
        q = q.where(StudyMaterial.faculty_id == current_user.id)
        
    res = await db.execute(q)
    rows = res.all()
    
    subject_counts = {}
    category_counts = {}
    monthly_counts = {}
    status_counts = {"Draft": 0, "Pending Approval": 0, "Approved": 0, "Rejected": 0, "Archived": 0}
    
    for m, course_name in rows:
        sub = course_name or "Constitutional Law"
        cat = m.type or "Lecture Notes"
        stat = "Approved" if m.status == "APPROVED" else ("Rejected" if m.status == "REJECTED" else ("Draft" if m.status == "DRAFT" else "Pending Approval"))
        
        subject_counts[sub] = subject_counts.get(sub, 0) + 1
        category_counts[cat] = category_counts.get(cat, 0) + 1
        
        month_name = m.created_at.strftime("%B %Y")
        monthly_counts[month_name] = monthly_counts.get(month_name, 0) + 1
        
        status_counts[stat] = status_counts.get(stat, 0) + 1
            
    return {
        "summary": {
            "total": len(rows),
            "approved": status_counts["Approved"],
            "pending": status_counts["Pending Approval"],
            "rejected": status_counts["Rejected"],
            "draft": status_counts["Draft"],
            "archived": status_counts["Archived"]
        },
        "subject_wise": [{"subject": k, "count": v} for k, v in subject_counts.items()],
        "category_wise": [{"category": k, "count": v} for k, v in category_counts.items()],
        "monthly_wise": [{"month": k, "count": v} for k, v in monthly_counts.items()]
    }

@router.post("/upload-file")
async def upload_file(
    file: UploadFile = File(...),
    current_user: User = Depends(get_current_user)
):
    static_dir = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(__file__)))), "static")
    uploads_dir = os.path.join(static_dir, "uploads")
    os.makedirs(uploads_dir, exist_ok=True)
    
    safe_filename = (file.filename or f"upload_{uuid.uuid4().hex}").replace(" ", "_")
    file_path = os.path.join(uploads_dir, safe_filename)
    
    with open(file_path, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)
        
    return {"file_url": f"/mock-uploads/{safe_filename}", "filename": safe_filename}


# Student Study Materials Module Endpoints (persistent student interactions stored in json)
@router.get("/student/bookmarks")
async def get_student_bookmarks(current_user: User = Depends(get_current_user)):
    db = load_db()
    user_id = current_user.id
    return db["bookmarks"].get(user_id, [])

@router.post("/student/bookmarks/toggle/{material_id}")
async def toggle_student_bookmark(material_id: str, current_user: User = Depends(get_current_user)):
    db = load_db()
    user_id = current_user.id
    bookmarks = db["bookmarks"].get(user_id, [])
    if material_id in bookmarks:
        bookmarks.remove(material_id)
    else:
        bookmarks.append(material_id)
    db["bookmarks"][user_id] = bookmarks
    save_db(db)
    return bookmarks

@router.get("/student/favorites")
async def get_student_favorites(current_user: User = Depends(get_current_user)):
    db = load_db()
    user_id = current_user.id
    return db["favorites"].get(user_id, [])

@router.post("/student/favorites/toggle/{material_id}")
async def toggle_student_favorite(material_id: str, current_user: User = Depends(get_current_user)):
    db = load_db()
    user_id = current_user.id
    favorites = db["favorites"].get(user_id, [])
    if material_id in favorites:
        favorites.remove(material_id)
    else:
        favorites.append(material_id)
    db["favorites"][user_id] = favorites
    save_db(db)
    return favorites

@router.post("/student/download/{material_id}")
async def log_student_download(material_id: str, current_user: User = Depends(get_current_user)):
    db = load_db()
    user_id = current_user.id
    db["downloads"].append({
        "id": str(uuid.uuid4()),
        "student_id": user_id,
        "material_id": material_id,
        "downloaded_at": datetime.now().isoformat()
    })
    save_db(db)
    return {"detail": "Download logged successfully"}

@router.get("/student/downloads")
async def get_student_downloads(current_user: User = Depends(get_current_user)):
    db = load_db()
    user_id = current_user.id
    user_downloads = [d for d in db["downloads"] if d["student_id"] == user_id]
    return sorted(user_downloads, key=lambda x: x["downloaded_at"], reverse=True)

@router.get("/student/notifications")
async def get_student_notifications(current_user: User = Depends(get_current_user)):
    db = load_db()
    user_id = current_user.id
    user_notifs = [n for n in db.get("student_notifications", []) if n.get("student_id") == user_id or n.get("student_id") == "all"]
    if not user_notifs:
        preseeded_notifs = [
            {
                "id": "snotif_1",
                "student_id": user_id,
                "title": "New Case Law Published",
                "message": "Landmark Judgment Kesavananda Bharati v. State of Kerala summary is now available.",
                "date": datetime.now().isoformat(),
                "is_read": False,
                "category": "Case Law"
            },
            {
                "id": "snotif_2",
                "student_id": user_id,
                "title": "BNS Bare Act Uploaded",
                "message": "The Bharatiya Nyaya Sanhita (BNS) 2023 Bare Act resource is now available for reference.",
                "date": datetime.now().isoformat(),
                "is_read": False,
                "category": "Bare Act"
            }
        ]
        db["student_notifications"].extend(preseeded_notifs)
        save_db(db)
        user_notifs = preseeded_notifs
    return sorted(user_notifs, key=lambda x: x["date"], reverse=True)

@router.post("/student/notifications/read/{notif_id}")
async def mark_student_notification_read(notif_id: str, current_user: User = Depends(get_current_user)):
    db = load_db()
    for n in db.get("student_notifications", []):
        if n["id"] == notif_id:
            n["is_read"] = True
            break
    save_db(db)
    return {"detail": "Notification marked as read"}

@router.get("/student/analytics")
async def get_student_analytics(current_user: User = Depends(get_current_user)):
    db = load_db()
    user_id = current_user.id
    bookmarks = db["bookmarks"].get(user_id, [])
    favorites = db["favorites"].get(user_id, [])
    downloads = [d for d in db["downloads"] if d["student_id"] == user_id]
    
    return {
        "total_bookmarks": len(bookmarks),
        "total_favorites": len(favorites),
        "total_downloads": len(downloads),
        "subject_wise_downloads": []
    }
