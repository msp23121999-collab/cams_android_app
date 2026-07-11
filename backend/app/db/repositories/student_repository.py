from sqlalchemy import select, update
from sqlalchemy.ext.asyncio import AsyncSession
from app.db.models.user import User, UserRole
from app.db.models.student import Student, ParentStudentMap

class StudentRepository:
    def __init__(self, db: AsyncSession) -> None:
        self.db = db

    async def get_all_users(self, skip: int = 0, limit: int = 100) -> list[User]:
        from sqlalchemy.orm import joinedload
        result = await self.db.execute(
            select(User)
            .options(joinedload(User.department))
            .where(User.is_deleted.is_(False))
            .offset(skip)
            .limit(limit)
        )
        return list(result.scalars().all())

    async def get_student_by_user_id(self, user_id: str) -> Student | None:
        result = await self.db.execute(
            select(Student).where(Student.user_id == user_id, Student.is_deleted.is_(False))
        )
        return result.scalar_one_or_none()

    async def get_student_by_id(self, student_id: str) -> Student | None:
        result = await self.db.execute(
            select(Student).where(Student.id == student_id, Student.is_deleted.is_(False))
        )
        return result.scalar_one_or_none()

    async def get_parent_student_map(self, parent_id: str) -> list[ParentStudentMap]:
        result = await self.db.execute(
            select(ParentStudentMap).where(ParentStudentMap.parent_id == parent_id, ParentStudentMap.is_deleted.is_(False))
        )
        return list(result.scalars().all())

    async def create_user(self, user_data: dict) -> User:
        user = User(**user_data)
        self.db.add(user)
        await self.db.flush()
        return user

    async def create_student(self, student_data: dict) -> Student:
        student = Student(**student_data)
        self.db.add(student)
        await self.db.flush()
        return student

    async def create_parent_student_map(self, parent_id: str, student_id: str) -> ParentStudentMap:
        mapping = ParentStudentMap(parent_id=parent_id, student_id=student_id)
        self.db.add(mapping)
        await self.db.flush()
        return mapping
