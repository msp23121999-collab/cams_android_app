"""
scripts/seed.py — Production-grade seed data for CAMS Law College.

Covers ALL portal tabs for all 6 roles:
  Admin, Principal, HOD, Faculty, Student, Parent

Safety lock: Set RESET_DB=true AND RESET_DB_TOKEN=CONFIRM-WIPE-cams2026-xK9pQ3
to allow database cleanup before seeding.
"""
import asyncio
from datetime import date, datetime, time, timedelta, timezone
from sqlalchemy import select, text
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.security import hash_password
from app.db.models.academic import (
    Department, Course, Section, Timetable, TimetableApproval,
    Exam, ExamSetting, ApprovalStatus, Weekday, ExamType
)
from app.db.models.attendance import Attendance, AttendanceStatus
from app.db.models.communication import Notice, Notification, Message
from app.db.models.faculty import FacultyProfile, FacultyWorkload, FacultyResearch, PublicationPlan, ResearchCompliance
from app.db.models.fee import FeeStructure, FeeRecord, Payment, FeeStatus
from app.db.models.grievance import Grievance
from app.db.models.leave import LeaveRequest, LeaveApproval, LeaveStatus
from app.db.models.marks import Mark, MarkExamType
from app.db.models.payroll import Salary, Deduction, SalarySlip, DeductionType, WorkingDayConfig
from app.db.models.student import Student, ParentStudentMap
from app.db.models.study_material import StudyMaterial, Assignment
from app.db.models.user import User, UserRole
from app.db.session import AsyncSessionLocal

SEED_PASSWORD = "Password@123"

# =====================================================================
# SAFETY LOCK: Requires TWO matching env variables to allow database wipe.
# =====================================================================
SAFETY_RESET_TOKEN = "CONFIRM-WIPE-cams2026-xK9pQ3"

async def clean_database(db: AsyncSession) -> None:
    import os
    reset_flag = os.getenv("RESET_DB")
    reset_token = os.getenv("RESET_DB_TOKEN")

    if reset_flag != "true":
        print("[SAFE] Skipping cleanup: RESET_DB != 'true'. Data is protected.")
        return

    if reset_token != SAFETY_RESET_TOKEN:
        print("[SAFE] RESET_DB_TOKEN mismatch — cleanup aborted.")
        return

    env = os.getenv("ENVIRONMENT", "").lower()
    if env == "production":
        print("[BLOCKED] Database wipe blocked: ENVIRONMENT=production.")
        return

    print("WARNING: Database wipe authorised. Proceeding with TRUNCATE...")
    tables = [
        "payments", "fee_records", "fee_structure", "deductions", "salary_slips", "salary",
        "attendance", "marks", "exam_settings", "exams", "timetable_approvals", "timetable",
        "assignments", "study_materials", "sections", "courses", "leave_approvals", "leaves",
        "grievances", "notices", "notifications", "messages", "parent_student_map", "students",
        "faculty_research", "faculty_workload", "faculty_profiles", "publication_plans",
        "research_compliance", "activity_logs", "audit_logs", "working_day_config", "users", "departments"
    ]
    for table in tables:
        try:
            await db.execute(text(f"TRUNCATE TABLE {table} CASCADE"))
        except Exception:
            pass
    await db.commit()
    print("Database wiped successfully.")


