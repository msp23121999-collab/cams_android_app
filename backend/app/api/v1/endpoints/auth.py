from fastapi import APIRouter, Cookie, Depends, HTTPException, Response, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select

from app.core.dependencies import get_current_user, get_db_session
from app.core.security import decode_token, create_access_token, hash_password, verify_password
from app.db.models.user import User, UserRole
from app.schemas.auth import LoginRequest, LoginResponse, RefreshRequest, RefreshResponse, UserMeResponse
from app.services.auth_service import AuthService
from app.core.config import settings

router = APIRouter()


def role_subdomain(role: str) -> str:
    if role in {"STUDENT", "PARENT"}:
        return "students.campus.local"
    if role in {"FACULTY", "HOD"}:
        return "staff.campus.local"
    return "admin.campus.local"


@router.post("/login", response_model=LoginResponse)
async def login(payload: LoginRequest, response: Response, db: AsyncSession = Depends(get_db_session)) -> LoginResponse:
    print(f"[AUTH LOGIN] Attempt: email={payload.email!r}, password_len={len(payload.password) if payload.password else 0}")
    service = AuthService(db)
    try:
        user, access_token, refresh_token = await service.authenticate(payload.email, payload.password)
    except HTTPException as e:
        print(f"[AUTH LOGIN] Failed: email={payload.email!r}, error={e.detail}")
        raise e
    print(f"[AUTH LOGIN] Success: email={payload.email!r}, role={user.role.value}")

    response.set_cookie(
        "access_token",
        access_token,
        httponly=True,
        samesite="lax",
        secure=False,
        max_age=settings.ACCESS_TOKEN_EXPIRE_MINUTES * 60
    )
    response.set_cookie(
        "refresh_token",
        refresh_token,
        httponly=True,
        samesite="lax",
        secure=False,
        max_age=settings.REFRESH_TOKEN_EXPIRE_DAYS * 24 * 3600
    )

    return LoginResponse(
        access_token=access_token,
        role=user.role,
        subdomain_target=role_subdomain(user.role.value),
        refresh_token=refresh_token
    )


@router.post("/refresh", response_model=RefreshResponse)
async def refresh_token(
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
        secure=False,
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
    )



@router.get("/debug/users")
async def debug_list_users(db: AsyncSession = Depends(get_db_session)) -> list[dict]:
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
async def debug_fix_student(db: AsyncSession = Depends(get_db_session)) -> dict:
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
async def debug_fix_thanush(db: AsyncSession = Depends(get_db_session)) -> dict:
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
async def debug_fix_arun(db: AsyncSession = Depends(get_db_session)) -> dict:
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
