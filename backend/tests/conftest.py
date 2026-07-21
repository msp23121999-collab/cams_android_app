"""Shared pytest fixtures for the CAMS backend test suite.

DATABASE ISOLATION
------------------
The developer's real database is the SQLite file ``backend/cams.db`` (see
``.env``).  Before *any* application module is imported, this module points
``DATABASE_URL`` at a throwaway SQLite file inside pytest's tmp area.  Nothing
in the suite can therefore touch ``cams.db``:

  * ``app.core.config.Settings`` reads ``DATABASE_URL`` from the environment,
    and environment variables take precedence over the ``.env`` file in
    pydantic-settings.
  * ``app.core.json_db_helper`` derives its own sqlite3 connection from the same
    ``DATABASE_URL``, so the JSON-blob side tables land in the temp file too.

The schema is created explicitly here from ``Base.metadata`` rather than relying
on the ``create_all`` call in ``app/main.py`` (which is being removed) or on
Alembic (owned by another agent).  The fixture is correct either way because
``create_all`` is a no-op when the tables already exist.
"""
from __future__ import annotations

import os
import secrets
import sys
import tempfile
import uuid
from datetime import date, datetime, time, timedelta, timezone
from pathlib import Path

BACKEND_DIR = Path(__file__).resolve().parent.parent
if str(BACKEND_DIR) not in sys.path:
    sys.path.insert(0, str(BACKEND_DIR))

# ── Must run before any `app.*` import ───────────────────────────────────────
_TEST_DB_DIR = Path(tempfile.mkdtemp(prefix="cams-tests-"))
_TEST_DB_PATH = (_TEST_DB_DIR / "cams_test.db").as_posix()
os.environ["DATABASE_URL"] = f"sqlite+aiosqlite:///{_TEST_DB_PATH}"
os.environ["ENVIRONMENT"] = "test"
# Never hardcode secrets: reuse the developer's configured key if the process
# already has one, otherwise mint a random per-run key.
os.environ.setdefault("JWT_SECRET_KEY", secrets.token_urlsafe(48))
# Razorpay must look "configured" so the gateway endpoints run their real logic;
# the client itself is always mocked, so these are throwaway per-run values and
# never reach Razorpay.
os.environ["RAZORPAY_KEY_ID"] = f"rzp_test_{secrets.token_hex(8)}"
os.environ["RAZORPAY_KEY_SECRET"] = secrets.token_hex(16)
# ─────────────────────────────────────────────────────────────────────────────

import pytest  # noqa: E402
import pytest_asyncio  # noqa: E402
from httpx import ASGITransport, AsyncClient  # noqa: E402

from app.core.config import settings  # noqa: E402

assert settings.DATABASE_URL.endswith("cams_test.db"), (
    f"Refusing to run: tests are pointed at {settings.DATABASE_URL!r}, not the "
    "temporary test database. Aborting to protect the developer's data."
)

from app.core.security import create_access_token, hash_password  # noqa: E402
from app.db import models  # noqa: F401,E402  (registers every mapper)
from app.db.base import Base  # noqa: E402
from app.db.models.academic import (  # noqa: E402
    ApprovalStatus,
    Course,
    Degree,
    Department,
    Section,
    Timetable,
    TimetableApproval,
    Weekday,
)
from app.db.models.fee import FeeRecord, FeeStatus, FeeStructure  # noqa: E402
from app.db.models.student import ParentStudentMap, Student  # noqa: E402
from app.db.models.user import User, UserRole  # noqa: E402
from app.db.session import AsyncSessionLocal, engine  # noqa: E402
from app.main import create_app  # noqa: E402


# ─────────────────────────────────────────────────────────────────────────────
#  Schema + seed
# ─────────────────────────────────────────────────────────────────────────────

def _uid() -> str:
    return str(uuid.uuid4())


@pytest_asyncio.fixture(scope="session", autouse=True)
async def _schema():
    async with engine.begin() as conn:
        await conn.run_sync(lambda c: Base.metadata.create_all(c, checkfirst=True))
    yield
    await engine.dispose()


class Seed:
    """Plain container for the ids/tokens the tests need."""

    def __init__(self, **kw):
        self.__dict__.update(kw)


