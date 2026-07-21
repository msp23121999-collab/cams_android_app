# CAMS Enterprise — Project Context

## Repos
- This repository is self-contained: Android app, backend and database live here.
  - Android app (Kotlin/Compose): `app/src/main/java/com/example/`
  - Backend (FastAPI):            `backend/`
  - Database reference:           `database/`  (live migrations: `backend/alembic/`)
- The CAMS **web app** is a REFERENCE ONLY for schema and API contract. It is never
  copied into this repository and is blocked by .gitignore.

## Hard rules — apply to every task
- NEVER change existing UI design/layout/colors — only fix alignment/spacing bugs
- Treat the web app's schema and API contract as source of truth
- Never hardcode secrets — always env vars
- Payment gateway: Razorpay — server-side signature verification always, never trust client callback alone
- Before deleting any file, list it and ask first
- Role-based access enforced on every backend endpoint

## Current status (update as we go)
- Login/Role Selection/Session: [not started / in progress / done]
- Student/Parent portal screens: [not started]
- Backend Postgres migration (from JSON files): [not started]
---

## Production Audit Progress Tracker
(Update after every session — this is how work resumes across sessions)

### Stage 0 — Admin & Principal Discovery ✅ DONE
- [x] Admin portal screens discovered — **23 screens**, 22 providers
- [x] Principal portal screens discovered — **14 screens**, 3 providers
- [x] Shell/stub ViewModels traced to actual screen usage (see findings below)

**Finding — the "Misc ViewModels return empty lists" issue is mostly DEAD CODE, not broken screens:**

| ViewModel | File | Used by a screen? | Verdict |
|---|---|---|---|
| AdminInventoryViewModel | AdminMiscViewModels.kt | No (screen uses `…ViewModel2`) | dead code |
| AdminLibraryViewModel | AdminMiscViewModels.kt | No (screen uses `…ViewModel2`) | dead code |
| AdminTransportViewModel | AdminMiscViewModels.kt | No (screen uses `…ViewModel2`) | dead code |
| AdminExamMgmtViewModel | AdminMiscViewModels.kt | No (screen uses `AdminExamViewModel`) | dead code |
| AdminAcademicYearConfigViewModel | AdminMiscViewModels.kt | No (uses `AdminAcademicYearViewModel`) | dead code |
| PrincipalEventsManagementViewModel | PrincipalMiscViewModels.kt | No (uses `PrincipalEventsViewModel` — WIRED) | dead code |
| PrincipalInstitutionalPerformanceViewModel | PrincipalMiscViewModels.kt | No (uses `PrincipalPerformanceViewModel` — WIRED) | dead code |
| PrincipalStudyMaterialsViewModel | PrincipalMiscViewModels.kt | No — screen uses `PrincipalStudyMaterialsViewModel2` (WIRED) | dead code |

**CORRECTION to an earlier reading:** the initial grep matched on substring and suggested
PrincipalStudyMaterialsScreen used the shell. It does not — it uses
`PrincipalStudyMaterialsViewModel2` in PrincipalViewModel.kt, which is properly wired.
**All 8 shells were dead code; there was no real defect.**

- [x] Both dead files DELETED (`AdminMiscViewModels.kt` 6 classes, `PrincipalMiscViewModels.kt`
      4 classes). Debug + release builds verified clean afterwards.

### Stage 1 — Backend Integrity
- [x] fix_db.py — absent (nothing to delete)
- [x] debug.py — absent (nothing to delete)
- [x] **study_material.py — NOT DELETED.** The only match is `app/db/models/study_material.py`,
      a live SQLAlchemy model for the `study_materials` table imported by 10 files.
      Deleting it would break the app. Tracker entry assumed a debug script that does not exist.
- [x] Debug `print()` removed — 0 remaining in auth.py, dependencies.py, teaching_logs.py,
      main.py, backup_service.py
- [x] main.py ad-hoc ALTER TABLE reconciled into Alembic (migrations c9f21a7de401,
      d4e77b1c9a02, e5a11c3f7b28); `create_all` removed from startup
