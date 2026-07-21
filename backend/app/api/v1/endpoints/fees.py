"""Fees endpoint — student fee records & admin fee management."""
from fastapi import APIRouter, Depends, HTTPException, Query, Body, Request
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, text, or_, func
from typing import List, Optional, Any
from datetime import date, datetime, timezone
import hashlib
import hmac
import logging
import uuid

logger = logging.getLogger(__name__)

from app.core.dependencies import get_db_session, role_required
from app.core.config import settings
from app.db.models.user import User, UserRole
from app.db.models.fee import FeeRecord, FeeStructure, Payment, FeeStatus
from app.db.models.student import Student
from app.db.models.academic import Department, Degree
from app.db.repositories.student_repository import StudentRepository
from app.services.fee_service import FeeService
from app.schemas.payment import (
    CreateOrderRequest,
    CreateOrderResponse,
    VerifyPaymentRequest,
    VerifyPaymentResponse,
)

router = APIRouter()


def _get_razorpay_client():
    import razorpay
    return razorpay.Client(auth=(settings.RAZORPAY_KEY_ID, settings.RAZORPAY_KEY_SECRET))


# ─────────────────────────────────────────────────────────
#  STUDENT ENDPOINTS
# ─────────────────────────────────────────────────────────

@router.get("/")
async def get_student_fees(
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session),
):
    """Return all fee records for the authenticated student."""
    student_res = await db.execute(
        select(Student).where(Student.user_id == current_user.id, Student.is_deleted.is_(False))
    )
    student = student_res.scalar_one_or_none()
    if not student:
        raise HTTPException(status_code=404, detail="Student profile not found")

    rows = await db.execute(
        select(FeeRecord, FeeStructure)
        .join(FeeStructure, FeeRecord.fee_structure_id == FeeStructure.id)
        .where(FeeRecord.student_id == student.id, FeeRecord.is_deleted.is_(False))
        .order_by(FeeStructure.due_date)
    )

    result = []
    for record, structure in rows.all():
        # Compute paid amount from payments table
        paid_q = await db.execute(
            select(func.coalesce(func.sum(Payment.amount), 0)).where(
                Payment.fee_record_id == record.id,
                Payment.is_deleted.is_(False)
            )
        )
        paid_amount = float(paid_q.scalar_one() or 0)
        total = float(structure.amount)
        status_val = record.status.value if hasattr(record.status, "value") else str(record.status)
        result.append({
            "record_id": str(record.id),
            "fee_type": structure.fee_type,
            "amount": total,
            "total_amount": total,
            "paid_amount": paid_amount,
            "remaining_amount": max(0, total - paid_amount),
            "due_date": structure.due_date.isoformat() if structure.due_date else None,
            "semester": structure.semester,
            "status": status_val.lower(),
        })

    return result


def _sync_record_status(record, rec_detail) -> None:
    """Align a fee record's stored status with what the summary service computes.

    The service derives status from the payments actually recorded against the
    record, which is the authoritative view. Keeping the stored column in step
    means the two never disagree — previously a partly-settled record reported
    "partially_paid" from the summary while the column still said "pending".
    Only ever promotes toward settlement; it never reopens a PAID record.
    """
    if not rec_detail:
        return
    computed = rec_detail.get("status")
    if computed == "paid":
        record.status = FeeStatus.PAID
    elif computed == "partially_paid" and record.status != FeeStatus.PAID:
        record.status = FeeStatus.PARTIALLY_PAID


@router.post("/{record_id}/pay")
async def pay_fee(
    record_id: str,
    payload: dict,
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session),
):
    """Manual/legacy payment path — kept for backward compat with the web app.
    Ownership-checked: a student may only pay their own fee record."""
    record_res = await db.execute(
        select(FeeRecord).where(FeeRecord.id == record_id, FeeRecord.is_deleted.is_(False))
    )
    record = record_res.scalar_one_or_none()
    if not record:
        raise HTTPException(status_code=404, detail="Fee record not found")

    fee_service = FeeService(db)
    await fee_service.assert_owns_record(record, current_user)

    if record.status == FeeStatus.PAID:
        raise HTTPException(status_code=400, detail="Fee already paid")

    amount = payload.get("amount", 0)
    mode = payload.get("mode", "UPI")
    txn_id = payload.get("txn_id", "") or f"TXN-{uuid.uuid4().hex[:12].upper()}"

    payment = Payment(
        fee_record_id=record.id,
        amount=float(amount),
        mode=mode,
        txn_id=txn_id,
        status="paid",
        paid_at=datetime.now(timezone.utc),
    )
    db.add(payment)
    await db.commit()

    # Only mark the record PAID once the payments actually cover the balance — a
    # partial payment must leave it payable, otherwise the remainder becomes
    # permanently uncollectable.
    summary = await fee_service.get_student_fee_summary(record.student_id)
    rec_detail = next(
        (item for item in summary.get("records", []) if str(item.get("record_id")) == str(record.id)),
        None,
    )
    _sync_record_status(record, rec_detail)
    await db.commit()

    return {
        "status": record.status.value if hasattr(record.status, "value") else str(record.status),
        "record_id": str(record.id),
    }


