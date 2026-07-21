import secrets
from datetime import datetime, timedelta, timezone

from fastapi import APIRouter, BackgroundTasks, Cookie, Depends, HTTPException, Request, Response, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select

from app.core.dependencies import get_current_user, get_db_session, role_required
from app.core.security import decode_token, create_access_token, hash_password, verify_password
from app.db.models.user import User, UserRole
from app.db.models.password_reset import PasswordResetToken
from app.schemas.auth import (
    LoginRequest,
    LoginResponse,
    RefreshRequest,
    RefreshResponse,
    UserMeResponse,
    ChangePasswordRequest,
    MessageResponse,
    RequestEmailChangeRequest,
    ConfirmEmailChangeRequest,
    ForgotPasswordRequest,
    ResetPasswordRequest,
    NotificationPreferencesRequest,
)
from app.services.auth_service import AuthService
from app.services.email_service import send_email
from app.core.config import settings
from app.core.rate_limit import (
    limiter,
    account_login_allowed,
    record_failed_login,
    clear_failed_logins,
)

router = APIRouter()

MIN_PASSWORD_LENGTH = 8


def validate_password_strength(password: str) -> None:
    """Enforce the password policy shown to users in the app UI:
    at least 8 characters with uppercase, lowercase, number, and special character.
    Raises HTTPException(400) with a specific message on the first rule violated."""
    if len(password) < MIN_PASSWORD_LENGTH:
        raise HTTPException(status_code=400, detail=f"Password must be at least {MIN_PASSWORD_LENGTH} characters long")
    if not any(c.isupper() for c in password):
        raise HTTPException(status_code=400, detail="Password must include at least one uppercase letter")
    if not any(c.islower() for c in password):
        raise HTTPException(status_code=400, detail="Password must include at least one lowercase letter")
    if not any(c.isdigit() for c in password):
        raise HTTPException(status_code=400, detail="Password must include at least one number")
    if all(c.isalnum() for c in password):
        raise HTTPException(status_code=400, detail="Password must include at least one special character")


def role_subdomain(role: str) -> str:
    if role in {"STUDENT", "PARENT"}:
        return "students.campus.local"
    if role in {"FACULTY", "HOD"}:
        return "staff.campus.local"
    return "admin.campus.local"


@router.post("/login", response_model=LoginResponse)
@limiter.limit(settings.RATE_LIMIT_LOGIN)
async def login(request: Request, payload: LoginRequest, response: Response, db: AsyncSession = Depends(get_db_session)) -> LoginResponse:
    # Per-account brute-force protection. Checked before authenticating so a
    # locked account cannot be probed further, and only failures consume budget.
    if not account_login_allowed(payload.email):
        raise HTTPException(
            status_code=429,
            detail="Too many failed sign-in attempts for this account. Please try again shortly.",
        )

    service = AuthService(db)
    try:
        user, access_token, refresh_token = await service.authenticate(payload.email, payload.password)
    except HTTPException as exc:
        if exc.status_code in (400, 401, 403):
            record_failed_login(payload.email)
        raise

    clear_failed_logins(payload.email)

    response.set_cookie(
        "access_token",
        access_token,
        httponly=True,
        samesite="lax",
        secure=settings.COOKIE_SECURE,
        max_age=settings.ACCESS_TOKEN_EXPIRE_MINUTES * 60
    )
    response.set_cookie(
        "refresh_token",
        refresh_token,
        httponly=True,
        samesite="lax",
        secure=settings.COOKIE_SECURE,
        max_age=settings.REFRESH_TOKEN_EXPIRE_DAYS * 24 * 3600
    )

    return LoginResponse(
        access_token=access_token,
        role=user.role,
        subdomain_target=role_subdomain(user.role.value),
        refresh_token=refresh_token
    )


