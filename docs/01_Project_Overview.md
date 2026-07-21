# CAMS Enterprise
## 01. Project Overview

### Project Introduction
CAMS Enterprise is a comprehensive, production-grade campus management system built specifically for legal educational institutions. The system provides role-specific portals to handle all aspects of academic lifecycle, administrative operations, communication, and human resource management.

### Vision
To provide a seamless, digital-first experience for all stakeholders in a legal education institution, unifying academic administration, student engagement, and faculty management into a single robust platform.

### Objectives
- Digitize and streamline campus workflows to eliminate manual interventions.
- Enforce strict Role-Based Access Control (RBAC) across both mobile client and backend.
- Ensure data integrity using robust schemas and validated DTOs.
- Provide tailored experiences via specific portals for different users.

### Scope
The project encompasses 6 main roles (Student, Parent, Faculty, HOD, Principal, Admin) using an Android-based mobile frontend and a scalable FastAPI Python backend backed by PostgreSQL.

### Features
- **Multi-Role Portals:** Isolated views for users based on their assigned role.
- **Academic Management:** Course and classroom management, attendance tracking, exams, and grading.
- **Law-Specific Operations:** Modules for Moot Court scheduling, Legal Aid, and specific Activity Points tracking.
- **Communication:** Real-time chat, broadcasting, and push notifications via Firebase.
- **Administration:** Budgeting, Payroll, PF management, Backup scheduling.

### Architecture Overview
The system follows a micro-service-inspired monolithic client-server architecture. The Android client manages UI and local state, communicating strictly over HTTPS REST APIs with the Python FastAPI backend. The backend is fully responsible for business logic, RBAC verification, and transactional database interactions.

### Technology Stack
- **Frontend:** Android (Kotlin), Jetpack Compose (Declarative UI), Retrofit (Networking), Room (Local Cache), Hilt (Dependency Injection).
- **Backend:** Python 3.10+, FastAPI (ASGI Framework), SQLAlchemy (ORM), Alembic (Migrations), Celery (Background Tasks), Pydantic (Validation).
- **Database:** PostgreSQL.
- **Infrastructure:** Docker & Docker Compose for consistent environments, Uvicorn (ASGI server).

### System Components
1. **Android App (`/app`)**: The mobile interface.
2. **Backend API (`/backend`)**: The core business logic and API layer.
3. **Database Scripts (`/database`)**: Schema definition and seed data.
4. **Docs (`/docs`)**: Architecture, configuration, and developer guides.

### Design Principles
- **Self-Contained:** Everything needed to run the system is within the repository.
- **Strict Typing:** Strong reliance on Kotlin type safety and Python type hints (Pydantic).
- **Database-Driven Schema:** The DB schema is entirely owned and mutated by Alembic migrations.
- **Security-First:** Strict RBAC middleware enforced on all API endpoints.
