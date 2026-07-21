"""Auth-gated file download endpoint.

Various upload endpoints (profile photos, study materials, assignment
attachments, hall tickets, leave attachments, fee receipts, ...) save files
under ``<app>/static/uploads`` but that content is mostly personal data.

Authorization here is **fail-closed**: a file is served only when it is either
(a) explicitly classified as a genuinely shared/broadcast asset, or (b) the
requesting user can be proven — from the database — to be an owner or an
authorized viewer of that asset.  Anything that cannot be classified or whose
owner cannot be resolved is served to staff roles only and 404s for everyone
else.
"""
import json
import mimetypes
import os

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy import or_, select
from sqlalchemy.ext.asyncio import AsyncSession
from fastapi.responses import FileResponse

from app.core.dependencies import get_current_user, get_db_session
from app.db.models.user import User, UserRole

router = APIRouter()

# Administrative staff: allowed to view records institution-wide.
_STAFF_ROLES = (UserRole.HOD, UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.SUPER_ADMIN)
_PAYROLL_STAFF_ROLES = _STAFF_ROLES
# Kept for backwards compatibility with existing imports/tests.
_STUDENT_RECORD_STAFF_ROLES = _STAFF_ROLES

# Path prefixes / filename prefixes that hold student personal data and therefore
# require an ownership check, not merely authentication.
_STUDENT_PERSONAL_PREFIXES = ("documents/",)
_STUDENT_PERSONAL_FILENAME_PREFIXES = ("avatar_",)

# Directories whose content is broadcast to the whole institution.
_SHARED_PREFIXES = ("announcements/",)
# Class-diary attachments are saved by teaching_logs.upload_diary_file as
# ``diary_{uuid}{ext}`` in the uploads root and are shared with the class.
_SHARED_FILENAME_PREFIXES = ("diary_",)

_BACKEND_DIR = os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))
UPLOADS_DIR = os.path.join(_BACKEND_DIR, "static", "uploads")

_PAPERS_DB_PATH = os.path.join(_BACKEND_DIR, "api", "papers_db.json")


def _resolve_safe_path(relative_path: str) -> str:
    """Resolve `relative_path` under UPLOADS_DIR, rejecting traversal attempts."""
    if not relative_path or ".." in relative_path.replace("\\", "/").split("/"):
        raise HTTPException(status_code=400, detail="Invalid file path")
    if os.path.isabs(relative_path):
        raise HTTPException(status_code=400, detail="Invalid file path")

    normalized = os.path.normpath(relative_path).replace("\\", "/")
    if normalized.startswith("..") or normalized.startswith("/"):
        raise HTTPException(status_code=400, detail="Invalid file path")

    full_path = os.path.normpath(os.path.join(UPLOADS_DIR, normalized))
    uploads_dir_norm = os.path.normpath(UPLOADS_DIR)
    if not (full_path == uploads_dir_norm or full_path.startswith(uploads_dir_norm + os.sep)):
        raise HTTPException(status_code=400, detail="Invalid file path")

    return full_path


def _url_variants(normalized: str) -> list[str]:
    """Every URL form under which a stored file may be recorded in the DB."""
    return [
        f"/api/v1/files/{normalized}",
        f"/static/uploads/{normalized}",
        f"/app/static/uploads/{normalized}",
        normalized,
    ]


def _forbid() -> HTTPException:
    return HTTPException(status_code=403, detail="You do not have access to this file")


def _not_found() -> HTTPException:
    return HTTPException(status_code=404, detail="File not found")


def _is_student_personal(normalized: str, filename: str) -> bool:
    return normalized.startswith(_STUDENT_PERSONAL_PREFIXES) or filename.startswith(
        _STUDENT_PERSONAL_FILENAME_PREFIXES
    )


# ── ownership resolution helpers ───────────────────────────────────────────

async def _owning_student_id(db: AsyncSession, normalized: str) -> str | None:
    """Find which student a personal asset belongs to.

    Filenames are server-generated as ``{kind}_{student_id}_{uuid}{ext}``, but we
    resolve ownership from the database rather than trusting the filename shape.
    """
    from app.db.models.certification import Certification
    from app.db.models.student import Student

    urls = _url_variants(normalized)

    res = await db.execute(
        select(Student.id).where(
            or_(
                Student.document_aadhaar_url.in_(urls),
                Student.document_community_url.in_(urls),
                Student.document_tc_url.in_(urls),
                Student.document_other_url.in_(urls),
                Student.profile_photo_url.in_(urls),
            )
        ).limit(1)
    )
    student_id = res.scalar_one_or_none()
    if student_id:
        return student_id

    res = await db.execute(
        select(Certification.student_id)
        .where(Certification.file_url.in_(urls))
        .limit(1)
    )
    return res.scalar_one_or_none()


async def _student_id_for_user(db: AsyncSession, user: User) -> str | None:
    from app.db.repositories.student_repository import StudentRepository

    student = await StudentRepository(db).get_student_by_user_id(user.id)
    return student.id if student else None