- [x] Alembic single head confirmed — `c2f9a6d80e14`; verified deterministic on empty PostgreSQL 16
- [x] JSON→Postgres resolved: papers, council, parent_inquiries, college_info, teaching_logs
      now read/write `local_json_db` (migration c2f9a6d80e14); files kept as first-run seed only
- [x] JSON→Postgres traced for ALL modules — every live store is now database-backed:
      - study_materials: **materials themselves are SQL/Postgres**; only per-student state
        (bookmarks, favourites, downloads, notifications, analytics) was a JSON document —
        now DB-backed. Not a split-brain; a clean split, traced endpoint by endpoint.
      - legal_events / legal_questions / legal_registrations → DB-backed
      - online_meetings / lecture_recordings → DB-backed. Also removed a module-level
        `_meetings_store` cache that issued a DB query at import time and diverged
        between uvicorn workers.
      - academic_calendar, activity_points → already DB-backed
      - papers, council, parent_inquiries, college_info, teaching_logs → DB-backed earlier
      - substitutions / assignments / clubs / internships → no JSON store exists; SQL only
      - `attendance_corrections_db.json`, `teaching_logs_db.json` → UNREFERENCED dead files
        (deletion needs approval — listed, not removed)
- [x] Production DB password rotation — N/A; no live credential in repo
      (only `CONFIRM-WIPE-…`, a reset-confirmation phrase, not a credential)
- [x] Dead JSON files deleted: `attendance_corrections_db.json`, `teaching_logs_db.json`
      (0 code references, verified before deletion)
- [x] **`study_material.py` NOT deleted** — it is a live model, see above

### Stage 2 — Screens
Role Selection: [x] | Login: [x] | Email Change: [ ] | Password Reset: [ ]
  - RoleSelection: static picker, no data path — loading/error states correctly N/A. Clean.
  - Login: **SECURITY FIX** — "Use Demo Account" button filled the seeded password
    `Password@123` in every build. Now inside `if (BuildConfig.DEBUG)`, so it is compiled
    out of release APKs entirely rather than merely hidden. Debug + release builds verified.
    Error/loading/validation states present and correct.
Email Change: [x] | Password Reset: [x]
  - **CRITICAL REGRESSION FOUND & FIXED — rate limiter broke account recovery.**
    `headers_enabled=True` on the slowapi Limiter made every rate-limited endpoint that
    returns a Pydantic model raise `parameter 'response' must be an instance of
    starlette.responses.Response` → **HTTP 500**. This silently broke
    forgot-password, reset-password, change-password and request-email-change — the whole
    account-recovery path. Login survived only because it takes a `response: Response`
    parameter. Fixed: `headers_enabled=False` (429s still returned; only the advisory
    headers are dropped). All four endpoints verified working.
  - **SECURITY — account-takeover chain closed.** `request-email-change` required no
    current password, so a stolen session could repoint the account to an attacker's
    address then take it over via password reset. Now re-authenticates (mirrors
    /change-password) and is rate limited. Client updated end-to-end: DTO → repository →
    ViewModel → dialog (password field added, button disabled until filled, cleared on
    reset). Verified: wrong password 400, missing field 422, correct password succeeds.
  - Password reset verified: no user enumeration, single-use token, 1h expiry,
    strength validation, invalid token rejected.
  - **Note (seed-data limitation, not a bug):** seeded accounts use `@cams.local`, a
    reserved TLD the email validator correctly rejects, so forgot-password cannot be
    exercised with them. Works normally with real domains.
**Student** — NOTE: the portal is larger than the original estimate of 14. The drawer
exposes ~30 destinations; `features/student/screens/` holds only 6, the rest live in
shared feature folders (academics, attendance, fees, leave, campus_life, ...).

Core pass done: [x] Dashboard · [x] Academics · [x] Profile · [x] Settings
                [x] ActivityPoints · [x] MootCourtMemorials
