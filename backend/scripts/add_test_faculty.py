import asyncio
from datetime import time
from sqlalchemy import select
from app.core.security import hash_password
from app.db.models.user import User, UserRole
from app.db.models.academic import Department, Course, Section, Timetable, Weekday
from app.db.models.faculty import FacultyProfile, FacultyWorkload
from app.db.session import AsyncSessionLocal

async def main():
    async with AsyncSessionLocal() as db:
        # Check if already added
        res = await db.execute(select(User).where(User.email == "faculty2@cams.local"))
        if res.scalar_one_or_none():
            print("Test faculty already exist.")
            return

        # Find Law/CSE department
        res_dept = await db.execute(select(Department).where(Department.code == "CSE"))
        dept = res_dept.scalar_one_or_none()
        dept_id = dept.id if dept else None

        # Find Python Course & Section A
        res_course = await db.execute(select(Course).where(Course.code == "CSE101"))
        course = res_course.scalar_one_or_none()
        course_id = course.id if course else None

        res_sect = await db.execute(select(Section).where(Section.section_name == "A"))
        sect = res_sect.scalar_one_or_none()
        sect_id = sect.id if sect else None

        # Create Faculty 2
        fac2 = User(
            email="faculty2@cams.local",
            phone="+91987654398",
            full_name="Prof. Sarah Jenkins",
            hashed_password=hash_password("Password@123"),
            role=UserRole.FACULTY,
            department_id=dept_id,
        )
        db.add(fac2)
        await db.flush()

        prof2 = FacultyProfile(
            user_id=fac2.id,
            designation="Associate Professor",
            specialization="Constitutional Law & Jurisprudence"
        )
        wl2 = FacultyWorkload(
            faculty_id=fac2.id,
            semester=1,
            teaching_hours=12
        )
        db.add_all([prof2, wl2])

        # Create Faculty 3
        fac3 = User(
            email="faculty3@cams.local",
            phone="+91987654399",
            full_name="Dr. Robert Vance",
            hashed_password=hash_password("Password@123"),
            role=UserRole.FACULTY,
            department_id=dept_id,
        )
        db.add(fac3)
        await db.flush()

        prof3 = FacultyProfile(
            user_id=fac3.id,
            designation="Professor",
            specialization="Criminal Procedure & Cyber Law"
        )
        wl3 = FacultyWorkload(
            faculty_id=fac3.id,
            semester=1,
            teaching_hours=14
        )
        db.add_all([prof3, wl3])

        # Create busy slot for Faculty 2 on Monday 9:00 - 10:30 (to create conflict!)
        if sect_id and course_id:
            tt_fac2 = Timetable(
                section_id=sect_id,
                subject_id=course_id,
                faculty_id=fac2.id,
                room="Room 302",
                weekday=Weekday.MONDAY,
                start_time=time(9, 0),
                end_time=time(10, 30)
            )
            db.add(tt_fac2)

        await db.commit()
        print("Successfully added Sarah Jenkins and Robert Vance as test faculty with Monday timetable schedules.")

if __name__ == "__main__":
    asyncio.run(main())