async def seed() -> None:
    async with AsyncSessionLocal() as db:
        # Skip if data already exists (safety guard)
        user_check = await db.execute(select(User).limit(1))
        if user_check.scalars().first() is not None:
            print("[SAFE] Database already contains user data. Skipping seed.")
            return

        today = date.today()
        now = datetime.utcnow()

        # ─── 1. DEPARTMENTS ───────────────────────────────────────────────────
        llb_dept = Department(
            name="LLB", code="L1", program_level="UG",
            duration_years=3, sem_count=6
        )
        ba_llb_dept = Department(
            name="BA LLB", code="L2", program_level="INTEGRATED",
            duration_years=5, sem_count=10
        )
        cse_dept = Department(
            name="Computer Science & Engineering", code="CSE", program_level="UG",
            duration_years=4, sem_count=8
        )
        db.add_all([llb_dept, ba_llb_dept, cse_dept])
        await db.flush()

        # ─── 2. USERS ─────────────────────────────────────────────────────────
        hashed_pwd = hash_password(SEED_PASSWORD)

        admin_user = User(email="admin@cams.local", phone="+919876543200", full_name="Ananya Sharma", hashed_password=hashed_pwd, role=UserRole.ADMIN)
        principal_user = User(email="principal@cams.local", phone="+919876543201", full_name="Dr. R. Krishnamurti", hashed_password=hashed_pwd, role=UserRole.PRINCIPAL)

        hod_user = User(email="hod@cams.local", phone="+919876543202", full_name="Dr. Meena Sundar", hashed_password=hashed_pwd, role=UserRole.HOD, department_id=ba_llb_dept.id)

        fac1_user = User(email="faculty@cams.local", phone="+919876543203", full_name="Dr. Vivek Anand", hashed_password=hashed_pwd, role=UserRole.FACULTY, department_id=ba_llb_dept.id)
        fac2_user = User(email="faculty2@cams.local", phone="+919876543204", full_name="Dr. K. Rajalakshmi", hashed_password=hashed_pwd, role=UserRole.FACULTY, department_id=ba_llb_dept.id)
        fac3_user = User(email="faculty3@cams.local", phone="+919876543205", full_name="Prof. Arjun Nair", hashed_password=hashed_pwd, role=UserRole.FACULTY, department_id=ba_llb_dept.id)

        stu1_user = User(email="student@cams.local", phone="+919876543206", full_name="Priya Lakshmi", hashed_password=hashed_pwd, role=UserRole.STUDENT, department_id=ba_llb_dept.id)
        stu2_user = User(email="student2@cams.local", phone="+919876543207", full_name="Rajan Mehta", hashed_password=hashed_pwd, role=UserRole.STUDENT, department_id=ba_llb_dept.id)
        stu3_user = User(email="student3@cams.local", phone="+919876543208", full_name="Kavitha Devi", hashed_password=hashed_pwd, role=UserRole.STUDENT, department_id=ba_llb_dept.id)
        stu4_user = User(email="student4@cams.local", phone="+919876543209", full_name="Arun Balaji", hashed_password=hashed_pwd, role=UserRole.STUDENT, department_id=ba_llb_dept.id)
        stu5_user = User(email="student5@cams.local", phone="+919876543210", full_name="Sneha Patel", hashed_password=hashed_pwd, role=UserRole.STUDENT, department_id=ba_llb_dept.id)

        parent1_user = User(email="parent@cams.local", phone="+919876543211", full_name="K. R. Sundar", hashed_password=hashed_pwd, role=UserRole.PARENT)
        parent2_user = User(email="parent2@cams.local", phone="+919876543212", full_name="Suresh Mehta (Parent)", hashed_password=hashed_pwd, role=UserRole.PARENT)

        db.add_all([
            admin_user, principal_user, hod_user,
            fac1_user, fac2_user, fac3_user,
            stu1_user, stu2_user, stu3_user, stu4_user, stu5_user,
            parent1_user, parent2_user
        ])
        await db.flush()

        # Link HOD to department
        ba_llb_dept.hod_id = hod_user.id
        db.add(ba_llb_dept)

        # ─── 3. FACULTY PROFILES, WORKLOADS, RESEARCH ────────────────────────
        fac1_profile = FacultyProfile(user_id=fac1_user.id, designation="Associate Professor", specialization="Constitutional Law & Jurisprudence")
        fac2_profile = FacultyProfile(user_id=fac2_user.id, designation="Assistant Professor", specialization="Intellectual Property Law")
        fac3_profile = FacultyProfile(user_id=fac3_user.id, designation="Assistant Professor", specialization="Criminal Law & Procedure")
        db.add_all([fac1_profile, fac2_profile, fac3_profile])

        fac1_workload = FacultyWorkload(faculty_id=fac1_user.id, semester=1, teaching_hours=16)
        fac2_workload = FacultyWorkload(faculty_id=fac2_user.id, semester=1, teaching_hours=20)
        fac3_workload = FacultyWorkload(faculty_id=fac3_user.id, semester=1, teaching_hours=10)
        db.add_all([fac1_workload, fac2_workload, fac3_workload])

        # Research for fac1
        r1 = FacultyResearch(faculty_id=fac1_user.id, title="Constitutional Interpretation in the Digital Age", publication="Indian Law Journal, 2026", grant_amount=125000.00, status="APPROVED")
        r2 = FacultyResearch(faculty_id=fac1_user.id, title="Privacy and Data Protection Laws in the Digital Age", publication="Journal of Legal Studies, 2026", grant_amount=90000.00, status="PENDING")
        r3 = FacultyResearch(faculty_id=fac2_user.id, title="Intellectual Property Rights in Biotechnology", publication="IP Law Review, 2026", grant_amount=75000.00, status="APPROVED")
        r4 = FacultyResearch(faculty_id=fac3_user.id, title="Bail Jurisprudence under the BNSS 2023", publication="Criminal Law Quarterly, 2026", grant_amount=60000.00, status="PENDING")
        db.add_all([r1, r2, r3, r4])

        pub1 = PublicationPlan(faculty_id=fac1_user.id, title="Deep Learning in Legal Curriculum Analysis", journal_conference="Journal of Legal Education, 2026", target_date=date(2026, 12, 1), status="PLANNED")
        pub2 = PublicationPlan(faculty_id=fac1_user.id, title="Predictive Judicial Decisions via Neural Transformers", journal_conference="ICAIL 2026", target_date=date(2026, 9, 15), status="IN_PROGRESS")
        pub3 = PublicationPlan(faculty_id=fac2_user.id, title="Trade Marks in the Metaverse", journal_conference="JIPR, 2026", target_date=date(2026, 8, 1), status="PLANNED")
        db.add_all([pub1, pub2, pub3])

        comp1 = ResearchCompliance(faculty_id=fac1_user.id, requirement_name="Annual Research Output Declaration", deadline=date(2026, 7, 31), status="PENDING")
        comp2 = ResearchCompliance(faculty_id=fac1_user.id, requirement_name="Research Ethics Board Certification", deadline=date(2026, 5, 15), status="COMPLETED", submitted_at=now - timedelta(days=20))
        comp3 = ResearchCompliance(faculty_id=fac2_user.id, requirement_name="Grant Progress Audit Q1", deadline=date(2026, 3, 1), status="OVERDUE")
        db.add_all([comp1, comp2, comp3])

        # ─── 4. STUDENTS ──────────────────────────────────────────────────────
        stu1 = Student(user_id=stu1_user.id, roll_no="LAW-2026-001", department_id=ba_llb_dept.id, semester=3, batch_year=2024, mentor_id=fac1_user.id, cgpa=8.45, skills=["Legal Research", "Moot Court", "Drafting"])
        stu2 = Student(user_id=stu2_user.id, roll_no="LAW-2026-002", department_id=ba_llb_dept.id, semester=3, batch_year=2024, mentor_id=fac1_user.id, cgpa=7.80, skills=["Contract Drafting", "Arbitration"])
        stu3 = Student(user_id=stu3_user.id, roll_no="LAW-2026-003", department_id=ba_llb_dept.id, semester=1, batch_year=2026, mentor_id=fac2_user.id, cgpa=9.10, skills=["Research", "Legal Writing"])
        stu4 = Student(user_id=stu4_user.id, roll_no="LAW-2026-004", department_id=ba_llb_dept.id, semester=1, batch_year=2026, mentor_id=fac2_user.id, cgpa=7.20, skills=["Advocacy", "Negotiation"])
        stu5 = Student(user_id=stu5_user.id, roll_no="LAW-2026-005", department_id=ba_llb_dept.id, semester=3, batch_year=2024, mentor_id=fac3_user.id, cgpa=8.90, skills=["Moot Court", "Legal Aid"])
        db.add_all([stu1, stu2, stu3, stu4, stu5])
        await db.flush()

        # Parent maps
        pm1 = ParentStudentMap(parent_id=parent1_user.id, student_id=stu1.id)
        pm2 = ParentStudentMap(parent_id=parent2_user.id, student_id=stu2.id)
        db.add_all([pm1, pm2])

        # ─── 5. COURSES & SECTIONS ────────────────────────────────────────────
        c1 = Course(dept_id=ba_llb_dept.id, code="LAW101", name="Introduction to Constitutional Law", credits=4, semester=1)
        c2 = Course(dept_id=ba_llb_dept.id, code="LAW102", name="Contract Law & Obligations", credits=4, semester=1)
        c3 = Course(dept_id=ba_llb_dept.id, code="LAW201", name="Criminal Law & Procedure", credits=4, semester=3)
        c4 = Course(dept_id=ba_llb_dept.id, code="LAW202", name="Intellectual Property Law", credits=3, semester=3)
        db.add_all([c1, c2, c3, c4])
        await db.flush()

        sec_a = Section(course_id=c1.id, section_name="A", faculty_id=fac1_user.id)
        sec_b = Section(course_id=c2.id, section_name="A", faculty_id=fac2_user.id)
        sec_c = Section(course_id=c3.id, section_name="A", faculty_id=fac3_user.id)
        sec_d = Section(course_id=c4.id, section_name="A", faculty_id=fac2_user.id)
        db.add_all([sec_a, sec_b, sec_c, sec_d])
        await db.flush()

        # ─── 6. TIMETABLE ─────────────────────────────────────────────────────
        tt_entries = [
            Timetable(section_id=sec_a.id, subject_id=c1.id, faculty_id=fac1_user.id, room="Hall A-101", weekday=Weekday.MONDAY, start_time=time(9, 0), end_time=time(10, 30)),
            Timetable(section_id=sec_a.id, subject_id=c1.id, faculty_id=fac1_user.id, room="Hall A-101", weekday=Weekday.WEDNESDAY, start_time=time(11, 0), end_time=time(12, 30)),
            Timetable(section_id=sec_b.id, subject_id=c2.id, faculty_id=fac2_user.id, room="Hall B-201", weekday=Weekday.TUESDAY, start_time=time(9, 0), end_time=time(10, 30)),
            Timetable(section_id=sec_b.id, subject_id=c2.id, faculty_id=fac2_user.id, room="Hall B-201", weekday=Weekday.THURSDAY, start_time=time(11, 0), end_time=time(12, 30)),
            Timetable(section_id=sec_c.id, subject_id=c3.id, faculty_id=fac3_user.id, room="Hall C-301", weekday=Weekday.MONDAY, start_time=time(14, 0), end_time=time(15, 30)),
            Timetable(section_id=sec_c.id, subject_id=c3.id, faculty_id=fac3_user.id, room="Hall C-301", weekday=Weekday.FRIDAY, start_time=time(9, 0), end_time=time(10, 30)),
            Timetable(section_id=sec_d.id, subject_id=c4.id, faculty_id=fac2_user.id, room="Hall B-202", weekday=Weekday.WEDNESDAY, start_time=time(14, 0), end_time=time(15, 30)),
        ]
        db.add_all(tt_entries)
        await db.flush()

        tt_approval = TimetableApproval(timetable_id=tt_entries[0].id, status=ApprovalStatus.APPROVED, approved_by=principal_user.id)
        db.add(tt_approval)

        # ─── 7. ATTENDANCE (30 days per section) ──────────────────────────────
        attendance_records = []
        for i in range(30):
            d = today - timedelta(days=i)
            if d.weekday() < 6:  # Mon-Sat
                for sec in [sec_a, sec_b, sec_c]:
                    absentees = [stu1.user_id] if i % 5 == 0 else []
                    ods = [stu2.user_id] if i % 8 == 0 else []
                    attendance_records.append(Attendance(
                        section_id=sec.id,
                        subject_id=c1.id,
                        faculty_id=fac1_user.id,
                        date=d,
                        hour="1",
                        absentee_ids=absentees,
                        od_ids=ods
                    ))
        db.add_all(attendance_records)

        # ─── 8. MARKS ─────────────────────────────────────────────────────────
        marks_data = [
            (stu1, sec_a, MarkExamType.CIA, 42.50, 50.00),
            (stu1, sec_a, MarkExamType.SEMESTER, 85.00, 100.00),
            (stu1, sec_b, MarkExamType.CIA, 44.00, 50.00),
            (stu2, sec_a, MarkExamType.CIA, 38.00, 50.00),
            (stu2, sec_a, MarkExamType.SEMESTER, 76.00, 100.00),
            (stu2, sec_b, MarkExamType.CIA, 40.00, 50.00),
            (stu3, sec_c, MarkExamType.CIA, 47.00, 50.00),
            (stu3, sec_d, MarkExamType.CIA, 45.50, 50.00),
            (stu4, sec_c, MarkExamType.CIA, 33.00, 50.00),
            (stu5, sec_c, MarkExamType.CIA, 46.00, 50.00),
            (stu5, sec_d, MarkExamType.CIA, 43.00, 50.00),
        ]
        db.add_all([Mark(student_id=s.id, section_id=sec.id, exam_type=et, mark=m, max_mark=mx) for s, sec, et, m, mx in marks_data])

        # ─── 9. EXAMS ─────────────────────────────────────────────────────────
        exam1 = Exam(course_id=c1.id, type=ExamType.SEMESTER, center="Block A – Room 102", date=today + timedelta(days=10), start_time=time(10, 0), end_time=time(13, 0))
        exam2 = Exam(course_id=c3.id, type=ExamType.CIA, center="Block C – Room 302", date=today + timedelta(days=5), start_time=time(9, 0), end_time=time(10, 0))
        db.add_all([exam1, exam2])
        await db.flush()
        db.add(ExamSetting(exam_id=exam1.id, halls="Block A Room 102, Block B Room 204", rules="No electronic devices. Bring ID cards.", is_published=True))
        db.add(ExamSetting(exam_id=exam2.id, halls="Block C Room 302", rules="Open notes allowed.", is_published=True))

        # ─── 10. FEES ─────────────────────────────────────────────────────────
        fs_tuition = FeeStructure(dept_id=ba_llb_dept.id, semester=1, amount=75000.00, due_date=today + timedelta(days=15), fee_type="Tuition Fee")
        fs_hostel = FeeStructure(dept_id=ba_llb_dept.id, semester=1, amount=45000.00, due_date=today + timedelta(days=15), fee_type="Hostel Fee")
        fs_library = FeeStructure(dept_id=ba_llb_dept.id, semester=1, amount=5000.00, due_date=today + timedelta(days=20), fee_type="Library Fee")
        db.add_all([fs_tuition, fs_hostel, fs_library])
        await db.flush()

        for stu_profile, pay_tuition, pay_hostel in [
            (stu1, True, True),
            (stu2, True, False),
            (stu3, False, False),
            (stu4, True, False),
            (stu5, False, False),
        ]:
            fr_t = FeeRecord(student_id=stu_profile.id, fee_structure_id=fs_tuition.id, status=FeeStatus.PAID if pay_tuition else FeeStatus.PENDING)
            fr_h = FeeRecord(student_id=stu_profile.id, fee_structure_id=fs_hostel.id, status=FeeStatus.PAID if pay_hostel else FeeStatus.PENDING)
            fr_l = FeeRecord(student_id=stu_profile.id, fee_structure_id=fs_library.id, status=FeeStatus.PENDING)
            db.add_all([fr_t, fr_h, fr_l])
            await db.flush()
            if pay_tuition:
                db.add(Payment(fee_record_id=fr_t.id, amount=75000.00, mode="UPI", txn_id=f"TXN{stu_profile.id[:6].upper()}", paid_at=now - timedelta(days=10)))
            if pay_hostel:
                db.add(Payment(fee_record_id=fr_h.id, amount=45000.00, mode="Net Banking", txn_id=f"TXNH{stu_profile.id[:6].upper()}", paid_at=now - timedelta(days=8)))

        # ─── 11. LEAVE REQUESTS ───────────────────────────────────────────────
        leaves = [
            LeaveRequest(user_id=stu1_user.id, type="Medical Leave", from_date=today + timedelta(days=2), to_date=today + timedelta(days=4), reason="Recovering from viral fever.", status=LeaveStatus.PENDING, emergency_contact="9999999901"),
            LeaveRequest(user_id=stu2_user.id, type="Casual Leave", from_date=today - timedelta(days=5), to_date=today - timedelta(days=4), reason="Family function.", status=LeaveStatus.APPROVED, emergency_contact="9999999902"),
            LeaveRequest(user_id=stu3_user.id, type="OD", from_date=today + timedelta(days=1), to_date=today + timedelta(days=1), reason="State Moot Court competition.", status=LeaveStatus.APPROVED, emergency_contact="9999999903"),
            LeaveRequest(user_id=fac1_user.id, type="Casual Leave", from_date=today + timedelta(days=5), to_date=today + timedelta(days=6), reason="Attending family wedding.", status=LeaveStatus.PENDING, emergency_contact="9999999904"),
            LeaveRequest(user_id=fac2_user.id, type="Academic Leave", from_date=today - timedelta(days=10), to_date=today - timedelta(days=8), reason="Research conference at IIT Madras.", status=LeaveStatus.APPROVED, emergency_contact="9999999905"),
            LeaveRequest(user_id=fac3_user.id, type="Medical Leave", from_date=today - timedelta(days=3), to_date=today - timedelta(days=2), reason="Medical treatment.", status=LeaveStatus.REJECTED, emergency_contact="9999999906"),
        ]
        db.add_all(leaves)

        # ─── 12. GRIEVANCES ───────────────────────────────────────────────────
        grievances = [
            Grievance(raised_by=stu1_user.id, category="Infrastructure", description="The air conditioning in Hall A-101 is non-functional for 3 weeks.", status="PENDING", assigned_to=admin_user.id),
            Grievance(raised_by=stu2_user.id, category="Academic", description="The CIA marks for Contract Law are not updated in the portal.", status="IN_PROGRESS", assigned_to=hod_user.id),
            Grievance(raised_by=stu3_user.id, category="Library", description="Request for more copies of 'Jurisprudence' by V.D. Mahajan.", status="RESOLVED", assigned_to=admin_user.id),
            Grievance(raised_by=stu4_user.id, category="Fees", description="The hostel fee receipt is not visible in my portal.", status="PENDING", assigned_to=admin_user.id),
            Grievance(raised_by=stu5_user.id, category="Ragging", description="Senior students misbehaving in hostel corridor.", status="IN_PROGRESS", assigned_to=principal_user.id),
        ]
        db.add_all(grievances)

        # ─── 13. NOTICES ──────────────────────────────────────────────────────
        notices = [
            Notice(created_by=principal_user.id, title="End Semester Exam Schedule Published", body="Semester final exams commence from June 25th, 2026. Check your portals for date sheets and hall allotments.", audience_type="STUDENT", publish_date=today),
            Notice(created_by=hod_user.id, title="HOD Board Meeting: Curriculum Review", body="Faculty members, there is a curriculum review meeting at the HOD Cabin today at 3:00 PM.", audience_type="FACULTY", publish_date=today),
            Notice(created_by=admin_user.id, title="Maintenance Closure: Hostel Block B Elevator", body="Hostel Block B elevator will be down for servicing on Saturday from 9 AM to 1 PM.", audience_type="PARENT", publish_date=today),
            Notice(created_by=principal_user.id, title="National Moot Court Registration Open", body="Students interested in the All India Moot Court 2026 must register with the Law Coordinator by June 20th.", audience_type="STUDENT", publish_date=today - timedelta(days=2)),
            Notice(created_by=hod_user.id, title="Research Grant Deadline Reminder", body="All faculty must submit their Annual Research Output Declaration by July 31st.", audience_type="FACULTY", publish_date=today - timedelta(days=3)),
            Notice(created_by=admin_user.id, title="Fee Payment Deadline: July 15th", body="Students with pending tuition or hostel fee must pay before July 15, 2026 to avoid late fine.", audience_type="ALL", publish_date=today - timedelta(days=1)),
        ]
        db.add_all(notices)

        # ─── 14. STUDY MATERIALS & ASSIGNMENTS ───────────────────────────────
        materials = [
            StudyMaterial(section_id=sec_a.id, faculty_id=fac1_user.id, title="Constitutional Law – Unit 1 Slides", type="Lecture Slides", file_url="/uploads/study_materials/law101_unit1.pdf", is_verified=True, status="APPROVED"),
            StudyMaterial(section_id=sec_a.id, faculty_id=fac1_user.id, title="Fundamental Rights – Case Notes", type="Lecture Notes", file_url="/uploads/study_materials/fundamental_rights.pdf", is_verified=True, status="APPROVED"),
            StudyMaterial(section_id=sec_b.id, faculty_id=fac2_user.id, title="Contract Law – Offer & Acceptance Notes", type="Lecture Notes", file_url="/uploads/study_materials/contract_law.pdf", is_verified=False, status="PENDING"),
            StudyMaterial(section_id=sec_c.id, faculty_id=fac3_user.id, title="BNSS 2023 – Overview Presentation", type="Lecture Slides", file_url="/uploads/study_materials/bnss_overview.pdf", is_verified=True, status="APPROVED"),
            StudyMaterial(section_id=sec_d.id, faculty_id=fac2_user.id, title="IP Law – Trademarks & Passing Off", type="Lecture Notes", file_url="/uploads/study_materials/ip_trademarks.pdf", is_verified=False, status="PENDING"),
        ]
        db.add_all(materials)

        assignments = [
            Assignment(section_id=sec_a.id, faculty_id=fac1_user.id, title="Assignment 1: Case Study on Fundamental Rights", deadline=today + timedelta(days=5), submission_count=3),
            Assignment(section_id=sec_b.id, faculty_id=fac2_user.id, title="Assignment 1: Draft a Sale Agreement", deadline=today + timedelta(days=7), submission_count=1),
            Assignment(section_id=sec_c.id, faculty_id=fac3_user.id, title="Assignment 1: Analyse a Criminal Case Judgment", deadline=today + timedelta(days=10), submission_count=4),
        ]
        db.add_all(assignments)

        # ─── 15. SALARY & PAYROLL ─────────────────────────────────────────────
        for fac_user, basic, allowances in [
            (fac1_user, 80000.00, 15000.00),
            (fac2_user, 70000.00, 12500.00),
            (fac3_user, 65000.00, 10000.00),
        ]:
            for month, year in [(5, 2026), (6, 2026)]:
                sal = Salary(faculty_id=fac_user.id, basic=basic, allowances=allowances, gross=basic + allowances, month=month, year=year)
                db.add(sal)
                await db.flush()
                ded = Deduction(salary_id=sal.id, type=DeductionType.PF, days=0, amount=round(basic * 0.12, 2))
                slip = SalarySlip(salary_id=sal.id, pdf_url=f"/uploads/payroll/slip_{fac_user.id[:4]}_{year}_{month:02d}.pdf", generated_at=now - timedelta(days=5), delivered_at=now - timedelta(days=4))
                db.add_all([ded, slip])

        # ─── 16. NOTIFICATIONS ────────────────────────────────────────────────
        notifs = [
            Notification(user_id=stu1_user.id, type="announcement", message="End Semester Exam Schedule has been published.", is_read=False, sent_via="In-App"),
            Notification(user_id=stu1_user.id, type="leave", message="Your Medical Leave request is under review.", is_read=True, sent_via="In-App"),
            Notification(user_id=fac1_user.id, type="assignment", message="3 students have submitted Assignment 1.", is_read=False, sent_via="In-App"),
            Notification(user_id=hod_user.id, type="leave", message="Faculty Dr. Vivek Anand has applied for Casual Leave.", is_read=False, sent_via="In-App"),
            Notification(user_id=stu2_user.id, type="marks", message="Your CIA marks for Contract Law have been updated.", is_read=False, sent_via="In-App"),
        ]
        db.add_all(notifs)

        # ─── 17. MESSAGES ─────────────────────────────────────────────────────
        messages = [
            Message(sender_id=hod_user.id, receiver_id=fac1_user.id, body="Dr. Vivek, please submit your research plan by end of week.", is_read=True, read_at=now - timedelta(hours=2)),
            Message(sender_id=fac1_user.id, receiver_id=hod_user.id, body="Sure HOD. I have uploaded the draft. Please review.", is_read=False),
            Message(sender_id=stu1_user.id, receiver_id=hod_user.id, body="HOD Ma'am, regarding my attendance waiver for the moot court, when will it be processed?", is_read=True, read_at=now - timedelta(hours=1)),
            Message(sender_id=hod_user.id, receiver_id=stu1_user.id, body="Your request is under review by the academic committee. We will update you by tomorrow.", is_read=False),
            Message(sender_id=fac2_user.id, receiver_id=principal_user.id, body="Principal Sir, the IP Law textbooks in the library are outdated. Could we request for an update?", is_read=False),
        ]
        db.add_all(messages)

        # ─── 18. WORKING DAY CONFIG ───────────────────────────────────────────
        working_configs = [
            WorkingDayConfig(month=5, year=2026, total_working_days=26, overrides_json='{}'),
            WorkingDayConfig(month=6, year=2026, total_working_days=25, overrides_json='{"2026-06-11": "holiday"}'),
        ]
        db.add_all(working_configs)

        # ─── 19. LOCAL JSON DB (Fee Blueprints & Scholarship Types) ───────────
        # Create table if not exists
        await db.execute(text("""
            CREATE TABLE IF NOT EXISTS local_json_db (
                key VARCHAR(255) PRIMARY KEY,
                data JSONB,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """))
        
        import json
        fee_blueprints = [
            {
                "id": "31b29971-b254-4488-96c6-5638fa323d72",
                "degreeCode": "BA LLB",
                "regCode": "BA LLB",
                "degreeName": "BA LLB (2025-2030)",
                "batch": "2025",
                "programLevel": "INTEGRATED",
                "durationYears": 5,
                "fees": {
                    "1": {"government": "85000", "management": "120000", "nri": "150000"},
                    "2": {"government": "85000", "management": "120000", "nri": "150000"},
                    "3": {"government": "85000", "management": "120000", "nri": "150000"},
                    "4": {"government": "85000", "management": "120000", "nri": "150000"},
                    "5": {"government": "85000", "management": "120000", "nri": "150000"}
                }
            },
            {
                "id": "ba26ba26-1111-2222-3333-444444444444",
                "degreeCode": "BA LLB",
                "regCode": "BA LLB",
                "degreeName": "BA LLB (2026-2031)",
                "batch": "2026",
                "programLevel": "INTEGRATED",
                "durationYears": 5,
                "fees": {
                    "1": {"government": "85000", "management": "120000", "nri": "150000"},
                    "2": {"government": "85000", "management": "120000", "nri": "150000"},
                    "3": {"government": "85000", "management": "120000", "nri": "150000"},
                    "4": {"government": "85000", "management": "120000", "nri": "150000"},
                    "5": {"government": "85000", "management": "120000", "nri": "150000"}
                }
            },
            {
                "id": "9fd9a17d-1b3a-4533-b943-7a3fcf5df2b5",
                "degreeCode": "LLB",
                "regCode": "LLB",
                "degreeName": "LLB (2025-2028)",
                "batch": "2025",
                "programLevel": "UG",
                "durationYears": 3,
                "fees": {
                    "1": {"government": "85000", "management": "120000", "nri": "150000"},
                    "2": {"government": "85000", "management": "120000", "nri": "150000"},
                    "3": {"government": "85000", "management": "120000", "nri": "150000"}
                }
            },
            {
                "id": "llb26llb-1111-2222-3333-444444444444",
                "degreeCode": "LLB",
                "regCode": "LLB",
                "degreeName": "LLB (2026-2029)",
                "batch": "2026",
                "programLevel": "UG",
                "durationYears": 3,
                "fees": {
                    "1": {"government": "85000", "management": "120000", "nri": "150000"},
                    "2": {"government": "85000", "management": "120000", "nri": "150000"},
                    "3": {"government": "85000", "management": "120000", "nri": "150000"}
                }
            }
        ]
        
        scholarship_types = [
            {
                "id": "st-scholar",
                "name": "ST Scholar",
                "description": "100% tuition fee waiver for Scheduled Tribe category students.",
                "reduction_type": "percentage",
                "reduction_value": 100.0,
                "scope": "all_batches",
                "batch_year": "",
                "department_id": "all",
                "program_level": "all"
            },
            {
                "id": "merit-scholar",
                "name": "Merit Scholarship",
                "description": "50% tuition fee waiver for academic excellence and top rankers.",
                "reduction_type": "percentage",
                "reduction_value": 50.0,
                "scope": "all_batches",
                "batch_year": "",
                "department_id": "all",
                "program_level": "all"
            },
            {
                "id": "sports-concession",
                "name": "Sports Concession",
                "description": "25% tuition fee waiver for national/state level sports achievers.",
                "reduction_type": "percentage",
                "reduction_value": 25.0,
                "scope": "all_batches",
                "batch_year": "",
                "department_id": "all",
                "program_level": "all"
            },
            {
                "id": "ews-concession",
                "name": "EWS Concession",
                "description": "Flat Rs. 20,000 fee concession for Economically Weaker Section category students.",
                "reduction_type": "flat",
                "reduction_value": 20000.0,
                "scope": "all_batches",
                "batch_year": "",
                "department_id": "all",
                "program_level": "all"
            }
        ]

        await db.execute(text("""
            INSERT INTO local_json_db (key, data, updated_at)
            VALUES (:key, :data, CURRENT_TIMESTAMP)
            ON CONFLICT (key) DO NOTHING;
        """), {
            "key": "fee_blueprints_list.json",
            "data": json.dumps(fee_blueprints)
        })

        await db.execute(text("""
            INSERT INTO local_json_db (key, data, updated_at)
            VALUES (:key, :data, CURRENT_TIMESTAMP)
            ON CONFLICT (key) DO NOTHING;
        """), {
            "key": "scholarship_types_list.json",
            "data": json.dumps(scholarship_types)
        })

        # ─── COMMIT ALL ───────────────────────────────────────────────────────
        await db.commit()
        print("[OK] Database successfully seeded with comprehensive law college data.")
        print(f"   Roles: Admin, Principal, HOD, 3 Faculty, 5 Students, 2 Parents")
        print(f"   Login: <role>@cams.local  |  Password: {SEED_PASSWORD}")
        print(f"   Role emails: admin, principal, hod, faculty, faculty2, faculty3,")
        print(f"                student, student2, student3, student4, student5,")
        print(f"                parent, parent2 — all @cams.local")


if __name__ == "__main__":
    async def main():
        async with AsyncSessionLocal() as session:
            try:
                await clean_database(session)
            except Exception as e:
                print(f"Cleanup step error: {e}")
        await seed()

    asyncio.run(main())