Cross-cutting verified for the student role:
  - All 10 core screens have error + loading states present (audited, none missing).
  - Hardcoded `listOf(...)` occurrences reviewed — all legitimate (weekday labels,
    leave-type dropdowns, empty-timetable grid skeleton). No fake data presented as real.
  - **FIXED:** Settings showed a hardcoded "Version 1.0.0" while the build is actually
    `1.0` — already wrong and would drift every release. Now reads
    `BuildConfig.VERSION_NAME`/`VERSION_CODE`.
  - Settings "Language/Theme/Privacy Policy" are correctly `enabled = false` placeholders,
    not dead buttons.
  - **Data accuracy verified against the database** (roll_no, semester, batch_year, cgpa,
    email, full_name) — all match exactly.
  - **Isolation verified:** student1 and student2 each see only their own record.
  - **Role boundaries verified:** student → faculty roster / admin backups / HOD leaves /
    principal dashboard all return 403.

Remaining student screens (shared folders, not yet individually QA'd): Attendance,
InternalMarks, Timetable, Fees, Leave, Circulars, Notifications, Assignments, Syllabus,
HallTicket, Calendar, Council, CampusLife, Certifications, Internships, LegalEvents,
LegalSkills, LexNova, LexSphere, CommunityService, InnovationWall, ProjectShowcase,
OnlineMeetings, Grievances, StudyMaterials.
**Parent (9 screens — 2 more than the original estimate of 7)** — ALL DONE:
[x] Dashboard · [x] ParentAttendance · [x] ExamResults · [x] FeeStatus
[x] ParentChildProfile · [x] ParentChildTimetable · [x] ParentNotices
[x] ContactCollege · [x] ParentSettings

  - **FIXED — silent failures:** `ParentChildTimetableScreen` and `ParentNoticesScreen`
    never read `uiState.error`, even though both ViewModels expose it. A failed request
    rendered an empty screen with no message and no retry — indistinguishable from
    "no data". Both now use the same `NetworkErrorView` + retry pattern as the other
    parent screens (no design change).
  - All 9 screens verified for loading/empty states; hardcoded lists reviewed and
    legitimate.
  - **IDOR verified on all 7 child endpoints** (dashboard, fees, marks, attendance,
    timetable, profile, internal-marks): parent1 explicitly passing parent2's child_id
    is blocked with 403 on every one.
  - **Isolation verified:** each parent's /children returns only their own child.
  - **Money path:** parent cannot create an order against a fee record outside their
    family (404).
  - **Role boundaries:** parent → faculty roster / admin backups / HOD leaves all 403.
  - **Data accuracy verified against the database** (roll_no, semester, full_name).
**Faculty (26 screens — far more than the estimate of 9)** — portal pass DONE:
  - All 26 screens audited for error/loading/empty states — **all present, none missing.**
  - Hardcoded `listOf(...)` reviewed across the 4 heaviest users (SmartClassroom,
    StudyMaterials, Internships, LegalEvents) — all legitimate tab labels and dropdown
    option sets. No fake data presented as real.
  - **Teaching-scope enforcement verified on both data-integrity-critical paths:**
      * attendance `mark-bulk` — F1 marking F2's section → **403**; own section → 200
      * internal marks `GET /marks/internal` — F2's section → **403**; own → 200
    This is the guarantee that a faculty member cannot alter grades or attendance for a
    class they do not teach.
  - Roster scoping verified: F1 sees 4 students, F2 sees 2 — different, and **PII
    (aadhaar/income/medical) redacted for both**.
  - Role boundaries: faculty → admin backups 403, principal dashboard 403.
  - `users/academic-years/list` returns 200 for faculty — **verified intentional**: it is
    read-only reference data needed for filtering, and every write operation
    (initialize/update/delete/set-semester) is ADMIN/SUPER_ADMIN only.
  - Core endpoints all 200: dashboard, students/list, attendance/sections,
    mentor/students, study-materials/my-materials, teaching-logs/notifications.
**HOD (26 screens) / Admin (23) / Principal (14)** — CROSS-CUTTING SWEEP DONE
(depth-on-finding rather than screen-by-screen, at user's request)

**Real counts:** HOD is 26 screens, not 9. Admin 23 and Principal 14 confirmed accurate.

**FIXED — silent failures on 9 screens.** These rendered an empty screen on any request
failure: no message, no retry, indistinguishable from having no data. All 9 ViewModels
already exposed `error`; the screens simply never read it. Now each has a
`NetworkErrorView` branch with a **working** retry wired to that ViewModel's real load
function:
  AdminAcademicCatalog(loadCatalog) · AdminBackups(fetchBackups) ·
  AdminSystemConfig(fetchConfig) · AdminUserMgmt(fetchUsers) ·
  PrincipalCirculars(loadCirculars) · PrincipalGrievances(loadGrievances) ·
  PrincipalInfrastructure(loadInfrastructure) · PrincipalResearchCompliance(loadResearch) ·
  PrincipalStrategicNotices(loadCirculars)

**Cross-role access matrix verified** (student/parent/faculty/hod/principal/admin against
6 sensitive endpoints): students and parents blocked from every staff endpoint; admin
correctly cannot reach the principal dashboard; principal has intended oversight breadth.

**HOD department scoping verified:** passing another department's `department_id` is
ignored server-side — the HOD still receives only their own department. The query
parameter cannot widen scope.

### UI POLISH PASS ✅ (static classes)
- [x] **Touch targets.** Naive scan flagged 96 — verified and reduced to **21 genuine**
      (most hits were icons *inside* buttons, which is normal). Of those, 18 had no visible
      decoration, so enlarging the tap area to 40dp changes nothing visually — icons keep
      their original size. Applied.
      **3 deliberately NOT changed** (StudyMaterials L199, HODApprovals L136/L142): their
      `.size()` drives a visible `.background()`, so enlarging would visibly grow a coloured
      chip — that is a design change, which is out of scope per the hard constraints.
- [x] **Text overflow.** 4 dynamic `Text` inside `Row` with `weight()` and no
      `maxLines`/`overflow` — student name + roll number, and subject + section, all of which
      clip mid-word on long values. Added `maxLines = 1` + `Ellipsis`.
      (AdminHostel, AdminLibrary, AdminTransport, FacultySmartClassroom)
- [x] **Accessibility.** 24 icons inside interactive `IconButton`s had
      `contentDescription = null`, making them unidentifiable to TalkBack. Labelled from the
      icon's own meaning (Delete/Close/Edit/Send/Back/Download/…). No visual change.

**Still open — needs a running app, not static analysis:** visual alignment, spacing rhythm,
typography consistency, and rendering on small/large screens. Static checks have been taken
as far as they usefully go.

**Admin (23)** — execution order:
1 [ ] AdminDashboard · 2 [ ] AdminUserMgmt · 3 [ ] AdminAcademicCatalog
4 [ ] AdminAcademicYearConfig · 5 [ ] AdminBatchSetup · 6 [ ] AdminCourseSetup
7 [ ] AdminFacultyAssignment · 8 [ ] AdminExamMgmt · 9 [ ] AdminAttendanceDefaulters
10 [ ] AdminFeeMgmt · 11 [ ] AdminCollectFee · 12 [ ] AdminSalaryMgmt
13 [ ] AdminCirculars · 14 [ ] AdminNotifications · 15 [ ] AdminAcademicCalendar
16 [ ] AdminReports · 17 [ ] AdminLogs · 18 [ ] AdminBackups · 19 [ ] AdminSystemConfig
20 [ ] AdminHostel · 21 [ ] AdminInventory · 22 [ ] AdminLibrary · 23 [ ] AdminTransport

**Principal (14)** — execution order:
1 [ ] PrincipalDashboard · 2 [ ] PrincipalInstitutionalPerformance · 3 [ ] PrincipalApprovals
4 [ ] PrincipalFacultyOverview · 5 [ ] PrincipalGrievances · 6 [ ] PrincipalClassDiary
7 [ ] PrincipalStudyMaterials ⚠️ shell VM · 8 [ ] PrincipalResearchCompliance
9 [ ] PrincipalBudgetGrants · 10 [ ] PrincipalInfrastructure · 11 [ ] PrincipalEventsManagement
12 [ ] PrincipalInstitutionalCalendar · 13 [ ] PrincipalCirculars · 14 [ ] PrincipalStrategicNotices

### Stage 3 — Final E2E ✅ (except FCM)
- [x] Six-role walkthrough on a FRESH PostgreSQL 16 database (migrate → seed → app):
      all golden paths 200 for student/parent/faculty/HOD/principal/admin.
      Migration exit 0, single head c2f9a6d80e14, seed exit 0, tests 23 passed.
- [x] Full secret scan — **0 secret files tracked**; `.env`, `local.properties`,
      `my-upload-key.jks`, `debug.keystore` all present on disk but untracked.
      Remaining grep hits are variable assignments (`access_token = create_access_token(...)`),
      plus one test fixture — no hardcoded credentials.
- [x] PlaceholderScreen.kt — **no longer wired to any route** (0 references in
      AppNavigation). Item resolved; the file itself is now unused.
- [x] **FCM token registration — IMPLEMENTED.**
      Backend: `device_tokens` table (migration d1a8c47f62b3, token unique not user, so a
      device that changes hands re-points to whoever signs in next);
      `POST/DELETE /notifications/device-token`; `app/services/push_service.py` using
      firebase-admin, which **degrades gracefully** when unconfigured (push skipped and
      logged — mirrors SMTP/Razorpay) and prunes tokens FCM reports as dead.
      Android: `PushTokenRegistrar` sends the token; `onNewToken` now registers instead of
      discarding; registration fires on successful login, unregistration on logout
      **before** the session is cleared.
      Verified: register is idempotent (no duplicate rows), unauthenticated → 401,
      device handover re-points the token to the new user, and one user **cannot**
      unregister another's device.
      **Route-shadowing bug found and fixed:** `DELETE /device-token` was being swallowed by
      the earlier single-segment `DELETE /{notification_id}` route and returned
      "Notification not found". The device-token routes now sit above the parameterised ones.
      **Still required to actually send pushes:** set `FIREBASE_CREDENTIALS_JSON` or
      `FIREBASE_CREDENTIALS_FILE`, and add `app/google-services.json` from the same Firebase
      project (absent from this repo by design — it is per-project config).
- [x] Cross-role access boundaries verified — full matrix, 10 endpoints × 6 roles,
      plus unauthenticated spot-checks (all 401).

**CRITICAL PRIVILEGE FLAW FOUND & FIXED — `role_required` treated PRINCIPAL as a wildcard.**
`app/core/dependencies.py` returned early for PRINCIPAL on *every* guard, before the
allow-list was consulted. The comment claimed "HOD and Faculty endpoints"; the effect was
that a principal satisfied **all 219 guards that exclude the role** — including 33
ADMIN/SUPER_ADMIN-only operations (backups, academic-year setup, user management), 5
SUPER_ADMIN-only, and the STUDENT/PARENT endpoints.
Verified safe before changing: the Principal repository calls no admin-only endpoint, and
all principal routes declare PRINCIPAL in their own guard. Inheritance is now scoped to
FACULTY/HOD only; SUPER_ADMIN remains a true superuser.
Verified after: principal → backups 403, temp-debug-users 403, while every principal
screen and faculty/HOD oversight endpoint still returns 200. Admin and HOD unaffected.
23 tests pass; Android builds clean.

### Known functional gaps
- FCM token may never reach backend (CamsFirebaseMessagingService.kt TODO)
- PlaceholderScreen.kt still wired to some nav routes — verify which
- PrincipalStudyMaterialsScreen bound to a shell ViewModel (see Stage 0)