@router.post("/refresh", response_model=RefreshResponse)
@limiter.limit(settings.RATE_LIMIT_TOKEN_REFRESH)
async def refresh_token(
    request: Request,
    response: Response,
    payload: RefreshRequest | None = None,
    refresh_token: str | None = Cookie(default=None)
) -> RefreshResponse:
    token = (payload.refresh_token if payload else None) or refresh_token
    if not token:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Missing refresh token")

    try:
        decoded_payload = decode_token(token)
    except ValueError as exc:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid refresh token") from exc
    if decoded_payload.get("type") != "refresh":
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid refresh token")

    access_token = create_access_token(subject=decoded_payload["sub"], role=decoded_payload.get("role", "STUDENT"))
    response.set_cookie(
        "access_token",
        access_token,
        httponly=True,
        samesite="lax",
        secure=settings.COOKIE_SECURE,
        max_age=settings.ACCESS_TOKEN_EXPIRE_MINUTES * 60
    )
    return RefreshResponse(access_token=access_token, refresh_token=token)


@router.post("/logout")
async def logout(response: Response) -> dict[str, str]:
    response.delete_cookie("access_token")
    response.delete_cookie("refresh_token")
    return {"detail": "Logged out"}


@router.get("/me", response_model=UserMeResponse)
async def me(current_user: User = Depends(get_current_user)) -> UserMeResponse:
    return UserMeResponse(
        id=current_user.id,
        email=current_user.email,
        full_name=current_user.full_name,
        role=current_user.role,
        department_id=current_user.department_id if current_user.department_id else None,
        email_notifications_enabled=current_user.email_notifications_enabled,
    )


@router.patch("/notification-preferences", response_model=UserMeResponse)
async def update_notification_preferences(
    payload: NotificationPreferencesRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session),
) -> UserMeResponse:
    current_user.email_notifications_enabled = payload.email_notifications_enabled
    await db.commit()
    return UserMeResponse(
        id=current_user.id,
        email=current_user.email,
        full_name=current_user.full_name,
        role=current_user.role,
        department_id=current_user.department_id if current_user.department_id else None,
        email_notifications_enabled=current_user.email_notifications_enabled,
    )



@router.post("/change-password", response_model=MessageResponse)
@limiter.limit(settings.RATE_LIMIT_CHANGE_PASSWORD)
async def change_password(
    request: Request,
    payload: ChangePasswordRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session),
) -> MessageResponse:
    if not verify_password(payload.current_password, current_user.hashed_password):
        raise HTTPException(status_code=400, detail="Current password is incorrect")

    validate_password_strength(payload.new_password)

    current_user.hashed_password = hash_password(payload.new_password)
    await db.commit()
    return MessageResponse(detail="Password changed successfully")


@router.post("/request-email-change", response_model=MessageResponse)
@limiter.limit(settings.RATE_LIMIT_CHANGE_PASSWORD)
async def request_email_change(
    request: Request,
    payload: RequestEmailChangeRequest,
    background_tasks: BackgroundTasks,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session),
) -> MessageResponse:
    # Re-authenticate before allowing the account's address to move. A bearer token
    # alone must not be enough: otherwise a stolen session lets an attacker point the
    # account at their own address and then use the password-reset flow to take it
    # over outright. Mirrors the check on /change-password.
    if not verify_password(payload.current_password, current_user.hashed_password):
        raise HTTPException(status_code=400, detail="Current password is incorrect")

    new_email = payload.new_email.lower()

    existing_res = await db.execute(
        select(User).where(User.email == new_email, User.id != current_user.id, User.is_deleted.is_(False))
    )
    if existing_res.scalar_one_or_none():
        raise HTTPException(status_code=400, detail="That email address is already in use")

    token = secrets.token_urlsafe(32)
    current_user.pending_email = new_email
    current_user.email_change_token = token
    current_user.email_change_token_expires_at = datetime.now(timezone.utc) + timedelta(hours=1)
    await db.commit()

    verify_url = f"{settings.FRONTEND_BASE_URL}/verify-email-change?token={token}"
    background_tasks.add_task(
        send_email,
        new_email,
        "Confirm your new CAMS email address",
        f"Hello {current_user.full_name},\n\n"
        f"Please confirm your new email address by visiting the link below "
        f"(valid for 1 hour):\n\n{verify_url}\n\n"
        f"If you did not request this change, you can safely ignore this email.",
    )

    # Always return the same generic success response regardless of whether
    # the SMTP send actually succeeds — the pending change itself was created.
    return MessageResponse(detail="Verification email sent to the new address")


