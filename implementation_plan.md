# Implementation Plan: Complete Enterprise Backend Integration

## 1. Authentication & Lifecycle Sync
- **Login Flow**: Update `com.example.core.network.LoginRequest` and `LoginResponse` DTOs to match FastAPI schemas.
  - `LoginResponse` will include `access_token`, `role`, `subdomain_target`, and `refresh_token`.
- **Token Management**: Enhance `AuthManager` to handle `refresh_token` and use `EncryptedSharedPreferences` for secure storage.
- **AuthRepository**: Update `OfflineFirstAuthRepository.login` to process the new response and save both tokens and role.
- **Role-Based Navigation**: In `AuthViewModel`, handle success by navigating to the specific portal based on the `subdomain_target` or `role`.
- **Logout Flow**: Implement a full cleanup in `AuthManager` and navigate back to the Role Selection screen.

## 2. Portal & Module Wiring (31+ Modules)
We will map the existing Android UI to the following FastAPI endpoints:

### Student Portal (`/students/*`)
- **Dashboard**: `GET /students/dashboard` (Metrics, upcoming events).
- **Profile**: `GET /students/profile` & `PUT /students/profile`.
- **Attendance**: `GET /students/attendance` (Summary & records).
- **Marks**: `GET /students/marks`.
- **Fees**: `GET /students/fees` & `POST /students/fees/pay/{record_id}`.
- **Timetable**: `GET /students/timetable`.
- **Study Materials**: `GET /students/study-materials`.
- **Assignments**: `GET /students/assignments`.
- **Leaves**: `GET /students/leaves` & `POST /students/leaves/apply`.
- **Notices**: `GET /students/notices`.

### Parent Portal (`/students/parent/*`)
- **Child Dashboard**: `GET /students/parent/child/dashboard`.
- **Child Profile**: `GET /students/parent/child/profile`.
- **Child Attendance**: `GET /students/parent/child/attendance`.
- **Child Marks**: `GET /students/parent/child/marks`.
- **Child Fees**: `GET /students/parent/child/fees`.

### Faculty & HOD Portal (`/faculty/*`)
- **Dashboard**: `GET /faculty/dashboard`.
- **Attendance Marking**: `POST /faculty/attendance/mark-bulk`.
- **Timetable**: `GET /faculty/timetable`.
- **Payroll**: `GET /faculty/payroll`.
- **HOD Dashboard**: `GET /faculty/hod/dashboard`.
- **HOD Timetable Management**: `GET /faculty/hod/timetable`.

### Admin & Principal Portal (`/dashboard/*`, `/users/*`)
- **Admin Metrics**: `GET /dashboard/metrics`.
- **User Management**: `GET /users/`.
- **Approvals**: `GET /faculty/hod/leaves` (HOD) and `GET /principal/approvals/*` (Principal).

## 3. Data Integrity & Stability
- **Moshi DTOs**: Create/Update DTOs in `com.example.core.network` using `@Json(name = "snake_case")` to match backend payloads.
- **State Management**: viewModels will use `StateFlow` with `Loading`, `Success`, and `Error` states.
- **Global Error Handling**: Use `NetworkErrorInterceptor` to emit events for connection issues or server errors.

## 4. Visual Polish: Font Correction
- **Typography.kt**: Update `com.example.core.theme.AppTheme` to include a custom `Typography` object.
- **Force Visibility**: Set `color = Color.Black` for light theme and crisp white/slate for dark theme.
- **Emphasis**: Use `FontWeight.SemiBold` for titles and labels to eliminate "blurry" text.

I will proceed with these changes upon your approval.
