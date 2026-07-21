"""
scripts/backfill_academic_hierarchy.py — Idempotent academic-hierarchy backfill.

The academic hierarchy is  Department -> Degree -> AcademicYear (cohort) -> Course -> Section.
Existing installs were missing the Degree and AcademicYear levels, and Courses were
not linked to a Degree (`courses.degree_id` was NULL). That made every screen built on
subject allocation non-functional (Admin Faculty Assignment, HOD Subject Allocation,
Batch Setup, Academic Year Config) because /subject-allocations/setup 404s with
"No degrees found for the department".

This script is SAFE to re-run: it only creates rows that are missing and never
deletes or overwrites existing data.

Run:  python -m scripts.backfill_academic_hierarchy
"""
import asyncio
from datetime import date

from sqlalchemy import select

from app.db.models.academic import AcademicYear, Course, Degree, Department
from app.db.session import AsyncSessionLocal


# Sensible defaults per department code. Departments not listed here get a
# generic degree derived from the department itself.
DEGREE_DEFAULTS = {
    "L1": dict(code="LLB", name="Bachelor of Legislative Law", program_level="UG", duration_years=3),
    "L2": dict(code="BA LLB", name="BA LLB (Hons)", program_level="UG", duration_years=5),
    "CSE": dict(code="BE CSE", name="B.E. Computer Science & Engineering", program_level="UG", duration_years=4),
}

CURRENT_BATCH = "2026-2031"
ACADEMIC_YEAR_NAME = "2026-2027"


async def backfill() -> None:
    async with AsyncSessionLocal() as db:
        created_degrees = 0
        created_years = 0
        linked_courses = 0

        depts = (await db.execute(
            select(Department).where(Department.is_deleted.is_(False))
        )).scalars().all()

        if not depts:
            print("[SKIP] No departments found — nothing to backfill.")
            return

        for dept in depts:
            spec = DEGREE_DEFAULTS.get(dept.code) or dict(
                code=dept.code,
                name=dept.name,
                program_level="UG",
                duration_years=4,
            )

            # ---- Degree (unique on code + program_level + applicable_batch) ----
            degree = (await db.execute(
                select(Degree).where(
                    Degree.code == spec["code"],
                    Degree.program_level == spec["program_level"],
                    Degree.applicable_batch == CURRENT_BATCH,
                    Degree.is_deleted.is_(False),
                )
            )).scalar_one_or_none()

            if not degree:
                degree = Degree(
                    code=spec["code"],
                    name=spec["name"],
                    applicable_batch=CURRENT_BATCH,
                    program_level=spec["program_level"],
                    duration_years=spec["duration_years"],
                    dept_id=dept.id,
                    passing_marks=40,
                )
                db.add(degree)
                await db.flush()
                created_degrees += 1
                print(f"[CREATE] Degree {degree.code} ({degree.name}) -> dept {dept.name}")
            else:
                if not degree.dept_id:
                    degree.dept_id = dept.id
                print(f"[OK]     Degree {degree.code} already exists")

            # ---- AcademicYear cohort ----
            ay = (await db.execute(
                select(AcademicYear).where(
                    AcademicYear.degree_id == degree.id,
                    AcademicYear.batch == CURRENT_BATCH,
                    AcademicYear.is_deleted.is_(False),
                )
            )).scalar_one_or_none()

            if not ay:
                ay = AcademicYear(
                    name=ACADEMIC_YEAR_NAME,
                    start_date=date(2026, 7, 1),
                    end_date=date(2027, 6, 30),
                    degree_id=degree.id,
                    batch=CURRENT_BATCH,
                    current_semester=1,
                    is_semester_open=True,
                    is_exam_period=False,
                    is_active=True,
                )
                db.add(ay)
                created_years += 1
                print(f"[CREATE] AcademicYear {ACADEMIC_YEAR_NAME} for {degree.code}")
            else:
                print(f"[OK]     AcademicYear for {degree.code} already exists")

            # ---- Link this department's courses to the degree ----
            courses = (await db.execute(
                select(Course).where(
                    Course.dept_id == dept.id,
                    Course.degree_id.is_(None),
                    Course.is_deleted.is_(False),
                )
            )).scalars().all()
            for course in courses:
                course.degree_id = degree.id
                linked_courses += 1
            if courses:
                print(f"[LINK]   {len(courses)} course(s) -> degree {degree.code}")

        await db.commit()

    print(
        f"\nDone. Degrees created: {created_degrees}, "
        f"AcademicYears created: {created_years}, Courses linked: {linked_courses}"
    )


if __name__ == "__main__":
    asyncio.run(backfill())