@router.post("/{record_id}/create-order", response_model=CreateOrderResponse)
async def create_razorpay_order(
    record_id: str,
    payload: CreateOrderRequest,
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session),
):
    """Create a Razorpay order for a fee record and record a 'created' Payment row."""
    record_res = await db.execute(
        select(FeeRecord).where(FeeRecord.id == record_id, FeeRecord.is_deleted.is_(False))
    )
    record = record_res.scalar_one_or_none()
    if not record:
        raise HTTPException(status_code=404, detail="Fee record not found")

    fee_service = FeeService(db)
    await fee_service.assert_owns_record(record, current_user)

    if record.status == FeeStatus.PAID:
        raise HTTPException(status_code=400, detail="Fee already paid")

    if payload.amount <= 0:
        raise HTTPException(status_code=400, detail="Amount must be greater than 0")

    if not settings.RAZORPAY_KEY_ID or not settings.RAZORPAY_KEY_SECRET:
        raise HTTPException(status_code=503, detail="Payment gateway is not configured")

    paise = int(round(payload.amount * 100))
    client = _get_razorpay_client()
    try:
        order = client.order.create({
            "amount": paise,
            "currency": "INR",
            "receipt": record_id,
        })
    except Exception as exc:
        raise HTTPException(status_code=502, detail=f"Failed to create payment order: {exc}")

    payment = Payment(
        fee_record_id=record.id,
        amount=payload.amount,
        mode="Razorpay",
        txn_id=order["id"],
        razorpay_order_id=order["id"],
        status="created",
        paid_at=datetime.now(timezone.utc),
    )
    db.add(payment)
    await db.commit()

    return CreateOrderResponse(
        order_id=order["id"],
        amount=payload.amount,
        currency="INR",
        key_id=settings.RAZORPAY_KEY_ID,
    )


@router.post("/{record_id}/verify-payment", response_model=VerifyPaymentResponse)
async def verify_razorpay_payment(
    record_id: str,
    payload: VerifyPaymentRequest,
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session),
):
    """Verify a Razorpay payment signature server-side and update the Payment/FeeRecord."""
    record_res = await db.execute(
        select(FeeRecord).where(FeeRecord.id == record_id, FeeRecord.is_deleted.is_(False))
    )
    record = record_res.scalar_one_or_none()
    if not record:
        raise HTTPException(status_code=404, detail="Fee record not found")

    fee_service = FeeService(db)
    await fee_service.assert_owns_record(record, current_user)

    payment_res = await db.execute(
        select(Payment).where(
            Payment.razorpay_order_id == payload.razorpay_order_id,
            Payment.fee_record_id == record.id,
            Payment.is_deleted.is_(False),
        )
    )
    payment = payment_res.scalar_one_or_none()
    if not payment:
        raise HTTPException(status_code=404, detail="No matching payment order found for this fee record")

    client = _get_razorpay_client()
    try:
        client.utility.verify_payment_signature({
            "razorpay_order_id": payload.razorpay_order_id,
            "razorpay_payment_id": payload.razorpay_payment_id,
            "razorpay_signature": payload.razorpay_signature,
        })
        verified = True
    except Exception:
        verified = False

    if not verified:
        payment.status = "failed"
        payment.razorpay_payment_id = payload.razorpay_payment_id
        payment.razorpay_signature = payload.razorpay_signature
        await db.commit()
        raise HTTPException(status_code=400, detail="Payment signature verification failed")

    payment.status = "paid"
    payment.razorpay_payment_id = payload.razorpay_payment_id
    payment.razorpay_signature = payload.razorpay_signature
    payment.paid_at = datetime.now(timezone.utc)
    await db.commit()

    # Recompute settlement from the recorded payments and align the stored status,
    # so a verified part-payment lands in PARTIALLY_PAID rather than staying PENDING.
    summary = await fee_service.get_student_fee_summary(record.student_id)
    rec_detail = next((item for item in summary.get("records", []) if str(item.get("record_id")) == str(record.id)), None)
    _sync_record_status(record, rec_detail)
    await db.commit()

    return VerifyPaymentResponse(
        status="paid",
        record_id=str(record.id),
        fee_status=record.status.value if hasattr(record.status, "value") else str(record.status),
    )


