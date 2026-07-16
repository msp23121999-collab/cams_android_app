# Implementation Plan: Student Portal Synchronization

This plan outlines the steps to achieve 1-to-1 functional synchronization between the Android Student Portal and the FastAPI backend/React web app.

## 1. API Synchronization (Retrofit)
Update `CamsApiService.kt` with the following endpoint mappings:

- **Dashboard**: `GET /students/dashboard` (Returns metrics)
- **Profile**: `GET /students/profile` (Detailed profile info)
- **Attendance**: `GET /students/attendance` (Summary + Records)
- **Marks**: `GET /students/marks` (Exam marks)
- **Fees**: `GET /students/fees` (Summary + Records)
- **Timetable**: `GET /students/timetable` (Schedule slots)
- **Assignments**: `GET /assignments/active-assignments` (Active tasks)
- **Study Materials**: `GET /students/study-materials` (Course resources)
- **Leaves**: `GET /students/leaves` and `POST /students/leaves/apply`
- **Grievances**: `GET /students/grievances` and `POST /students/grievances/raise`
- **Notices**: `GET /students/notices` (Circulars)
- **LexNova**: `GET /students/lexnova/stats` (KPIs)
- **Activity Points**: `GET /students/activity-points`

## 2. Data Model Alignment (Moshi DTOs)
Ensure Kotlin DTOs in `CamsApiService.kt` perfectly match Backend schemas:
- Map `snake_case` fields (e.g., `date_of_birth`, `total_fees`, `pending_balance`) to `camelCase` using `@Json(name = "...")`.
- Ensure optional fields are handled correctly with nullable types.

## 3. ViewModel Development (Student Portal)
Create or update ViewModels for every student screen to handle:
- **StateFlow**: Manage UI state (Loading, Success, Error).
- **Error Handling**: Graceful network error management.
- **Data Transformation**: Convert DTOs to UI-friendly models.

Target ViewModels:
- `StudentDashboardViewModel`
- `StudentProfileViewModel`
- `AttendanceViewModel`
- `MarksViewModel`
- `FeesViewModel`
- `TimetableViewModel`
- `AssignmentsViewModel`
- `LeavesViewModel`
- `GrievancesViewModel`
- `StudyMaterialsViewModel`
- `LexNovaViewModel`
- `ActivityPointsViewModel`

## 4. UI Data Binding & Font Correction
- **Data Binding**: Connect existing Jetpack Compose screens to the new ViewModels.
- **Font Correction**: Ensure `AppTypography` is applied to all Text components.
- **Accessibility**: Verify touch targets and content descriptions.

## 5. Verification
- `compile_applet` to ensure everything builds correctly.
- Functional testing of each module.