@pytest_asyncio.fixture(scope="session")
async def seed(_schema) -> Seed:
    """Two unrelated families plus staff, seeded once for the whole session.

    family A: parent_a -> student_a (dept A, has an unpaid tuition fee record)
    family B: parent_b -> student_b (dept A, fee record fully paid -> the
              "no fees due" edge case that produced the due_date=null crash)
    """
    ids = {}
    async with AsyncSessionLocal() as db:
        dept = Department(id=_uid(), name="School of Law", code="LAW", program_level="UG")
        dept_other = Department(id=_uid(), name="School of Commerce", code="COM", program_level="UG")
        db.add_all([dept, dept_other])
        await db.flush()

        degree = Degree(
            id=_uid(), code="BALLB", name="B.A. LL.B.", applicable_batch="2023",
            program_level="UG", duration_years=5, dept_id=dept.id, passing_marks=40,
        )
        db.add(degree)
        await db.flush()

        def mk_user(role: UserRole, name: str, dept_id: str | None):
            u = User(
                id=_uid(), email=f"{name}@test.cams.local", full_name=name.title(),
                hashed_password=hash_password(secrets.token_urlsafe(16)),
                role=role, is_active=True, department_id=dept_id,
            )
            db.add(u)
            return u

        admin = mk_user(UserRole.ADMIN, "admin", None)
        principal = mk_user(UserRole.PRINCIPAL, "principal", None)
        hod = mk_user(UserRole.HOD, "hod", dept.id)
        hod_other = mk_user(UserRole.HOD, "hodother", dept_other.id)
        faculty = mk_user(UserRole.FACULTY, "faculty", dept.id)
        faculty_other = mk_user(UserRole.FACULTY, "facultyother", dept_other.id)
        stu_a_user = mk_user(UserRole.STUDENT, "studenta", dept.id)
        stu_b_user = mk_user(UserRole.STUDENT, "studentb", dept.id)
        parent_a = mk_user(UserRole.PARENT, "parenta", None)
        parent_b = mk_user(UserRole.PARENT, "parentb", None)
        await db.flush()

        dept.hod_id = hod.id
        dept_other.hod_id = hod_other.id

        course = Course(
            id=_uid(), dept_id=dept.id, degree_id=degree.id, code="LAW101",
            name="Constitutional Law", credits=4, semester=1,
        )
        db.add(course)
        await db.flush()

        section = Section(id=_uid(), course_id=course.id, section_name="A", faculty_id=faculty.id)
        db.add(section)
        await db.flush()

        def mk_student(user: User, roll: str, mentor_id: str | None):
            s = Student(
                id=_uid(), user_id=user.id, roll_no=roll, department_id=dept.id,
                semester=1, batch_year=2023, degree_id=degree.id, section_id=section.id,
                full_name=user.full_name, quota="Government", community_category="General",
                mentor_id=mentor_id, mobile_number="9000000000",
            )
            db.add(s)
            return s

        stu_a = mk_student(stu_a_user, "LAW23001", faculty.id)
        stu_b = mk_student(stu_b_user, "LAW23002", None)
        await db.flush()

        db.add_all([
            ParentStudentMap(id=_uid(), parent_id=parent_a.id, student_id=stu_a.id),
            ParentStudentMap(id=_uid(), parent_id=parent_b.id, student_id=stu_b.id),
        ])

        # Approved timetable slot so the student timetable endpoint returns a row.
        tt = Timetable(
            id=_uid(), section_id=section.id, subject_id=course.id, faculty_id=faculty.id,
            room="R-101", weekday=Weekday.MONDAY,
            start_time=time(9, 0), end_time=time(10, 0),
        )
        db.add(tt)
        await db.flush()
        db.add(TimetableApproval(
            id=_uid(), timetable_id=tt.id, status=ApprovalStatus.APPROVED, approved_by=principal.id,
        ))

        # Tuition fee structure for semester 1. FeeService prices semester-1
        # tuition from the blueprint table (default 85000), not from this amount.
        structure = FeeStructure(
            id=_uid(), dept_id=dept.id, semester=1, amount=85000,
            due_date=date.today() + timedelta(days=30), fee_type="Tuition Fee",
        )
        db.add(structure)
        await db.flush()

        rec_a = FeeRecord(id=_uid(), student_id=stu_a.id, fee_structure_id=structure.id,
                          status=FeeStatus.PENDING)
        rec_b = FeeRecord(id=_uid(), student_id=stu_b.id, fee_structure_id=structure.id,
                          status=FeeStatus.PENDING)
        db.add_all([rec_a, rec_b])
        await db.commit()

        ids = dict(
            dept_id=dept.id, dept_other_id=dept_other.id, degree_id=degree.id,
            course_id=course.id, section_id=section.id, timetable_id=tt.id,
            fee_structure_id=structure.id,
            admin=admin, principal=principal, hod=hod, hod_other=hod_other,
            faculty=faculty, faculty_other=faculty_other,
            student_a_user=stu_a_user, student_b_user=stu_b_user,
            parent_a=parent_a, parent_b=parent_b,
            student_a_id=stu_a.id, student_b_id=stu_b.id,
            fee_record_a=rec_a.id, fee_record_b=rec_b.id,
        )

    s = Seed(**ids)

    # Fully settle student B's fee via the service so the "nothing due"
    # (due_date == null) shape is exercised with realistic data.
    from app.db.models.fee import Payment
    from app.services.fee_service import FeeService
    async with AsyncSessionLocal() as db:
        svc = FeeService(db)
        summary = await svc.get_student_fee_summary(s.student_b_id)
        for rec in summary["records"]:
            if rec["remaining_amount"] > 0:
                db.add(Payment(
                    id=_uid(), fee_record_id=rec["record_id"], amount=rec["remaining_amount"],
                    mode="Cash", txn_id=f"SEED-{uuid.uuid4().hex[:10]}", status="paid",
                    paid_at=datetime.now(timezone.utc),
                ))
        await db.commit()
        summary = await svc.get_student_fee_summary(s.student_b_id)
        for rec in summary["records"]:
            fr = await db.get(FeeRecord, rec["record_id"])
            if rec["status"] == "paid" and fr is not None:
                fr.status = FeeStatus.PAID
        await db.commit()
    return s


def token_for(user: User) -> str:
    role = user.role.value if hasattr(user.role, "value") else str(user.role)
    return create_access_token(subject=user.id, role=role)


def auth(user: User) -> dict[str, str]:
    return {"Authorization": f"Bearer {token_for(user)}"}


# ─────────────────────────────────────────────────────────────────────────────
#  App / client
# ─────────────────────────────────────────────────────────────────────────────

@pytest.fixture(scope="session")
def app(_schema):
    return create_app()


@pytest_asyncio.fixture
async def client(app, seed) -> AsyncClient:
    async with AsyncClient(
        transport=ASGITransport(app=app), base_url="http://testserver"
    ) as c:
        yield c