@router.post("/webhook/razorpay")
async def razorpay_webhook(
    request: Request,
    db: AsyncSession = Depends(get_db_session),
):
    """Server-side Razorpay webhook — defense in depth against the app never
    calling /verify-payment (killed mid-flow, network drop after the payment
    succeeded on Razorpay's side). No auth dependency: this is called by
    Razorpay's servers directly, authenticated via HMAC signature instead."""
    raw_body = await request.body()
    signature = request.headers.get("X-Razorpay-Signature", "")

    if not settings.RAZORPAY_WEBHOOK_SECRET:
        # Intentionally unconfigured in this environment — no-op with a 200 so
        # Razorpay doesn't retry-storm us, rather than erroring.
        logger.warning("razorpay_webhook: RAZORPAY_WEBHOOK_SECRET not configured; ignoring webhook call")
        return {"status": "ignored", "reason": "webhook not configured"}

    # Verify the signature. Prefer the SDK helper if available, else fall back
    # to manual HMAC verification — never skip verification when a secret IS configured.
    verified = False
    try:
        client = _get_razorpay_client()
        client.utility.verify_webhook_signature(
            raw_body.decode("utf-8"), signature, settings.RAZORPAY_WEBHOOK_SECRET
        )
        verified = True
    except AttributeError:
        expected_signature = hmac.new(
            settings.RAZORPAY_WEBHOOK_SECRET.encode("utf-8"),
            raw_body,
            hashlib.sha256,
        ).hexdigest()
        verified = hmac.compare_digest(expected_signature, signature)
    except Exception:
        verified = False

    if not verified:
        raise HTTPException(status_code=400, detail="Invalid webhook signature")

    try:
        payload = await request.json()
    except Exception:
        # Signature verified but body isn't valid JSON — ack anyway (2xx),
        # nothing meaningful to process.
        return {"status": "ok", "processed": False}

    event = payload.get("event")
    if event != "payment.captured":
        # Not an event we act on yet; ack quickly regardless.
        return {"status": "ok", "processed": False, "event": event}

    try:
        entity = payload["payload"]["payment"]["entity"]
        order_id = entity.get("order_id")
        payment_id = entity.get("id")
    except (KeyError, TypeError):
        logger.warning("razorpay_webhook: unexpected payload shape for payment.captured event")
        return {"status": "ok", "processed": False}

    if not order_id:
        return {"status": "ok", "processed": False}

    payment_res = await db.execute(
        select(Payment).where(
            Payment.razorpay_order_id == order_id,
            Payment.is_deleted.is_(False),
        )
    )
    payment = payment_res.scalar_one_or_none()
    if not payment:
        logger.warning("razorpay_webhook: no Payment row found for razorpay_order_id=%s", order_id)
        return {"status": "ok", "processed": False}

    if payment.status != "paid":
        payment.status = "paid"
        payment.razorpay_payment_id = payment_id
        payment.paid_at = datetime.now(timezone.utc)
        await db.commit()

        record_res = await db.execute(
            select(FeeRecord).where(FeeRecord.id == payment.fee_record_id, FeeRecord.is_deleted.is_(False))
        )
        record = record_res.scalar_one_or_none()
        if record and record.status != FeeStatus.PAID:
            fee_service = FeeService(db)
            summary = await fee_service.get_student_fee_summary(record.student_id)
            rec_detail = next(
                (item for item in summary.get("records", []) if str(item.get("record_id")) == str(record.id)),
                None,
            )
            if rec_detail and rec_detail.get("status") == "paid":
                record.status = FeeStatus.PAID
                await db.commit()

    return {"status": "ok", "processed": True}


