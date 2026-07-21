# 06. API Documentation

Comprehensive API endpoint reference.

### Academic_calendar Endpoints
**File:** `backend/app/api/v1/endpoints/academic_calendar.py`

- `GET /setup`
- `POST /setup`
- `GET /events/conflicts`
- `GET /events`
- `POST /events`
- `PUT /events/{event_id}`
- `DELETE /events/{event_id}`
- `GET /draft/events`
- `POST /draft/events`
- `PUT /draft/events/{event_id}`
- `DELETE /draft/events/{event_id}`
- `POST /publish`
- `GET /history`
- `GET /published`

### Activity_points Endpoints
**File:** `backend/app/api/v1/endpoints/activity_points.py`

- `POST /upload-document`
- `GET /student`
- `POST /apply`
- `GET /faculty`
- `POST /review/{application_id}`
- `DELETE /{application_id}`
- `GET /categories`
- `POST /categories`
- `PUT /categories/{category_id}`
- `DELETE /categories/{category_id}`

### Assignments Endpoints
**File:** `backend/app/api/v1/endpoints/assignments.py`

- `GET /my-assignments`
- `POST /create`
- `PUT /{asg_id}`
- `DELETE /{asg_id}`
- `POST /{asg_id}/action`
- `GET /submissions`
- `POST /grade/{submission_id}`
- `GET /reports`
- `GET /active-assignments`
- `POST /{asg_id}/upload-submission`
- `POST /submit/{asg_id}`

### Attendance Endpoints
**File:** `backend/app/api/v1/endpoints/attendance.py`

- `GET /today`
- `POST /toggle`
- `POST /check-in`
- `POST /check-out`
- `GET /history/{faculty_id}`
- `GET /analytics`
- `GET /reports`
- `POST /change-status`

### Auth Endpoints
**File:** `backend/app/api/v1/endpoints/auth.py`

- `POST /login`
- `POST /refresh`
- `POST /logout`
- `GET /me`
- `PATCH /notification-preferences`
- `POST /change-password`
- `POST /request-email-change`
- `POST /confirm-email-change`
- `POST /forgot-password`
- `POST /reset-password`
- `GET /debug/users`
- `GET /debug/fix-student`
- `GET /debug/fix-thanush`
- `GET /debug/fix-arun`

### Budget Endpoints
**File:** `backend/app/api/v1/endpoints/budget.py`

- `GET /summary`
- `GET /line-items`
- `POST /line-items`
- `PUT /line-items/{item_id}`
- `DELETE /line-items/{item_id}`
- `POST /line-items/{item_id}/expenses`
- `GET /line-items/{item_id}/expenses`
- `GET /grants`
- `POST /grants`
- `PUT /grants/{grant_id}`
- `DELETE /grants/{grant_id}`

### Chatbot Endpoints
**File:** `backend/app/api/v1/endpoints/chatbot.py`

- `POST /message`
- `GET /history`
- `GET /session/{session_id}`
- `DELETE /session/{session_id}`

### Citations Endpoints
**File:** `backend/app/api/v1/endpoints/citations.py`

- `DELETE /{citation_id}`

### Classroom Endpoints
**File:** `backend/app/api/v1/endpoints/classroom.py`

- `GET /today-classes`
- `GET /resources`
- `POST /activities`
- `GET /activities`
- `POST /interactions`
- `GET /interactions`
- `POST /interactions/{interaction_id}/vote`
- `POST /session-summaries`
- `GET /session-summaries`
- `GET /reports`

### Class_advisor Endpoints
**File:** `backend/app/api/v1/endpoints/class_advisor.py`

- `GET /hod/classes`
- `POST /hod/assign`
- `GET /my-assignment`
- `GET /students`
- `GET /dashboard-stats`
- `GET /leaves`
- `POST /leaves/{leave_id}/action`
- `GET /class-timetable`

### Clubs Endpoints
**File:** `backend/app/api/v1/endpoints/clubs.py`

- `POST /{club_id}/join`
- `POST /{club_id}/leave`
- `GET /announcements`
- `POST /{club_id}/announcements`