@router.post("/confirm-email-change", response_model=MessageResponse)
async def confirm_email_change(
    payload: ConfirmEmailChangeRequest,
    db: AsyncSession = Depends(get_db_session),
) -> MessageResponse:
    user_res = await db.execute(
        select(User).where(User.email_change_token == payload.token, User.is_deleted.is_(False))
    )
    user = user_res.scalar_one_or_none()
    if not user:
        raise HTTPException(status_code=400, detail="Invalid or expired verification token")

    expires_at = user.email_change_token_expires_at
    if expires_at is not None and expires_at.tzinfo is None:
        expires_at = expires_at.replace(tzinfo=timezone.utc)

    if not expires_at or expires_at <= datetime.now(timezone.utc):
        raise HTTPException(status_code=400, detail="Invalid or expired verification token")

    if not user.pending_email:
        raise HTTPException(status_code=400, detail="Invalid or expired verification token")

    user.email = user.pending_email
    user.pending_email = None
    user.email_change_token = None
    user.email_change_token_expires_at = None
    await db.commit()
    return MessageResponse(detail="Email address updated successfully")


@router.post("/forgot-password", response_model=MessageResponse)
@limiter.limit(settings.RATE_LIMIT_PASSWORD_RESET_REQUEST)
async def forgot_password(
    request: Request,
    payload: ForgotPasswordRequest,
    background_tasks: BackgroundTasks,
    db: AsyncSession = Depends(get_db_session),
) -> MessageResponse:
    generic_response = MessageResponse(
        detail="If an account with that email exists, a reset link has been sent."
    )

    user_res = await db.execute(
        select(User).where(User.email == payload.email.lower(), User.is_deleted.is_(False))
    )
    user = user_res.scalar_one_or_none()
    if not user:
        # Do not reveal whether the email exists — always return the same response.
        return generic_response

    token = secrets.token_urlsafe(32)
    reset_token = PasswordResetToken(
        user_id=user.id,
        token=token,
        expires_at=datetime.now(timezone.utc) + timedelta(hours=1),
    )
    db.add(reset_token)
    await db.commit()

    reset_url = f"{settings.FRONTEND_BASE_URL}/reset-password?token={token}"
    background_tasks.add_task(
        send_email,
        user.email,
        "Reset your CAMS password",
        f"Hello {user.full_name},\n\n"
        f"We received a request to reset your password. This request is valid for 1 hour.\n\n"
        f"On the web, open this link to choose a new password:\n{reset_url}\n\n"
        f"In the mobile app, open the Reset Password screen and paste this reset code:\n\n"
        f"{token}\n\n"
        f"If you did not request this, you can safely ignore this email.",
    )

    return generic_response


@router.post("/reset-password", response_model=MessageResponse)
@limiter.limit(settings.RATE_LIMIT_PASSWORD_RESET_CONFIRM)
async def reset_password(
    request: Request,
    payload: ResetPasswordRequest,
    db: AsyncSession = Depends(get_db_session),
) -> MessageResponse:
    validate_password_strength(payload.new_password)

    token_res = await db.execute(
        select(PasswordResetToken).where(
            PasswordResetToken.token == payload.token,
            PasswordResetToken.is_deleted.is_(False),
        )
    )
    reset_token = token_res.scalar_one_or_none()

    if not reset_token or reset_token.used_at is not None:
        raise HTTPException(status_code=400, detail="Invalid or expired reset token")

    expires_at = reset_token.expires_at
    if expires_at.tzinfo is None:
        expires_at = expires_at.replace(tzinfo=timezone.utc)
    if expires_at <= datetime.now(timezone.utc):
        raise HTTPException(status_code=400, detail="Invalid or expired reset token")

    user_res = await db.execute(select(User).where(User.id == reset_token.user_id))
    user = user_res.scalar_one_or_none()
    if not user:
        raise HTTPException(status_code=400, detail="Invalid or expired reset token")

    user.hashed_password = hash_password(payload.new_password)
    reset_token.used_at = datetime.now(timezone.utc)
    await db.commit()
    return MessageResponse(detail="Password has been reset successfully")