# ─────────────────────────────────────────────────────────
#  ADMIN ENDPOINTS
# ─────────────────────────────────────────────────────────

@router.get("/admin/all")
async def get_all_fee_records(
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session),
):
    """Admin: return all fee records with student info."""
    rows = await db.execute(
        text("""
            SELECT fr.id, u.full_name, u.email, fs.fee_type, fs.amount, fs.due_date,
                   fr.status
            FROM fee_records fr
            JOIN students s ON fr.student_id = s.id
            JOIN users u ON s.user_id = u.id
            JOIN fee_structure fs ON fr.fee_structure_id = fs.id
            WHERE fr.is_deleted = false
            ORDER BY fs.due_date DESC
        """)
    )
    result = []
    for row in rows.all():
        result.append({
            "record_id": str(row[0]),
            "student_name": row[1],
            "student_email": row[2],
            "fee_type": row[3],
            "amount": float(row[4]),
            "due_date": row[5].isoformat() if row[5] else None,
            "status": str(row[6]).lower().replace("feestatus.", ""),
        })
    return result


@router.get("/admin/search-students")
async def admin_search_students(
    q: str = Query("", description="Search by name, roll no, or username"),
    degree_id: Optional[str] = Query(None),
    department_id: Optional[str] = Query(None),
    batch_year: Optional[str] = Query(None),
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session),
):
    """Admin: search students by name, roll no, or phone for fee collection."""
    query = q.strip()
    
    # Base query joining students with users and departments
    stmt = (
        select(Student, User, Department, Degree)
        .join(User, Student.user_id == User.id)
        .outerjoin(Department, Student.department_id == Department.id)
        .outerjoin(Degree, Student.degree_id == Degree.id)
        .where(
            Student.is_deleted.is_(False),
            User.is_deleted.is_(False),
            User.role == UserRole.STUDENT,
        )
    )

    if query:
        stmt = stmt.where(
            or_(
                User.full_name.ilike(f"%{query}%"),
                Student.roll_no.ilike(f"%{query}%"),
                User.phone.ilike(f"%{query}%"),
                User.email.ilike(f"%{query}%"),
            )
        )

    if degree_id:
        if len(degree_id) == 36 and degree_id.count("-") == 4:
            stmt = stmt.where(Student.degree_id == degree_id)
        else:
            stmt = stmt.where(Degree.code == degree_id)
    if department_id:
        stmt = stmt.where(Student.department_id == department_id)
    if batch_year:
        actual_year = batch_year
        if "-" in batch_year:
            actual_year = batch_year.split("-")[0]
        try:
            stmt = stmt.where(Student.batch_year == int(actual_year))
        except ValueError:
            pass
    
    stmt = stmt.limit(1000)
    rows = await db.execute(stmt)
    
    result = []
    for student, user, dept, reg in rows.all():
        batch_start = student.batch_year or 2021
        batch = f"{batch_start}-{batch_start + (reg.duration_years if reg else 3)}" if student.batch_year else ""
        result.append({
            "student_id": str(student.id),
            "user_id": str(user.id),
            "name": user.full_name or "",
            "roll_no": student.roll_no,
            "username": user.phone or user.email.split("@")[0],
            "email": user.email,
            "department_name": dept.name if dept else "",
            "department_id": str(dept.id) if dept else "",
            "semester": student.semester,
            "batch_year": student.batch_year,
            "batch": batch,
            "quota": student.quota or "Government",
            "community_category": student.community_category or "General",
            "degree_code": reg.code if reg else "",
            "degree_id": str(reg.id) if reg else "",
        })
    return result


