from datetime import date, datetime
from sqlalchemy.ext.asyncio import AsyncSession
from app.db.repositories.academic_repository import AcademicRepository
from app.db.repositories.leave_repository import LeaveRepository
from app.db.repositories.communication_repository import CommunicationRepository
from app.db.repositories.study_material_repository import StudyMaterialRepository
from app.db.models.leave import LeaveRequest, LeaveApproval, LeaveStatus
from app.db.models.grievance import Grievance
from app.db.models.communication import Notice
from app.db.models.study_material import StudyMaterial, Assignment

class AcademicService:
    def __init__(self, db: AsyncSession) -> None:
        self.acad_repo = AcademicRepository(db)
        self.leave_repo = LeaveRepository(db)
        self.comm_repo = CommunicationRepository(db)
        self.sm_repo = StudyMaterialRepository(db)

    async def get_student_timetable(self, section_id: str) -> list:
        return await self.acad_repo.get_timetable_by_section(section_id)

    async def get_faculty_timetable(self, faculty_id: str) -> list:
        return await self.acad_repo.get_timetable_by_faculty(faculty_id)

    async def get_all_leaves(self) -> list[LeaveRequest]:
        return await self.leave_repo.get_all_leaves()

    async def get_user_leaves(self, user_id: str) -> list[LeaveRequest]:
        return await self.leave_repo.get_leaves_by_user(user_id)

    async def apply_leave(
        self,
        user_id: str,
        app_category: str,
        type_val: str,
        from_date: date,
        to_date: date,
        reason: str,
        session_type: str | None = None,
        priority: str | None = None,
        photo_url: str | None = None,
        latitude: float | None = None,
        longitude: float | None = None,
        location_address: str | None = None,
        capture_time: datetime | None = None,
        verification_status: str | None = None,
        distance_from_campus: float | None = None,
        device_id: str | None = None,
        location_accuracy: float | None = None,
        geo_fence_status: str | None = None,
        device_network_info: str | None = None,
        metadata_: dict | None = None
    ) -> LeaveRequest:
        return await self.leave_repo.create_leave_request(
            user_id=user_id,
            app_category=app_category,
            type_val=type_val,
            session_type=session_type,
            priority=priority,
            from_date=from_date,
            to_date=to_date,
            reason=reason,
            photo_url=photo_url,
            latitude=latitude,
            longitude=longitude,
            location_address=location_address,
            capture_time=capture_time,
            verification_status=verification_status,
            distance_from_campus=distance_from_campus,
            device_id=device_id,
            location_accuracy=location_accuracy,
            geo_fence_status=geo_fence_status,
            device_network_info=device_network_info,
            metadata_=metadata_
        )

    async def process_leave_approval(self, leave_id: str, approved_by: str, status: LeaveStatus, remarks: str | None = None) -> LeaveApproval:
        return await self.leave_repo.approve_leave(leave_id, approved_by, status, remarks)

    async def get_user_grievances(self, user_id: str) -> list[Grievance]:
        return await self.comm_repo.get_grievances_by_user(user_id)

    async def get_all_grievances(self) -> list[Grievance]:
        return await self.comm_repo.get_all_grievances()

    async def raise_grievance(self, raised_by: str, category: str, description: str, subject: str = "General", priority: str = "Medium") -> Grievance:
        return await self.comm_repo.create_grievance(raised_by, category, description, subject=subject, priority=priority)

    async def update_grievance(self, grievance_id: str, status: str) -> None:
        await self.comm_repo.update_grievance_status(grievance_id, status)

    async def get_notices(self, audience_type: str) -> list[Notice]:
        return await self.comm_repo.get_notices_by_audience(audience_type)

    async def get_all_notices(self) -> list[Notice]:
        return await self.comm_repo.get_all_notices()

    async def create_notice(
        self,
        created_by: str,
        title: str,
        body: str,
        audience_type: str,
        publish_date: date,
        audience_types: list[str] | None = None,
        event_date: date | None = None,
        degree_id: str | None = None,
        batch_id: str | None = None,
        department_id: str | None = None,
        attachment_url: str | None = None,
        priority: str | None = None
    ) -> Notice:
        return await self.comm_repo.create_notice(
            created_by,
            title,
            body,
            audience_type,
            publish_date,
            audience_types=audience_types,
            event_date=event_date,
            degree_id=degree_id,
            batch_id=batch_id,
            department_id=department_id,
            attachment_url=attachment_url,
            priority=priority
        )

    async def get_materials_by_section(self, section_id: str) -> list[StudyMaterial]:
        return await self.sm_repo.get_materials_by_section(section_id)

    async def upload_material(self, section_id: str, faculty_id: str, title: str, type_val: str, file_url: str) -> StudyMaterial:
        return await self.sm_repo.create_study_material(section_id, faculty_id, title, type_val, file_url)

    async def verify_study_material(self, material_id: str, is_verified: bool) -> None:
        await self.sm_repo.verify_material(material_id, is_verified)

    async def get_assignments(self, section_id: str) -> list[Assignment]:
        return await self.sm_repo.get_assignments_by_section(section_id)

    async def create_assignment(self, section_id: str, faculty_id: str, title: str, deadline: date) -> Assignment:
        return await self.sm_repo.create_assignment(section_id, faculty_id, title, deadline)