async def _parent_child_ids(db: AsyncSession, user: User) -> set[str]:
    """Linked children of a parent — same linkage as ``_get_child_student``."""
    from app.db.repositories.student_repository import StudentRepository

    mappings = await StudentRepository(db).get_parent_student_map(user.id)
    return {m.student_id for m in (mappings or [])}


async def _authorize_student_owned(
    db: AsyncSession, current_user: User, owner_student_id: str | None
) -> None:
    """Allow the owning student, their linked parent, scoped faculty, or staff.

    Fails closed when ownership could not be resolved: staff only.
    """
    if current_user.role in _STAFF_ROLES:
        return
    if not owner_student_id:
        raise _not_found()

    if current_user.role == UserRole.STUDENT:
        if await _student_id_for_user(db, current_user) == owner_student_id:
            return
        raise _forbid()

    if current_user.role == UserRole.PARENT:
        if owner_student_id in await _parent_child_ids(db, current_user):
            return
        raise _forbid()

    if current_user.role == UserRole.FACULTY:
        from app.api.v1.endpoints.faculty import _faculty_permitted_student_ids

        if owner_student_id in await _faculty_permitted_student_ids(current_user, db):
            return
        raise _forbid()

    raise _forbid()


async def _authorize_student_personal(
    db: AsyncSession, current_user: User, normalized: str
) -> None:
    """Enforce ownership rules for student personal documents/photos."""
    if current_user.role in _STAFF_ROLES:
        return
    owner_id = await _owning_student_id(db, normalized)
    # Unreferenced personal file (orphaned upload): fail closed.
    await _authorize_student_owned(db, current_user, owner_id)


# ── category classifiers ───────────────────────────────────────────────────

async def _shared_by_reference(db: AsyncSession, normalized: str) -> bool:
    """Shared when the file is referenced as study material, a class-diary
    attachment, a notice attachment, or a faculty-published assignment
    attachment — all of which are meant to be read by the class."""
    from app.db.models.study_material import Assignment, StudyMaterial

    urls = _url_variants(normalized)

    res = await db.execute(select(StudyMaterial.id).where(StudyMaterial.file_url.in_(urls)).limit(1))
    if res.scalar_one_or_none():
        return True

    try:
        from app.db.models import class_diary as _cd

        diary_model = getattr(_cd, "ClassDiary", None) or getattr(_cd, "ClassDiaryEntry", None)
        if diary_model is not None:
            res = await db.execute(
                select(diary_model.id).where(diary_model.attachment_url.in_(urls)).limit(1)
            )
            if res.scalar_one_or_none():
                return True
    except Exception:  # pragma: no cover - model layout guard
        pass

    try:
        from app.db.models import communication as _comm

        notice_model = getattr(_comm, "Notice", None)
        if notice_model is not None:
            res = await db.execute(
                select(notice_model.id).where(notice_model.attachment_url.in_(urls)).limit(1)
            )
            if res.scalar_one_or_none():
                return True
    except Exception:  # pragma: no cover
        pass

    # Faculty-published assignment attachments (JSON-encoded list of urls).
    res = await db.execute(
        select(Assignment.id)
        .where(Assignment.attachments.isnot(None))
        .where(Assignment.attachments.like(f"%{os.path.basename(normalized)}%"))
        .limit(1)
    )
    if res.scalar_one_or_none():
        return True

    return False


async def _authorize_leave(db: AsyncSession, current_user: User, normalized: str) -> None:
    """Leave attachments (medical certificates).

    Readable by the applicant, the approving HOD/Principal and admin roles.
    """
    from app.db.models.leave import LeaveRequest

    if current_user.role in _STAFF_ROLES:
        return

    res = await db.execute(
        select(LeaveRequest.user_id)
        .where(LeaveRequest.attachment_url.in_(_url_variants(normalized)))
        .limit(1)
    )
    owner_user_id = res.scalar_one_or_none()
    if not owner_user_id:
        raise _not_found()
    if owner_user_id == current_user.id:
        return
    raise _forbid()


async def _authorize_hall_ticket(db: AsyncSession, current_user: User, normalized: str) -> None:
    from app.db.models.hall_ticket import HallTicket

    res = await db.execute(
        select(HallTicket.student_id)
        .where(
            or_(
                HallTicket.file_url.in_(_url_variants(normalized)),
                HallTicket.student_signature_url.in_(_url_variants(normalized)),
            )
        )
        .limit(1)
    )
    await _authorize_student_owned(db, current_user, res.scalar_one_or_none())