@router.get("/debug/users")
async def debug_list_users(
    db: AsyncSession = Depends(get_db_session),
    current_user: User = Depends(role_required([UserRole.SUPER_ADMIN])),
) -> list[dict]:
    """[DEV ONLY] List all users in the database."""
    if settings.ENVIRONMENT == "production":
        raise HTTPException(status_code=404)
    from app.db.models.student import Student
    result = await db.execute(select(User, Student).outerjoin(Student, User.id == Student.user_id))
    rows = result.all()
    return [
        {
            "email": u.email,
            "full_name": u.full_name,
            "role": u.role.value,
            "roll_no": s.roll_no if s else None,
            "verification_status": s.verification_status if s else None,
            "edit_request_status": s.edit_request_status if s else None,
        }
        for u, s in rows
    ]


@router.get("/debug/fix-student")
async def debug_fix_student(
    db: AsyncSession = Depends(get_db_session),
    current_user: User = Depends(role_required([UserRole.SUPER_ADMIN])),
) -> dict:
    """[DEV ONLY] Reset student@cams.local using raw SQL."""
    if settings.ENVIRONMENT == "production":
        raise HTTPException(status_code=404)

    import uuid
    from sqlalchemy import text

    target_email = "student@cams.local"
    target_password = "Password@123"
    new_hash = hash_password(target_password)

    try:
        # Check if user exists (raw SQL, no ORM filter issues)
        row = await db.execute(
            text("SELECT id, hashed_password, is_deleted FROM users WHERE email = :email"),
            {"email": target_email}
        )
        existing = row.fetchone()

        # Get BA LLB department & degree
        dept_res = await db.execute(text("SELECT id FROM departments WHERE code = 'L2' LIMIT 1"))
        dept_row = dept_res.fetchone()
        dept_id = dept_row[0] if dept_row else None

        deg_res = await db.execute(text("SELECT id FROM degrees WHERE code = 'BA LLB' LIMIT 1"))
        deg_row = deg_res.fetchone()
        deg_id = deg_row[0] if deg_row else None

        # Fetch Mentor ID (faculty@cams.local)
        fac_res = await db.execute(text("SELECT id FROM users WHERE email = 'faculty@cams.local' LIMIT 1"))
        fac_row = fac_res.fetchone()
        mentor_id = fac_row[0] if fac_row else None

        # Fetch Section ID
        sec_res = await db.execute(text("""
            SELECT s.id FROM sections s
            JOIN courses c ON s.course_id = c.id
            WHERE c.dept_id = :dept_id AND c.semester = 3 LIMIT 1
        """), {"dept_id": dept_id})
        sec_row = sec_res.fetchone()
        sec_id = sec_row[0] if sec_row else None

        if existing:
            user_id = existing[0]
            old_ok = verify_password(target_password, existing[1])
            await db.execute(
                text("""
                    UPDATE users
                    SET hashed_password = :h, is_deleted = false, is_active = true, role = 'STUDENT',
                        full_name = 'Priya Lakshmi', department_id = :dept_id
                    WHERE email = :email
                """),
                {"h": new_hash, "email": target_email, "dept_id": dept_id}
            )
            action = "updated"
        else:
            user_id = str(uuid.uuid4())
            old_ok = None
            await db.execute(
                text("""
                    INSERT INTO users (id, email, full_name, hashed_password, role, is_active, is_deleted,
                                       department_id, created_at, updated_at)
                    VALUES (:id, :email, 'Priya Lakshmi', :h, 'STUDENT', true, false,
                            :dept_id, NOW(), NOW())
                """),
                {"id": user_id, "email": target_email, "h": new_hash, "dept_id": dept_id}
            )
            action = "created"

        # Check if student profile exists
        profile_q = await db.execute(
            text("SELECT id FROM students WHERE user_id = :uid"),
            {"uid": user_id}
        )
        profile_row = profile_q.fetchone()

        import json
        from datetime import date
        skills = json.dumps(["Legal Research", "Moot Court", "Drafting"])
        languages = json.dumps(["English", "Tamil", "Hindi"])
        hobbies = json.dumps(["Reading", "Debating", "Chess"])
        special_skills = json.dumps(["Moot Court", "Legal Aid"])
        empty_list = json.dumps([])

        student_data = {
            "uid": user_id,
            "roll": "LAW-2026-001",
            "dept_id": dept_id,
            "deg_id": deg_id,
            "mentor_id": mentor_id,
            "sec_id": sec_id,
            "cgpa": 8.45,
            "skills": skills,
            "full_name": "Priya Lakshmi",
            "dob": date(2005, 5, 15),
            "gender": "Female",
            "blood_group": "O+",
            "nationality": "Indian",
            "mobile": "+919876543206",
            "address": "No. 45, Gandhi Street, Adyar, Chennai - 600020",
            "aadhaar": "1234-5678-9012",
            "passport": "Z1234567",
            "category": "General",
            "religion": "Hindu",
            "emergency_name": "K. R. Sundar",
            "emergency_rel": "Father",
            "emergency_num": "+919876543211",
            "father_name": "K. R. Sundar",
            "father_occ": "Advocate",
            "father_mob": "+919876543211",
            "father_email": "parent@cams.local",
            "mother_name": "S. Lakshmi",
            "mother_occ": "Teacher",
            "mother_mob": "+919876543222",
            "mother_email": "lakshmi@cams.local",
            "income": "₹8,00,000",
            "languages": languages,
            "hobbies": hobbies,
            "special_skills": special_skills,
            "empty_list": empty_list
        }

        profile_action = "already_exists"
        if profile_row:
            await db.execute(
                text("""
                    UPDATE students
                    SET roll_no = :roll, department_id = :dept_id, degree_id = :deg_id, mentor_id = :mentor_id,
                        section_id = :sec_id, cgpa = :cgpa, skills = :skills, full_name = :full_name,
                        date_of_birth = :dob, gender = :gender, blood_group = :blood_group, nationality = :nationality,
                        mobile_number = :mobile, current_address = :address, permanent_address = :address,
                        aadhaar_number = :aadhaar, passport_number = :passport, community_category = :category,
                        religion = :religion, emergency_contact_name = :emergency_name,
                        emergency_contact_relationship = :emergency_rel, emergency_contact_number = :emergency_num,
                        father_name = :father_name, father_occupation = :father_occ, father_mobile = :father_mob,
                        father_email = :father_email, mother_name = :mother_name, mother_occupation = :mother_occ,
                        mother_mobile = :mother_mob, mother_email = :mother_email, parent_annual_income = :income,
                        languages_known = :languages, hobbies_interests = :hobbies, special_skills = :special_skills,
                        certifications = :empty_list, internships = :empty_list, sports_records = :empty_list,
                        moot_courts = :empty_list, verification_status = 'VERIFIED_LOCKED', updated_at = NOW()
                    WHERE user_id = :uid
                """),
                student_data
            )
            profile_action = "updated"
        else:
            student_id = str(uuid.uuid4())
            student_data["id"] = student_id
            await db.execute(
                text("""
                    INSERT INTO students (
                        id, user_id, roll_no, department_id, semester, batch_year, mentor_id, degree_id,
                        section_id, cgpa, skills, full_name, date_of_birth, gender, blood_group, nationality,
                        mobile_number, current_address, permanent_address, aadhaar_number, passport_number,
                        community_category, religion, emergency_contact_name, emergency_contact_relationship,
                        emergency_contact_number, father_name, father_occupation, father_mobile, father_email,
                        mother_name, mother_occupation, mother_mobile, mother_email, parent_annual_income,
                        languages_known, hobbies_interests, special_skills, certifications, internships,
                        sports_records, moot_courts, verification_status, is_deleted, created_at, updated_at
                    ) VALUES (
                        :id, :uid, :roll, :dept_id, 3, 2024, :mentor_id, :deg_id,
                        :sec_id, :cgpa, :skills, :full_name, :dob, :gender, :blood_group, :nationality,
                        :mobile, :address, :address, :aadhaar, :passport,
                        :category, :religion, :emergency_name, :emergency_rel,
                        :emergency_num, :father_name, :father_occ, :father_mob, :father_email,
                        :mother_name, :mother_occ, :mother_mob, :mother_email, :income,
                        :languages, :hobbies, :special_skills, :empty_list, :empty_list,
                        :empty_list, :empty_list, 'VERIFIED_LOCKED', false, NOW(), NOW()
                    )
                """),
                student_data
            )
            profile_action = "created"

        await db.commit()

        return {
            "action": action,
            "profile_action": profile_action,
            "email": target_email,
            "password": target_password,
            "old_password_ok": old_ok,
            "user_id": user_id,
        }

    except Exception as e:
        await db.rollback()
        return {"action": "error", "error": str(e), "error_type": type(e).__name__}