@router.get("/admin/student/{student_id}")
async def admin_get_student_fees(
    student_id: str,
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session),
):
    """Admin: get all fee records for a specific student with paid/remaining breakdown."""
    from app.services.fee_service import FeeService
    fee_service = FeeService(db)
    await fee_service.ensure_student_fee_records(student_id)

    student_res = await db.execute(
        select(Student).where(Student.id == student_id, Student.is_deleted.is_(False))
    )
    student = student_res.scalar_one_or_none()
    if not student:
        raise HTTPException(status_code=404, detail="Student not found")

    summary = await fee_service.get_student_fee_summary(student_id)
    summary_records = summary.get("records", [])
    summary_map = {str(item["record_id"]): item for item in summary_records}

    rows = await db.execute(
        select(FeeRecord, FeeStructure)
        .join(FeeStructure, FeeRecord.fee_structure_id == FeeStructure.id)
        .where(FeeRecord.student_id == student.id, FeeRecord.is_deleted.is_(False))
        .order_by(FeeStructure.semester, FeeStructure.due_date)
    )

    result = []
    for record, structure in rows.all():
        paid_q = await db.execute(
            select(func.coalesce(func.sum(Payment.amount), 0)).where(
                Payment.fee_record_id == record.id,
                Payment.is_deleted.is_(False)
            )
        )
        paid_amount = float(paid_q.scalar_one() or 0)
        
        rec_id_str = str(record.id)
        if rec_id_str in summary_map:
            total = float(summary_map[rec_id_str].get("amount", 0.0))
        else:
            total = float(structure.amount)

        status_val = record.status.value if hasattr(record.status, "value") else str(record.status)

        result.append({
            "record_id": str(record.id),
            "fee_type": structure.fee_type,
            "semester": structure.semester,
            "amount": total,
            "total_amount": total,
            "paid_amount": paid_amount,
            "remaining_amount": max(0.0, total - paid_amount),
            "due_date": structure.due_date.isoformat() if structure.due_date else None,
            "status": status_val.lower(),
        })

    return result


@router.post("/admin/collect")
async def admin_collect_fee(
    payload: dict,
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session),
):
    """Admin: collect (partial or full) fee payment for a student fee record."""
    fee_record_id = payload.get("fee_record_id")
    amount = float(payload.get("amount", 0))
    mode = payload.get("mode", "Cash")
    txn_id = payload.get("txn_id", "") or f"ADM-{uuid.uuid4().hex[:12].upper()}"

    if not fee_record_id:
        raise HTTPException(status_code=400, detail="fee_record_id is required")
    if amount <= 0:
        raise HTTPException(status_code=400, detail="Amount must be greater than 0")

    record_res = await db.execute(
        select(FeeRecord).where(FeeRecord.id == fee_record_id, FeeRecord.is_deleted.is_(False))
    )
    record = record_res.scalar_one_or_none()
    if not record:
        raise HTTPException(status_code=404, detail="Fee record not found")

    # Compute how much is already paid
    paid_q = await db.execute(
        select(func.coalesce(func.sum(Payment.amount), 0)).where(
            Payment.fee_record_id == record.id,
            Payment.is_deleted.is_(False)
        )
    )
    already_paid = float(paid_q.scalar_one() or 0)

    # Resolve total net amount using FeeService summary (accounts for concessions & blueprints)
    from app.services.fee_service import FeeService
    fee_service = FeeService(db)
    summary = await fee_service.get_student_fee_summary(record.student_id)
    rec_detail = next((item for item in summary.get("records", []) if str(item.get("record_id")) == str(record.id)), None)

    if rec_detail:
        total = float(rec_detail.get("amount", 0.0))
    else:
        struct_res = await db.execute(
            select(FeeStructure).where(FeeStructure.id == record.fee_structure_id)
        )
        structure = struct_res.scalar_one_or_none()
        total = float(structure.amount) if structure else 0.0

    remaining = max(0.0, total - already_paid)
    if amount > remaining + 0.01:
        raise HTTPException(
            status_code=400,
            detail=f"Amount ₹{amount:,.0f} exceeds remaining balance ₹{remaining:,.0f}"
        )

    payment = Payment(
        fee_record_id=record.id,
        amount=amount,
        mode=mode,
        txn_id=txn_id,
        paid_at=datetime.now(timezone.utc),
    )
    db.add(payment)

    # Update status
    new_paid = already_paid + amount
    if new_paid >= total - 0.01:
        record.status = FeeStatus.PAID
    # else leave as PENDING (partial)

    await db.commit()
    return {
        "status": "success",
        "record_id": str(record.id),
        "amount_collected": amount,
        "total_paid": new_paid,
        "remaining": max(0, total - new_paid),
        "fee_status": record.status.value,
        "txn_id": txn_id,
    }