async def _authorize_assignment(db: AsyncSession, current_user: User, normalized: str) -> None:
    """Student submissions: submitting student, owning faculty, staff."""
    from app.db.models.study_material import Assignment, AssignmentSubmission

    if current_user.role in _STAFF_ROLES:
        return

    res = await db.execute(
        select(AssignmentSubmission.student_id, AssignmentSubmission.assignment_id)
        .where(AssignmentSubmission.submitted_file_url.in_(_url_variants(normalized)))
        .limit(1)
    )
    row = res.first()
    if row is None:
        raise _not_found()
    owner_student_id, assignment_id = row

    if current_user.role == UserRole.FACULTY:
        res = await db.execute(select(Assignment.faculty_id).where(Assignment.id == assignment_id))
        if res.scalar_one_or_none() == current_user.id:
            return
        raise _forbid()

    await _authorize_student_owned(db, current_user, owner_student_id)


def _paper_owner_name(normalized: str) -> str | None:
    """Primary author recorded for a paper upload in papers_db.json."""
    try:
        with open(_PAPERS_DB_PATH, encoding="utf-8") as fh:
            papers = json.load(fh)
    except Exception:
        return None
    basename = os.path.basename(normalized)
    for paper in papers if isinstance(papers, list) else []:
        url = paper.get("fileUrl") or ""
        if url and os.path.basename(url) == basename:
            team = paper.get("team") or []
            if team:
                return team[0]
    return None


async def _authorize_paper(db: AsyncSession, current_user: User, normalized: str) -> None:
    """Student paper submissions: the submitting student and staff.

    Papers are still stored in ``papers_db.json`` (no table yet), which records
    only the author's display name — so ownership resolves by full name.
    """
    if current_user.role in _STAFF_ROLES:
        return
    owner_name = _paper_owner_name(normalized)
    if not owner_name:
        raise _not_found()
    if current_user.role == UserRole.STUDENT and (current_user.full_name or "") == owner_name:
        return
    raise _forbid()


async def _authorize_research(db: AsyncSession, current_user: User, normalized: str) -> None:
    """Faculty research proofs: the owning faculty and staff."""
    from app.db.models.faculty import FacultyResearch
    from app.db.models.research import PublicationProof, ResearchPlan

    if current_user.role in _STAFF_ROLES:
        return

    urls = _url_variants(normalized)

    res = await db.execute(
        select(FacultyResearch.faculty_id).where(FacultyResearch.proof_file_url.in_(urls)).limit(1)
    )
    owner_id = res.scalar_one_or_none()

    if not owner_id:
        res = await db.execute(
            select(ResearchPlan.faculty_id)
            .join(PublicationProof, PublicationProof.plan_id == ResearchPlan.id)
            .where(PublicationProof.proof_file_url.in_(urls))
            .limit(1)
        )
        owner_id = res.scalar_one_or_none()

    if not owner_id:
        raise _not_found()
    if owner_id == current_user.id:
        return
    raise _forbid()


async def _authorize(db: AsyncSession, current_user: User, normalized: str, filename: str) -> None:
    """Fail-closed authorization router."""
    # 1. Genuinely shared / broadcast content.
    if normalized.startswith(_SHARED_PREFIXES) or filename.startswith(_SHARED_FILENAME_PREFIXES):
        return

    # 2. Owned categories.
    if normalized.startswith("payroll/"):
        if current_user.role in _PAYROLL_STAFF_ROLES:
            return
        if filename.startswith("salary_slip_"):
            parts = filename[len("salary_slip_"):].rsplit(".", 1)[0].split("_")
            if parts and parts[0] == current_user.id:
                return
        raise _forbid()

    if normalized.startswith("leaves/"):
        return await _authorize_leave(db, current_user, normalized)

    if normalized.startswith("hall-tickets/"):
        return await _authorize_hall_ticket(db, current_user, normalized)

    if normalized.startswith("assignments/"):
        return await _authorize_assignment(db, current_user, normalized)

    if normalized.startswith("papers/"):
        return await _authorize_paper(db, current_user, normalized)

    if normalized.startswith("research/"):
        return await _authorize_research(db, current_user, normalized)

    if _is_student_personal(normalized, filename):
        return await _authorize_student_personal(db, current_user, normalized)

    # 3. Root-level / unknown: shared only when the DB says so (study material,
    #    class diary, notice or published assignment attachment).
    if await _shared_by_reference(db, normalized):
        return

    # 4. Unclassifiable legacy upload — staff only, 404 for everyone else.
    if current_user.role in _STAFF_ROLES:
        return
    raise _not_found()


@router.get("/{file_path:path}")
async def download_file(
    file_path: str,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session),
):
    """Stream a previously uploaded file. Requires authentication.
    `file_path` may include subdirectories (e.g. 'leaves/leave_xxx.pdf')."""
    full_path = _resolve_safe_path(file_path)
    if not os.path.isfile(full_path):
        raise HTTPException(status_code=404, detail="File not found")
    filename = os.path.basename(full_path)
    normalized = os.path.normpath(file_path).replace("\\", "/")

    await _authorize(db, current_user, normalized, filename)

    media_type = mimetypes.guess_type(full_path)[0] or "application/octet-stream"
    return FileResponse(full_path, filename=filename, media_type=media_type)