### Community_service Endpoints
**File:** `backend/app/api/v1/endpoints/community_service.py`

- `GET /opportunities`
- `POST /opportunities/{opportunity_id}/apply`
- `POST /log-hours`
- `GET /logs`
- `DELETE /logs/{log_id}`
- `POST /upload-document`

### Dashboard Endpoints
**File:** `backend/app/api/v1/endpoints/dashboard.py`

- `GET /overview`

### Faculty Endpoints
**File:** `backend/app/api/v1/endpoints/faculty.py`

- `GET /dashboard`
- `GET /timetable`
- `GET /timetable/{user_id}`
- `POST /attendance/mark`
- `GET /attendance/dashboard-stats`
- `GET /attendance/sections`
- `GET /attendance/students`
- `POST /attendance/mark-bulk`
- `GET /attendance/records`
- `GET /attendance/corrections`
- `POST /attendance/correction/submit`
- `GET /attendance/correction-requests`
- `POST /attendance/correction-requests/{requestId}/approve`
- `POST /attendance/correction-requests/{requestId}/reject`
- `POST /attendance/send-warning`
- `GET /attendance/notifications`
- `GET /notifications`
- `POST /notifications/read/{notif_id}`
- `POST /notifications/read-all`
- `GET /attendance/audit-logs`
- `GET /students/list`
- `GET /departments/list`
- `GET /courses/list`
- `GET /leaves`
- `POST /leaves/apply`
- `GET /payroll`
- `POST /materials`
- `POST /assignments`
- `POST /research`
- `GET /notices`
- `GET /hod/dashboard`
- `GET /hod/workload`
- `GET /hod/leaves`
- `POST /hod/leaves/approve/{leave_id}`
- `GET /hod/materials`
- `POST /hod/materials/verify/{material_id}`
- `GET /profile`
- `PUT /profile`
- `GET /profile/update-requests/my`
- `POST /profile/update-requests`
- `GET /profile/update-requests/pending`
- `GET /profile/update-requests/history`
- `POST /profile/update-requests/{request_id}/approve`
- `POST /profile/update-requests/{request_id}/reject`
- `POST /profile/update-requests/{request_id}/request-changes`
- `GET /profile/activity-summary`
- `GET /profile/activity-summary/{user_id}`
- `GET /profile/{user_id}`
- `PUT /profile/{user_id}`
- `GET /hierarchy`
- `POST /documents/upload`
- `GET /research/list`
- `GET /research/list/{user_id}`
- `PUT /research/{research_id}`
- `DELETE /research/{research_id}`
- `GET /directory`
- `GET /hod/reports/department`
- `GET /hod/reports/faculty/{faculty_id}`
- `GET /hod/reports/students`
- `GET /hod/reports/export/department`
- `GET /hod/reports/export/students`
- `GET /substitutions/sync`
- `POST /hod/substitution/assign`
- `POST /substitutions/sync`
- `GET /hod/timetable`
- `POST /hod/timetable`
- `PUT /hod/timetable/{id}`
- `DELETE /hod/timetable/{id}`
- `GET /hod/timetable/metadata`
- `GET /hod/timetable/section/{section_id}`
- `POST /hod/timetable/submit`
- `GET /hod/attendance/monitoring`
- `GET /hod/timetable/subjects`
- `GET /hod/timetable/active-faculty`
- `GET /salary-requests`
- `POST /salary-requests`
- `GET /salary-requests/{request_id}/slip`
- `DELETE /salary-requests/{request_id}`
- `GET /hod/management/students`
- `POST /students/{student_id}/verify`
- `POST /students/{student_id}/review-edit-request`
- `POST /students/{student_id}/certifications/{cert_id}/verify`
- `GET /salary-slips`
- `GET /salary-slips/{salary_id}`
- `GET /hod/mentors`
- `POST /hod/mentor/assign`
- `GET /mentor/students`
- `GET /mentor/students/{student_id}/record`
- `POST /mentor/students/{student_id}/record`
- `POST /hod/faculty/reject/{user_id}`
- `GET /hod/substitution/available-faculty`
- `GET /hod/communication/students`
- `GET /hod/communication/announcements`
- `POST /hod/communication/announcements`
- `PUT /hod/communication/announcements/{announcement_id}`
- `DELETE /hod/communication/announcements/{announcement_id}`
- `POST /hod/communication/announcements/upload-image`
- `GET /principal/faculty-overview`

