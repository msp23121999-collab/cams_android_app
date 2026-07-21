# CAMS Enterprise
## 03. Role Management

CAMS Enterprise utilizes strict Role-Based Access Control (RBAC). A user's experience is fundamentally altered based on their role.

### 1. Student
- **Purpose:** Primary end-user of the academic features.
- **Responsibilities:** Attend classes, submit assignments, view academic records.
- **Permissions:** Read-only for curriculum and attendance, write for submissions and leave applications.
- **Authentication:** JWT linked to `Student` profile.
- **Accessible Modules:** Dashboard, Attendance, Assignments, Results, Chat.

### 2. Parent
- **Purpose:** Monitor student progress and compliance.
- **Responsibilities:** Review attendance, performance, and faculty feedback.
- **Permissions:** Read-only access strictly linked to specific enrolled student(s) via relational mapping.
- **Accessible Modules:** Student Performance, Attendance records, Timetable.

### 3. Faculty
- **Purpose:** Manage day-to-day academic and classroom activities.
- **Responsibilities:** Mark attendance, create/evaluate assignments, submit grades, handle smart classroom IoT if applicable.
- **Permissions:** Write access scoped to courses and classes they are officially assigned to.
- **Accessible Modules:** Smart Classroom, Attendance Entry, Assignment Grading, Internal Marks.

### 4. HOD (Head of Department)
- **Purpose:** Oversee departmental academic and administrative operations.
- **Responsibilities:** Approve faculty leave requests, monitor departmental KPIs, faculty substitution.
- **Permissions:** Department-wide read/write access.
- **Accessible Modules:** Approvals, Department Analytics, Faculty Management, Syllabus Tracking.

### 5. Principal
- **Purpose:** Executive oversight of the entire institution.
- **Responsibilities:** Review institutional analytics, grant final approvals for major events.
- **Permissions:** Institution-wide comprehensive read access, high-level administrative write access.
- **Accessible Modules:** Institution Dashboard, Consolidated Reports, High-level Analytics.

### 6. Admin
- **Purpose:** Complete system administration and maintenance.
- **Responsibilities:** User management, role assignment, system configuration, backup scheduling.
- **Permissions:** Superuser bypass for administrative tasks.
- **Accessible Modules:** System Settings, User Management, Audit Logs, Backups, Database Management.
