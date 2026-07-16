"""
Seed Attendance Data — June 16 to June 23, 2026
================================================
Generates realistic session-based attendance for 8 working days (skip Sundays).
Uses timetable to get subject+section+faculty for each day.
Marks ~88% present, ~10% absent, ~2% OD for realism.
"""

import asyncio
import os
import sys
import random
from datetime import date, timedelta

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from dotenv import load_dotenv
load_dotenv()

from sqlalchemy.ext.asyncio import create_async_engine, async_sessionmaker, AsyncSession
from sqlalchemy import select, delete
from app.db.models.student import Student
from app.db.models.user import User
from app.db.models.academic import Timetable, Weekday, Section, Course
from app.db.models.attendance import Attendance

DATABASE_URL = os.getenv("DATABASE_URL", "")
if not DATABASE_URL:
    raise ValueError("DATABASE_URL not set in .env")

engine = create_async_engine(DATABASE_URL, echo=False)
SessionLocal = async_sessionmaker(engine, class_=AsyncSession, expire_on_commit=False)

START_DATE = date(2026, 6, 16)
END_DATE   = date(2026, 6, 23)

WEEKDAY_MAP = {
    0: Weekday.MONDAY,
    1: Weekday.TUESDAY,
    2: Weekday.WEDNESDAY,
    3: Weekday.THURSDAY,
    4: Weekday.FRIDAY,
    5: Weekday.SATURDAY,
    6: None,  # Sunday — skip
}

async def seed():
    async with SessionLocal() as db:
        # Clear existing attendance records in this range to prevent duplicates
        print("Clearing existing attendance records between June 16 and June 23...")
        del_stmt = delete(Attendance).where(
            Attendance.date >= START_DATE,
            Attendance.date <= END_DATE
        )
        await db.execute(del_stmt)
        await db.commit()

        # Load all active students
        students_q = await db.execute(
            select(Student).where(Student.is_deleted.is_(False))
        )
        students = students_q.scalars().all()
        print(f"Found {len(students)} students")

        # Load all sections and courses
        sections_q = await db.execute(
            select(Section, Course)
            .join(Course, Section.course_id == Course.id)
            .where(Section.is_deleted.is_(False))
        )
        sections_list = sections_q.all()
        print(f"Found {len(sections_list)} sections")

        # Get section names
        sec_names = {}
        for sec, course in sections_list:
            sec_names[sec.id] = sec.section_name

        # Build student general section name mapping
        student_sec_names = {}
        for s in students:
            if s.section_id:
                student_sec_names[s.id] = sec_names.get(s.section_id, "A")
            else:
                student_sec_names[s.id] = "A"

        # Map section_id -> list of students taking that section
        section_students = {}
        for sec, course in sections_list:
            matching_students = []
            for s in students:
                s_sec_name = student_sec_names.get(s.id, "A")
                if (s.department_id == course.dept_id and 
                    s.semester == course.semester and 
                    s_sec_name == sec.section_name):
                    matching_students.append(s)
            section_students[sec.id] = matching_students

        # Load all timetable entries (active)
        tt_q = await db.execute(
            select(Timetable).where(Timetable.is_deleted.is_(False))
        )
        timetable_entries = tt_q.scalars().all()
        print(f"Found {len(timetable_entries)} timetable entries")

        total_inserted = 0
        current = START_DATE
        while current <= END_DATE:
            wd_num = current.weekday()
            wd = WEEKDAY_MAP.get(wd_num)

            if wd is None:
                print(f"  Skipping {current} (Sunday)")
                current += timedelta(days=1)
                continue

            print(f"Processing {current} ({wd.value})...")

            # Get timetable entries for this weekday
            day_entries = [t for t in timetable_entries if t.weekday == wd]

            # We can group timetable entries by section to assign Hour 1, Hour 2, Hour 3, Hour 4
            section_entries = {}
            for entry in day_entries:
                section_entries.setdefault(entry.section_id, []).append(entry)

            for sec_id, entries in section_entries.items():
                # Sort entries by start_time to assign sequential Hour 1, 2, 3...
                entries.sort(key=lambda x: x.start_time)
                
                sec_students = section_students.get(sec_id, [])
                if not sec_students:
                    continue

                for idx, entry in enumerate(entries):
                    hour_str = f"Hour {idx + 1}"

                    absentee_ids = []
                    od_ids = []

                    # Randomly distribute statuses for realism
                    for student in sec_students:
                        r = random.random()
                        if r < 0.10:  # 10% absent
                            absentee_ids.append(student.id)
                        elif r < 0.12:  # 2% OD (0.10 to 0.12)
                            od_ids.append(student.id)

                    att = Attendance(
                        section_id=entry.section_id,
                        subject_id=entry.subject_id,
                        faculty_id=entry.faculty_id,
                        date=current,
                        hour=hour_str,
                        absentee_ids=absentee_ids,
                        od_ids=od_ids
                    )
                    db.add(att)
                    total_inserted += 1

            await db.commit()
            print(f"  Committed for {current}")
            current += timedelta(days=1)

        print(f"\nDone! Inserted {total_inserted} scalable attendance records.")


if __name__ == "__main__":
    asyncio.run(seed())