### Fees Endpoints
**File:** `backend/app/api/v1/endpoints/fees.py`

- `GET /`
- `POST /{record_id}/pay`
- `POST /{record_id}/create-order`
- `POST /{record_id}/verify-payment`
- `POST /webhook/razorpay`
- `GET /admin/all`
- `GET /admin/search-students`
- `GET /admin/student/{student_id}`
- `POST /admin/collect`
- `POST /admin/fee-structure`
- `GET /admin/fee-structures`
- `DELETE /admin/fee-structure/{structure_id}`
- `GET /admin/blueprints`
- `POST /admin/blueprints`
- `GET /admin/scholarship-types`
- `POST /admin/scholarship-types`
- `GET /admin/scholarship-types/list`

### Files Endpoints
**File:** `backend/app/api/v1/endpoints/files.py`

- `GET /{file_path:path}`

### Grievances Endpoints
**File:** `backend/app/api/v1/endpoints/grievances.py`

- `GET /`
- `POST /`
- `PATCH /{grievance_id}`

### Hall_tickets Endpoints
**File:** `backend/app/api/v1/endpoints/hall_tickets.py`

- No standard router definitions found or uses dynamic routing.


### Hostel Endpoints
**File:** `backend/app/api/v1/endpoints/hostel.py`

- `GET /blocks`
- `POST /blocks`
- `PUT /blocks/{block_id}`
- `DELETE /blocks/{block_id}`
- `GET /rooms`
- `POST /rooms`
- `DELETE /rooms/{room_id}`
- `GET /allocations`
- `POST /allocations`
- `POST /allocations/{allocation_id}/vacate`

### Innovation_wall Endpoints
**File:** `backend/app/api/v1/endpoints/innovation_wall.py`

- `GET /projects`
- `POST /projects`
- `POST /projects/{project_id}/like`
- `POST /projects/{project_id}/comments`
- `DELETE /projects/{project_id}`

### Internship_drives Endpoints
**File:** `backend/app/api/v1/endpoints/internship_drives.py`

- `POST /apply`
- `GET /applications`
- `PATCH /applications/{application_id}`
- `GET /partners`
- `POST /partners`
- `PUT /partners/{partner_id}`
- `DELETE /partners/{partner_id}`

### Inventory Endpoints
**File:** `backend/app/api/v1/endpoints/inventory.py`

- `GET /items`
- `POST /items`
- `PUT /items/{item_id}`
- `DELETE /items/{item_id}`
- `POST /items/{item_id}/movement`
- `GET /transactions`

### Leave Endpoints
**File:** `backend/app/api/v1/endpoints/leave.py`

- `GET /balances`
- `GET /history`
- `POST /apply`
- `GET /hod/pending`
- `POST /hod/approve/{leave_id}`
- `GET /advisor/students`
- `POST /advisor/approve/{leave_id}`
- `DELETE /{leave_id}`

### Legal_events Endpoints
**File:** `backend/app/api/v1/endpoints/legal_events.py`

- `GET /registrations`
- `POST /registrations`
- `PUT /registrations`
- `GET /questions`
- `POST /questions`
- `POST /faculty`
- `GET /pending`
- `PATCH /{event_id}/approve`
- `PATCH /{event_id}/reject`

### Library Endpoints
**File:** `backend/app/api/v1/endpoints/library.py`

- `GET /books`
- `POST /books`
- `PUT /books/{book_id}`
- `DELETE /books/{book_id}`
- `GET /issues`
- `POST /issues`
- `POST /issues/{issue_id}/return`

### Maintenance Endpoints
**File:** `backend/app/api/v1/endpoints/maintenance.py`

