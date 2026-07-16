"""Grievances endpoint — student grievance management."""
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from typing import Optional
from datetime import datetime

from app.core.dependencies import get_db_session, role_required
from app.db.models.user import User, UserRole
from app.db.models.grievance import Grievance

router = APIRouter()


@router.get("/")
async def get_grievances(
    current_user: User = Depends(role_required([
        UserRole.STUDENT, UserRole.ADMIN, UserRole.HOD, UserRole.PRINCIPAL
    ])),
    db: AsyncSession = Depends(get_db_session),
):
    """
    Students see only their own grievances.
    Admin/HOD/Principal see all grievances.
    """
    q = select(Grievance, User).join(User, Grievance.raised_by == User.id).where(
        Grievance.is_deleted.is_(False)
    )

    if current_user.role == UserRole.STUDENT:
        q = q.where(Grievance.raised_by == current_user.id)

    q = q.order_by(Grievance.created_at.desc())
    rows = await db.execute(q)

    result = []
    for grievance, raised_user in rows.all():
        result.append({
            "id": str(grievance.id),
            "category": grievance.category,
            "description": grievance.description,
            "status": grievance.status,
            "assigned_to": str(grievance.assigned_to) if grievance.assigned_to else None,
            "raised_by_name": raised_user.full_name,
            "created_at": grievance.created_at.isoformat() if grievance.created_at else None,
        })
    return result


@router.post("/")
async def raise_grievance(
    payload: dict,
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session),
):
    """Student raises a new grievance."""
    category = payload.get("category", "General")
    description = payload.get("description", "")

    if not description.strip():
        raise HTTPException(status_code=400, detail="Description cannot be empty")

    grievance = Grievance(
        raised_by=current_user.id,
        category=category,
        description=description,
        status="PENDING",
    )
    db.add(grievance)
    await db.flush()

    # Trigger notifications
    from app.services.notification_service import NotificationService
    from app.db.repositories.student_repository import StudentRepository
    notif_service = NotificationService(db)

    # 1. Notify Admin and Principal
    receivers_q = await db.execute(select(User).where(User.role.in_([UserRole.ADMIN, UserRole.SUPER_ADMIN, UserRole.PRINCIPAL])))
    for r in receivers_q.scalars().all():
        await notif_service.send_notification(
            user_id=r.id,
            type_val="grievance_raised",
            message=f"Student {current_user.full_name} has raised a new grievance under category '{category}'."
        )

    # 2. Notify HOD of student's department
    student_repo = StudentRepository(db)
    student = await student_repo.get_student_by_user_id(current_user.id)
    if student and student.department_id:
        from app.db.models.academic import Department
        dept_q = await db.execute(select(Department).where(Department.id == student.department_id))
        dept = dept_q.scalars().first()
        if dept and dept.hod_id:
            await notif_service.send_notification(
                user_id=dept.hod_id,
                type_val="grievance_raised",
                message=f"Student {current_user.full_name} has raised a new grievance under category '{category}'."
            )

    await db.commit()
    await db.refresh(grievance)
    return {"id": str(grievance.id), "status": "PENDING", "message": "Grievance submitted successfully"}


@router.patch("/{grievance_id}")
async def update_grievance_status(
    grievance_id: str,
    payload: dict,
    current_user: User = Depends(role_required([
        UserRole.ADMIN, UserRole.HOD, UserRole.PRINCIPAL
    ])),
    db: AsyncSession = Depends(get_db_session),
):
    """Admin/HOD/Principal updates grievance status."""
    res = await db.execute(
        select(Grievance).where(Grievance.id == grievance_id, Grievance.is_deleted.is_(False))
    )
    grievance = res.scalar_one_or_none()
    if not grievance:
        raise HTTPException(status_code=404, detail="Grievance not found")

    new_status = payload.get("status", grievance.status)
    grievance.status = new_status
    if "assigned_to" in payload:
        grievance.assigned_to = payload["assigned_to"]

    # Trigger notifications
    from app.services.notification_service import NotificationService
    from app.db.repositories.student_repository import StudentRepository
    notif_service = NotificationService(db)

    # 1. Notify student
    await notif_service.send_notification(
        user_id=grievance.raised_by,
        type_val="grievance_update",
        message=f"Your grievance ({grievance.category}) status has been updated to {new_status}."
    )

    # 2. Notify student's parent
    student_repo = StudentRepository(db)
    student = await student_repo.get_student_by_user_id(grievance.raised_by)
    if student:
        from app.db.models.student import ParentStudentMap
        pm_q = await db.execute(select(ParentStudentMap).where(ParentStudentMap.student_id == student.id, ParentStudentMap.is_deleted.is_(False)))
        for pm in pm_q.scalars().all():
            await notif_service.send_notification(
                user_id=pm.parent_id,
                type_val="grievance_update",
                message=f"Your child's grievance ({grievance.category}) status has been updated to {new_status}."
            )

    await db.commit()
    return {"id": str(grievance.id), "status": grievance.status}