@router.post("/admin/fee-structure")
async def admin_create_fee_structure(
    payload: dict,
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session),
):
    """Admin: create a fee structure record for a department/semester."""
    dept_id = payload.get("dept_id")
    semester = int(payload.get("semester", 1))
    amount = float(payload.get("amount", 0))
    due_date_str = payload.get("due_date")
    fee_type = payload.get("fee_type", "Tuition Fee")

    if not dept_id:
        raise HTTPException(status_code=400, detail="dept_id is required")
    if amount <= 0:
        raise HTTPException(status_code=400, detail="Amount must be > 0")

    try:
        due_date = date.fromisoformat(due_date_str) if due_date_str else date.today()
    except ValueError:
        raise HTTPException(status_code=400, detail="Invalid due_date format. Use YYYY-MM-DD")

    structure = FeeStructure(
        dept_id=dept_id,
        semester=semester,
        amount=amount,
        due_date=due_date,
        fee_type=fee_type,
    )
    db.add(structure)
    await db.commit()
    await db.refresh(structure)

    return {
        "id": str(structure.id),
        "dept_id": str(structure.dept_id),
        "semester": structure.semester,
        "amount": float(structure.amount),
        "due_date": structure.due_date.isoformat(),
        "fee_type": structure.fee_type,
    }


@router.get("/admin/fee-structures")
async def admin_list_fee_structures(
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session),
):
    """Admin: list all fee structures."""
    rows = await db.execute(
        select(FeeStructure, Department)
        .outerjoin(Department, FeeStructure.dept_id == Department.id)
        .where(FeeStructure.is_deleted.is_(False))
        .order_by(FeeStructure.semester)
    )
    result = []
    for structure, dept in rows.all():
        result.append({
            "id": str(structure.id),
            "dept_id": str(structure.dept_id),
            "dept_name": dept.name if dept else "",
            "semester": structure.semester,
            "amount": float(structure.amount),
            "due_date": structure.due_date.isoformat() if structure.due_date else None,
            "fee_type": structure.fee_type,
        })
    return result


@router.delete("/admin/fee-structure/{structure_id}")
async def admin_delete_fee_structure(
    structure_id: str,
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session),
):
    """Admin: soft-delete a fee structure."""
    struct_res = await db.execute(
        select(FeeStructure).where(FeeStructure.id == structure_id, FeeStructure.is_deleted.is_(False))
    )
    structure = struct_res.scalar_one_or_none()
    if not structure:
        raise HTTPException(status_code=404, detail="Fee structure not found")
    structure.is_deleted = True
    await db.commit()
    return {"status": "deleted"}


# ─────────────────────────────────────────────────────────
#  BLUEPRINT ENDPOINTS
# ─────────────────────────────────────────────────────────

@router.get("/admin/blueprints")
async def get_fee_blueprints(
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.PRINCIPAL])),
):
    """Admin: get all fee blueprints."""
    from app.core.json_db_helper import load_db_from_postgres
    return load_db_from_postgres("fee_blueprints_list.json", lambda: [])


@router.post("/admin/blueprints")
async def save_fee_blueprints(
    payload: List[Any] = Body(...),
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.PRINCIPAL])),
):
    """Admin: save all fee blueprints."""
    from app.core.json_db_helper import save_db_to_postgres
    save_db_to_postgres("fee_blueprints_list.json", payload)
    return {"status": "success"}


# ─────────────────────────────────────────────────────────
#  SCHOLARSHIP TYPE ENDPOINTS
# ─────────────────────────────────────────────────────────

@router.get("/admin/scholarship-types")
async def get_scholarship_types(
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.PRINCIPAL])),
):
    """Admin: get all defined scholarship types."""
    from app.core.json_db_helper import load_db_from_postgres
    return load_db_from_postgres("scholarship_types_list.json", lambda: [])


@router.post("/admin/scholarship-types")
async def save_scholarship_types(
    payload: List[Any] = Body(...),
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.PRINCIPAL])),
):
    """Admin: save all scholarship type definitions."""
    from app.core.json_db_helper import save_db_to_postgres
    save_db_to_postgres("scholarship_types_list.json", payload)
    return {"status": "success"}


@router.get("/admin/scholarship-types/list")
async def get_scholarship_types_public(
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.PRINCIPAL, UserRole.STUDENT])),
):
    """All roles: get scholarship types list (read-only) for dropdowns."""
    from app.core.json_db_helper import load_db_from_postgres
    return load_db_from_postgres("scholarship_types_list.json", lambda: [])