- `GET /test_connection`

### Marks Endpoints
**File:** `backend/app/api/v1/endpoints/marks.py`

- `GET /internal`
- `POST /internal`
- `POST /internal/submit`
- `GET /internal/hod/pending`
- `POST /internal/hod/approve`
- `POST /internal/hod/message`
- `POST /internal/faculty/message`
- `GET /internal/student/me`
- `GET /internal/student/me/export-pdf`

### Messages Endpoints
**File:** `backend/app/api/v1/endpoints/messages.py`

- `GET /contacts`
- `GET /conversations`
- `GET /thread/{user_id}`
- `POST /send`
- `POST /read/{user_id}`

### Moot_court Endpoints
**File:** `backend/app/api/v1/endpoints/moot_court.py`

- `GET /memorials`
- `POST /memorials`
- `PUT /memorials/{memorial_id}`
- `DELETE /memorials/{memorial_id}`

### Notices Endpoints
**File:** `backend/app/api/v1/endpoints/notices.py`

- `GET /`
- `GET /received`
- `POST /`
- `DELETE /{notice_id}`
- `POST /{notice_id}/read`
- `POST /{notice_id}/acknowledge`
- `POST /{notice_id}/archive`

### Notifications Endpoints
**File:** `backend/app/api/v1/endpoints/notifications.py`

- `GET /`
- `GET /unread-count`
- `POST /device-token`
- `DELETE /device-token`
- `POST /{notification_id}/read`
- `POST /read-all`
- `DELETE /{notification_id}`
- `DELETE /delete-all/read`

### Online_meetings Endpoints
**File:** `backend/app/api/v1/endpoints/online_meetings.py`

- `POST /create`
- `DELETE /{meeting_id}`
- `GET /recordings`
- `POST /recordings`
- `POST /recordings/create`
- `DELETE /recordings/{recording_id}`

### Parents Endpoints
**File:** `backend/app/api/v1/endpoints/parents.py`

- No standard router definitions found or uses dynamic routing.


### Payroll Endpoints
**File:** `backend/app/api/v1/endpoints/payroll.py`

- No standard router definitions found or uses dynamic routing.


### Pf Endpoints
**File:** `backend/app/api/v1/endpoints/pf.py`

- `POST /config`
- `GET /config/{faculty_id}`
- `POST /historical`
- `GET /historical/{faculty_id}`
- `POST /claims`
- `GET /claims/{faculty_id}`
- `GET /statement/{faculty_id}`
- `GET /dashboard/{faculty_id}`
- `GET /audit-logs`
- `POST /leave-exclusions`
- `GET /leave-exclusions/{faculty_id}`
- `DELETE /leave-exclusions/{exclusion_id}`

### Reports Endpoints
**File:** `backend/app/api/v1/endpoints/reports.py`

- `GET /student/{user_id}/pdf`
- `GET /faculty/{user_id}/pdf`
- `GET /attendance/pdf`
- `GET /marks/pdf`
- `GET /salary/pdf`
- `GET /fees/pdf`

### Research_endpoints Endpoints
**File:** `backend/app/api/v1/endpoints/research_endpoints.py`

- `POST /plan`
- `GET /plans`
- `POST /progress/{plan_id}`
- `GET /progress/{plan_id}`
- `POST /proof/{plan_id}`
- `GET /hod/monitoring`
- `GET /hod/pending-proofs`
- `POST /hod/verify/{proof_id}`
- `GET /principal/compliance`
- `POST /cron/check-deadlines`
- `DELETE /plan/{plan_id}`

### Students Endpoints
**File:** `backend/app/api/v1/endpoints/students.py`

