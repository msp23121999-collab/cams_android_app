# CAMS Enterprise
## 04. Portal Documentation

The application surfaces specialized Portals based on the authenticated user's Role.

### Student Portal
- **Overview:** The primary interface for academic consumption.
- **Modules:** Timetable, Attendance, Assignments, Exams, Noticeboard.
- **Workflow:** Students log in, view their daily timetable, join virtual classes or verify physical attendance, and complete assignments.
- **Backend APIs:** `/api/v1/student/*`, `/api/v1/attendance/*`

### Parent Portal
- **Overview:** A read-only interface dedicated to monitoring wards.
- **Modules:** Linked Student Selector, Attendance Tracker, Report Cards.
- **Workflow:** Select ward, view current attendance standing, check recent grades.

### Faculty Portal
- **Overview:** The workspace for teachers and professors.
- **Modules:** Class Selector, Attendance Marker, Assignment Creator, Grading System.
- **Workflow:** Open assigned class, mark attendance bulk, evaluate submitted assignments.

### HOD Portal
- **Overview:** Administrative view scoped to a specific department.
- **Modules:** Faculty Roster, Leave Approvals, Substitution Matrix.
- **Workflow:** Review pending leave requests from faculty, assign substitute teachers, review class completion metrics.

### Principal Portal
- **Overview:** Executive dashboard.
- **Modules:** Institution Analytics, Grievance Redressal, Compliance Reports.

### Admin Portal
- **Overview:** System maintenance.
- **Modules:** User Directory, Backup Management, Audit Trail.
