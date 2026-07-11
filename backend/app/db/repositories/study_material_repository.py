from datetime import date
from sqlalchemy import select, update
from sqlalchemy.ext.asyncio import AsyncSession
from app.db.models.study_material import StudyMaterial, Assignment

class StudyMaterialRepository:
    def __init__(self, db: AsyncSession) -> None:
        self.db = db

    async def get_materials_by_section(self, section_id: str) -> list[StudyMaterial]:
        result = await self.db.execute(
            select(StudyMaterial).where(StudyMaterial.section_id == section_id, StudyMaterial.is_deleted.is_(False))
        )
        return list(result.scalars().all())

    async def get_all_materials(self) -> list[StudyMaterial]:
        result = await self.db.execute(
            select(StudyMaterial).where(StudyMaterial.is_deleted.is_(False))
        )
        return list(result.scalars().all())

    async def create_study_material(self, section_id: str, faculty_id: str, title: str, type_val: str, file_url: str) -> StudyMaterial:
        sm = StudyMaterial(
            section_id=section_id,
            faculty_id=faculty_id,
            title=title,
            type=type_val,
            file_url=file_url,
            is_verified=False
        )
        self.db.add(sm)
        await self.db.flush()
        return sm

    async def verify_material(self, material_id: str, is_verified: bool) -> None:
        await self.db.execute(
            update(StudyMaterial)
            .where(StudyMaterial.id == material_id)
            .values(is_verified=is_verified)
        )
        await self.db.flush()

    async def get_assignments_by_section(self, section_id: str) -> list[Assignment]:
        result = await self.db.execute(
            select(Assignment).where(Assignment.section_id == section_id, Assignment.is_deleted.is_(False))
        )
        return list(result.scalars().all())

    async def get_all_assignments(self) -> list[Assignment]:
        result = await self.db.execute(
            select(Assignment).where(Assignment.is_deleted.is_(False))
        )
        return list(result.scalars().all())

    async def create_assignment(self, section_id: str, faculty_id: str, title: str, deadline: date) -> Assignment:
        assign = Assignment(
            section_id=section_id,
            faculty_id=faculty_id,
            title=title,
            deadline=deadline,
            submission_count=0
        )
        self.db.add(assign)
        await self.db.flush()
        return assign

    async def increment_submission(self, assignment_id: str) -> None:
        await self.db.execute(
            update(Assignment)
            .where(Assignment.id == assignment_id)
            .values(submission_count=Assignment.submission_count + 1)
        )
        await self.db.flush()
