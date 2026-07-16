import asyncio
from app.db.session import AsyncSessionLocal
from app.db.models.academic import Degree, AcademicYear, Course
from app.db.models.student import Student
from sqlalchemy import select, func

async def main():
    async with AsyncSessionLocal() as db:
        deg_id='359e8b9a-2e39-4679-bc51-641f8da31a97'
        s_c = (await db.execute(select(func.count(Student.id)).where(Student.degree_id==deg_id, Student.is_deleted.is_(False)))).scalar()
        a_c = (await db.execute(select(func.count(AcademicYear.id)).where(AcademicYear.degree_id==deg_id, AcademicYear.is_deleted.is_(False)))).scalar()
        c_c = (await db.execute(select(func.count(Course.id)).where(Course.degree_id==deg_id, Course.is_deleted.is_(False)))).scalar()
        print(f'Students: {s_c}, AYs: {a_c}, Courses: {c_c}')

asyncio.run(main())