@router.get("/debug/fix-thanush")
async def debug_fix_thanush(
    db: AsyncSession = Depends(get_db_session),
    current_user: User = Depends(role_required([UserRole.SUPER_ADMIN])),
) -> dict:
    """[DEV ONLY] Reset thanush@college.edu password to Password@123."""
    if settings.ENVIRONMENT == "production":
        raise HTTPException(status_code=404)

    from sqlalchemy import text

    target_email = "thanush@college.edu"
    target_password = "Password@123"
    new_hash = hash_password(target_password)

    try:
        row = await db.execute(
            text("SELECT id, hashed_password FROM users WHERE email = :email"),
            {"email": target_email}
        )
        existing = row.fetchone()
        if not existing:
            return {"status": "not_found", "email": target_email}

        old_ok = verify_password(target_password, existing[1])
        await db.execute(
            text("UPDATE users SET hashed_password = :h, is_deleted = false, is_active = true WHERE email = :email"),
            {"h": new_hash, "email": target_email}
        )
        await db.commit()
        return {"status": "updated", "email": target_email, "password": target_password, "old_password_ok": old_ok}
    except Exception as e:
        await db.rollback()
        return {"error": str(e)}


@router.get("/debug/fix-arun")
async def debug_fix_arun(
    db: AsyncSession = Depends(get_db_session),
    current_user: User = Depends(role_required([UserRole.SUPER_ADMIN])),
) -> dict:
    """[DEV ONLY] Reset arun@college.edu password to Password@123."""
    if settings.ENVIRONMENT == "production":
        raise HTTPException(status_code=404)

    from sqlalchemy import text

    target_email = "arun@college.edu"
    target_password = "Password@123"
    new_hash = hash_password(target_password)

    try:
        row = await db.execute(
            text("SELECT id, hashed_password, role, is_active, is_deleted FROM users WHERE email = :email"),
            {"email": target_email}
        )
        existing = row.fetchone()
        if not existing:
            return {"status": "not_found", "email": target_email}

        old_ok = verify_password(target_password, existing[1])
        await db.execute(
            text("UPDATE users SET hashed_password = :h, is_deleted = false, is_active = true WHERE email = :email"),
            {"h": new_hash, "email": target_email}
        )
        await db.commit()
        return {
            "status": "updated",
            "email": target_email,
            "password": target_password,
            "old_password_ok": old_ok,
            "role": existing[2],
            "was_active": existing[3],
            "was_deleted": existing[4],
        }
    except Exception as e:
        await db.rollback()
        return {"error": str(e)}
