# CAMS ENTERPRISE ERP - BACKEND ARCHITECTURE DESIGN

## 1. Directory Structure (Domain-Driven Clean Architecture)
```text
backend/
├── alembic/                  # Database migrations (Wait for DB schema)
├── app/
│   ├── api/
│   │   ├── dependencies/     # Auth, DB, Role validations
│   │   ├── middleware/       # Exception handlers, Logging, Audit
│   │   └── v1/
│   │       ├── api.py        # Main API router setup
│   │       └── endpoints/    # Route controllers (auth, students, leave, etc.)
│   ├── core/
│   │   ├── config.py         # Pydantic BaseSettings (Environment vars)
│   │   ├── exceptions.py     # Global custom exceptions
│   │   ├── security.py       # JWT, Password hashing
│   │   └── logger.py         # Enterprise logging config
│   ├── db/
│   │   └── session.py        # SQLAlchemy session & engine
│   ├── models/               # SQLAlchemy ORM Models (Awaiting Schema)
│   ├── schemas/              # Pydantic Models (Validation & Serialization)
│   ├── repositories/         # Database access layer (CRUD operations)
│   ├── services/             # Business logic layer (Approval workflows, etc.)
│   ├── utils/                # File upload, Email/SMS notifications, Background tasks
│   ├── main.py               # FastAPI application entry point
│   └── worker.py             # Celery/Background task workers
├── tests/
├── requirements.txt
└── Dockerfile
```

## 2. Core Layers Overview

*   **API Router Layer (`api/v1/endpoints/`)**: Handles HTTP requests, delegates validation to Pydantic, calls the Service Layer, and formats standard responses.
*   **Service Layer (`services/`)**: Contains pure business logic. e.g., The multi-tier approval workflow for "Student Leave" (Faculty -> HOD -> Principal) lives here, completely decoupled from the HTTP layer.
*   **Repository Layer (`repositories/`)**: Abstracted database access. The Service layer calls repositories, ensuring that business logic doesn't contain raw SQL or SQLAlchemy specifics.
*   **Authentication & Authorization (`core/security.py` & `api/dependencies/`)**: Validates JWTs, checks token expiry, and ensures the user holds the correct Role (Super Admin, Principal, HOD, etc.) before allowing endpoint access.
*   **Exception & Validation Layer (`core/exceptions.py`)**: Global handlers catch all errors (DB, Validation, Auth) and return them in the standardized API format.

## 3. Standardized API Response Format
Every endpoint will return data in this exact structure:
```json
{
  "status": "success | error",
  "message": "Human readable message",
  "data": { ... },
  "pagination": { "page": 1, "total": 100 }, // Optional
  "errors": [ ... ], // Included if status == error
  "timestamp": "2026-07-08T10:00:00Z",
  "request_id": "uuid-v4-string"
}
```

## 4. Workflows & Business Logic (Example: Leave Request)
The backend is designed to support the frontend's strict enterprise workflows. 
1. **Student** submits leave (File upload handled via `utils.storage`).
2. **LeaveService** records the request as `PENDING_FACULTY`.
3. Notification triggered (via Background Tasks) to the assigned **Faculty**.
4. Faculty approves -> State shifts to `PENDING_HOD`.
5. HOD approves -> State shifts to `PENDING_PRINCIPAL`.
6. Principal approves -> State shifts to `APPROVED`.
7. **Audit Layer** logs every state change and the actor responsible.

## 5. Modules Identified from Frontend
Based on the frontend code analysis, the backend API will support the following domain modules:
*   **Auth Module**: Login, Logout, Token Refresh, Password Reset.
*   **Users Module**: User profiles, Role mapping (RBAC), Dashboard summaries.
*   **Academics Module**: Academic years, Courses, Batches, Subjects, Timetables.
*   **Attendance & Leave Module**: Biometric/Manual attendance, Multi-tier leave workflows.
*   **Exams & Grading Module**: Exam scheduling, Internal marks, Result publishing.
*   **Finance Module**: Fee collection, Payroll, Scholarships.
*   **Operations Module**: Library, Hostel, Transport, Infrastructure management.
*   **Communications Module**: Circulars, Events, Grievances, Notifications.

**STATUS:** 
The backend architecture is scaffolded in the `backend/` directory. 
Awaiting the complete Database Schema to generate Models, Repositories, and the actual API implementations without guessing the database structure.