- `GET /mentorship-record`
- `GET /lexnova/stats`
- `GET /dashboard`
- `GET /semester-timeline`
- `GET /profile`
- `GET /profile/export-pdf`
- `PUT /profile`
- `POST /profile/photo`
- `POST /leaves/upload`
- `GET /attendance`
- `GET /attendance/export-pdf`
- `GET /marks`
- `GET /fees`
- `POST /fees/pay/{record_id}`
- `GET /fees/receipts`
- `GET /fees/receipts/{payment_id}/download`
- `GET /fees/loan`
- `PUT /fees/loan`
- `GET /fees/assistance-requests`
- `POST /fees/assistance-requests`
- `GET /certifications`
- `POST /certifications`
- `DELETE /certifications/{certification_id}`
- `GET /timetable`
- `GET /courses`
- `GET /leaves`
- `POST /leaves/apply`
- `GET /grievances`
- `POST /grievances/raise`
- `GET /study-materials`
- `GET /notices`
- `GET /notifications`
- `POST /notifications/read`
- `POST /notifications/{notification_id}/read`
- `DELETE /notifications/{notification_id}`
- `GET /parent/child/dashboard`
- `GET /parent/notices`
- `GET /parent/college-info`
- `POST /parent/inquiries`
- `GET /parent/children`
- `GET /parent/child/profile`
- `GET /parent/child/attendance`
- `GET /parent/child/marks`
- `GET /parent/child/subject-attendance`
- `GET /parent/child/performance`
- `GET /parent/child/fees`
- `POST /parent/child/fees/{record_id}/create-order`
- `POST /parent/child/fees/{record_id}/verify-payment`
- `GET /parent/child/fees/export-pdf`
- `GET /parent/child/timetable`
- `GET /parent/child/courses`
- `GET /parent/child/internal-marks`
- `GET /parent/child/marks/export-pdf`
- `GET /papers`
- `POST /papers`
- `POST /papers/upload`
- `GET /council`
- `POST /council/proposals`
- `POST /council/feedback`
- `POST /council/feedback/{feedback_id}/upvote`
- `POST /profile/submit`
- `POST /profile/document`
- `POST /profile/request-edit`
- `GET /temp-debug-users`

### Study_materials Endpoints
**File:** `backend/app/api/v1/endpoints/study_materials.py`

- `GET /my-materials`
- `POST /upload`
- `POST /edit/{material_id}`
- `POST /archive/{material_id}`
- `POST /restore-version/{material_id}/{ver_num}`
- `GET /principal/pending`
- `POST /principal/review/{material_id}`
- `GET /hod/pending`
- `POST /hod/review/{material_id}`
- `GET /student/approved`
- `GET /notifications`
- `POST /notifications/read/{notif_id}`
- `GET /audit-logs`
- `GET /reports`
- `POST /upload-file`
- `GET /student/bookmarks`
- `POST /student/bookmarks/toggle/{material_id}`
- `GET /student/favorites`
- `POST /student/favorites/toggle/{material_id}`
- `POST /student/download/{material_id}`
- `GET /student/downloads`
- `GET /student/notifications`
- `POST /student/notifications/read/{notif_id}`
- `GET /student/analytics`

### Subject_allocation Endpoints
**File:** `backend/app/api/v1/endpoints/subject_allocation.py`

- `GET /setup`
- `GET /course-sections`
- `GET /subjects`
- `GET /faculty`
- `POST /allocate`
- `GET /allocations`
- `GET /history`
- `GET /my-subjects`

### Teaching_logs Endpoints
**File:** `backend/app/api/v1/endpoints/teaching_logs.py`

- `GET /dashboard`
- `GET /today-classes`
- `GET /attendance-summary/{section_id}/{subject_id}`
- `POST /diaries`
- `GET /diaries`
- `PUT /diaries/{id}`
- `POST /diaries/{id}/review`
- `GET /syllabus-progress`
- `GET /lesson-plans`
- `POST /lesson-plans`
- `GET /lesson-plan-tracking`
- `POST /activities`
- `GET /activities`
- `GET /pending-entries`
- `GET /notifications`
- `POST /notifications/read/{id}`
- `GET /audit-logs`
- `POST /upload-file`
- `GET /hod/dashboard`
- `GET /principal/dashboard`
- `GET /hod/syllabus/metadata`
- `GET /hod/syllabus/courses`
- `GET /hod/syllabus/courses/{course_name}/plan`
- `POST /hod/syllabus/courses/{course_name}/plan`

