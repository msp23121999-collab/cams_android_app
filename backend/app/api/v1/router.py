from fastapi import APIRouter

from app.api.v1.endpoints import academic_calendar
from app.api.v1.endpoints import activity_points
from app.api.v1.endpoints import community_service
from app.api.v1.endpoints import innovation_wall
from app.api.v1.endpoints import assignments
from app.api.v1.endpoints import attendance
from app.api.v1.endpoints import auth
from app.api.v1.endpoints import classroom
from app.api.v1.endpoints import messages
from app.api.v1.endpoints import dashboard
from app.api.v1.endpoints import faculty
from app.api.v1.endpoints import leave
from app.api.v1.endpoints import maintenance
from app.api.v1.endpoints import online_meetings
from app.api.v1.endpoints import pf
from app.api.v1.endpoints import research_endpoints
from app.api.v1.endpoints import students
from app.api.v1.endpoints import study_materials
from app.api.v1.endpoints import teaching_logs
from app.api.v1.endpoints import users
from app.api.v1.endpoints import chatbot
from app.api.v1.endpoints import legal_events
from app.api.v1.endpoints import internship_drives
from app.api.v1.endpoints import clubs
from app.api.v1.endpoints import files
from app.api.v1.endpoints import moot_court
from app.api.v1.endpoints import citations
from app.api.v1.endpoints.hall_tickets import student_router as hall_tickets_student_router, admin_router as hall_tickets_admin_router
from app.api.v1.endpoints.notices import router as notices_router
from app.api.v1.endpoints.subject_allocation import router as subject_allocation_router
from app.api.v1.endpoints.marks import router as marks_router
from app.api.v1.endpoints.fees import router as fees_router
from app.api.v1.endpoints.grievances import router as grievances_router
from app.api.v1.endpoints.reports import router as reports_router
from app.api.v1.endpoints.class_advisor import router as class_advisor_router
from app.api.v1.endpoints.notifications import router as notifications_router
from app.api.v1.endpoints.hostel import router as hostel_router
from app.api.v1.endpoints.inventory import router as inventory_router
from app.api.v1.endpoints.library import router as library_router
from app.api.v1.endpoints.transport import router as transport_router
from app.api.v1.endpoints.budget import router as budget_router
api_router = APIRouter()

api_router.include_router(notifications_router, prefix="/notifications", tags=["notifications"])

# ERP modules (Admin/Principal)
api_router.include_router(hostel_router, prefix="/hostel", tags=["hostel"])
api_router.include_router(inventory_router, prefix="/inventory", tags=["inventory"])
api_router.include_router(library_router, prefix="/library", tags=["library"])
api_router.include_router(transport_router, prefix="/transport", tags=["transport"])
api_router.include_router(budget_router, prefix="/budget", tags=["budget"])

api_router.include_router(class_advisor_router, prefix="/class-advisor", tags=["class-advisor"])
api_router.include_router(maintenance.router, tags=["maintenance"])
api_router.include_router(notices_router, prefix="/notices", tags=["notices"])
api_router.include_router(subject_allocation_router, prefix="/subject-allocations", tags=["subject_allocations"])
api_router.include_router(auth.router, prefix="/auth", tags=["auth"])
api_router.include_router(students.router, prefix="/students", tags=["students"])
api_router.include_router(faculty.router, prefix="/faculty", tags=["faculty"])
api_router.include_router(users.router, prefix="/users", tags=["users"])
api_router.include_router(pf.router, prefix="/pf", tags=["pf"])
api_router.include_router(attendance.router, prefix="/staff/attendance", tags=["staff-attendance"])
api_router.include_router(teaching_logs.router, prefix="/teaching-logs", tags=["teaching-logs"])
api_router.include_router(study_materials.router, prefix="/study-materials", tags=["study-materials"])
api_router.include_router(assignments.router, prefix="/assignments", tags=["assignments"])
api_router.include_router(classroom.router, prefix="/smart-classroom", tags=["smart-classroom"])
api_router.include_router(leave.router, prefix="/leave", tags=["leave"])
api_router.include_router(research_endpoints.router, prefix="/research-plan", tags=["research-publication-tracking"])
api_router.include_router(dashboard.router, prefix="/dashboard", tags=["dashboard"])
api_router.include_router(online_meetings.router, prefix="/online-meetings", tags=["online-meetings"])
api_router.include_router(internship_drives.router, prefix="/internship-drives", tags=["internship-drives"])
api_router.include_router(activity_points.router, prefix="/activity-points", tags=["activity-points"])
api_router.include_router(community_service.router, prefix="/community-service", tags=["community-service"])
api_router.include_router(innovation_wall.router, prefix="/innovation-wall", tags=["innovation-wall"])
api_router.include_router(academic_calendar.router, prefix="/academic-calendar", tags=["academic-calendar"])
api_router.include_router(chatbot.router, prefix="/chatbot", tags=["chatbot"])
api_router.include_router(marks_router, prefix="/marks", tags=["marks"])
api_router.include_router(legal_events.router, prefix="/legal-events", tags=["legal-events"])
api_router.include_router(fees_router, prefix="/fees", tags=["fees"])
api_router.include_router(grievances_router, prefix="/grievances", tags=["grievances"])
api_router.include_router(reports_router, prefix="/reports", tags=["reports"])
api_router.include_router(clubs.router, prefix="/clubs", tags=["clubs"])
api_router.include_router(files.router, prefix="/files", tags=["files"])
api_router.include_router(messages.router, prefix="/messages", tags=["messages"])
api_router.include_router(hall_tickets_student_router, prefix="/students/hall-tickets", tags=["hall-tickets"])
api_router.include_router(moot_court.router, prefix="/students/moot-court", tags=["moot-court"])
api_router.include_router(citations.router, prefix="/students/citations", tags=["citations"])
api_router.include_router(hall_tickets_admin_router, prefix="/admin/hall-tickets", tags=["hall-tickets-admin"])