### Timetable Endpoints
**File:** `backend/app/api/v1/endpoints/timetable.py`

- No standard router definitions found or uses dynamic routing.


### Transport Endpoints
**File:** `backend/app/api/v1/endpoints/transport.py`

- `GET /routes`
- `POST /routes`
- `PUT /routes/{route_id}`
- `DELETE /routes/{route_id}`
- `GET /vehicles`
- `POST /vehicles`
- `PUT /vehicles/{vehicle_id}`
- `DELETE /vehicles/{vehicle_id}`
- `GET /passes`
- `POST /passes`
- `POST /passes/{pass_id}/cancel`

### Users Endpoints
**File:** `backend/app/api/v1/endpoints/users.py`

- `GET /dashboard`
- `POST /create`
- `GET /list`
- `GET /details/{user_id}`
- `POST /update/{user_id}`
- `POST /status/{user_id}`
- `GET /faculty`
- `DELETE /delete/{user_id}`
- `POST /payroll/run`
- `POST /fees/structure`
- `GET /principal/dashboard`
- `GET /principal/attendance-summary`
- `GET /principal/timetable/approvals`
- `POST /hod/timetable/request-change/{section_id}`
- `POST /principal/timetable/approve/{section_id}`
- `GET /principal/leaves`
- `POST /principal/leaves/approve/{leave_id}`
- `GET /principal/grievances`
- `POST /principal/grievances/resolve/{grievance_id}`
- `GET /principal/circulars`
- `POST /principal/circulars`
- `DELETE /delete/{user_id}`
- `POST /departments/create`
- `GET /departments/list`
- `DELETE /departments/delete/{dept_id}`
- `POST /degrees/create`
- `PUT /degrees/update/{degree_id}`
- `GET /degrees/list`
- `DELETE /degrees/delete/{degree_id}`
- `POST /courses/create`
- `GET /courses/by-degree/{degree_id}`
- `POST /courses/copy`
- `PUT /courses/update/{course_id}`
- `DELETE /courses/delete/{course_id}`
- `GET /system-settings`
- `POST /system-settings`
- `GET /system-settings/history`
- `GET /academic-years/list`
- `POST /academic-years/initialize`
- `PUT /academic-years/update/{ay_id}`
- `DELETE /academic-years/delete/{ay_id}`
- `POST /academic-years/set-semester`
- `GET /students/search`
- `GET /students/fees/{student_id}`
- `GET /fees/tracker-data`
- `POST /fees/collect`
- `GET /payments/daily`
- `POST /backups/create`
- `GET /backups/history`
- `GET /backups/download/{backup_id}`
- `POST /backups/restore/{backup_id}`
- `POST /backups/restore-upload`
- `DELETE /backups/{backup_id}`
- `GET /backups/settings`
- `POST /backups/settings`
- `GET /backups/widget`
- `GET /backups/audit-logs`
- `GET /principal/faculty/pending`
- `POST /principal/faculty/approve/{user_id}`
- `POST /principal/faculty/reject/{user_id}`
- `GET /hod/faculty/pending`
- `GET /hod/courses`
- `POST /hod/faculty/approve/{user_id}`
- `GET /salary-requests`
- `POST /salary-requests/{request_id}/approve`
- `POST /salary-requests/{request_id}/reject`
- `GET /salary-requests/{request_id}/slip`
- `GET /payroll/preview`
- `GET /salary-slips`
- `POST /salary-slips`
- `PUT /salary-slips/{salary_id}`
- `POST /salary-slips/generate-bulk`
- `POST /salary-slips/generate-historical/{faculty_id}`
- `DELETE /salary-slips/clear-filtered`
- `DELETE /salary-slips/{salary_id}`
- `GET /working-day-config`
- `POST /working-day-config`
- `GET /working-day-config/all`
- `GET /infrastructure`
- `POST /infrastructure`
- `GET /attendance-defaulters`
- `PATCH /attendance-defaulters/{student_id}/pay`
- `PATCH /attendance-defaulters/{student_id}/adjust`

