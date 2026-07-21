"""Library management endpoints (Admin/Principal scoped).

Available-copy counts are maintained transactionally alongside issue/return so
a book can never be issued beyond its available stock.
"""
from datetime import date, timedelta

from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.dependencies import get_db_session, role_required
from app.db.models.library import IssueStatus, LibraryBook, LibraryIssue
from app.db.models.user import User, UserRole
from app.schemas.erp import (
    LibraryBookCreate,
    LibraryBookResponse,
    LibraryBookUpdate,
    LibraryIssueCreate,
    LibraryIssueResponse,
    LibraryReturnRequest,
)

router = APIRouter()

_MANAGE = role_required([UserRole.ADMIN, UserRole.SUPER_ADMIN, UserRole.PRINCIPAL])

DEFAULT_LOAN_DAYS = 14


def _to_book_response(book: LibraryBook) -> LibraryBookResponse:
    return LibraryBookResponse(
        id=book.id, title=book.title, author=book.author, accession_no=book.accession_no,
        isbn=book.isbn, category=book.category, publisher=book.publisher,
        published_year=book.published_year, shelf_location=book.shelf_location,
        total_copies=book.total_copies, available_copies=book.available_copies,
    )


# ---------------- Books ----------------

@router.get("/books", response_model=list[LibraryBookResponse])
async def list_books(
    search: str | None = Query(default=None),
    category: str | None = Query(default=None),
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    query = select(LibraryBook).where(LibraryBook.is_deleted.is_(False))
    if search:
        pattern = f"%{search}%"
        query = query.where(LibraryBook.title.ilike(pattern) | LibraryBook.author.ilike(pattern))
    if category:
        query = query.where(LibraryBook.category == category)
    query = query.order_by(LibraryBook.title)

    books = (await db.execute(query)).scalars().all()
    return [_to_book_response(b) for b in books]


@router.post("/books", response_model=LibraryBookResponse, status_code=201)
async def create_book(
    payload: LibraryBookCreate,
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    existing = (await db.execute(
        select(LibraryBook).where(
            LibraryBook.accession_no == payload.accession_no, LibraryBook.is_deleted.is_(False)
        )
    )).scalar_one_or_none()
    if existing:
        raise HTTPException(status_code=409, detail=f"Accession number '{payload.accession_no}' is already in use")

    data = payload.model_dump()
    book = LibraryBook(**data, available_copies=data["total_copies"])
    db.add(book)
    await db.commit()
    await db.refresh(book)
    return _to_book_response(book)


@router.put("/books/{book_id}", response_model=LibraryBookResponse)
async def update_book(
    book_id: str,
    payload: LibraryBookUpdate,
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    book = (await db.execute(
        select(LibraryBook).where(LibraryBook.id == book_id, LibraryBook.is_deleted.is_(False))
    )).scalar_one_or_none()
    if not book:
        raise HTTPException(status_code=404, detail="Book not found")

    updates = payload.model_dump(exclude_unset=True)
    if "total_copies" in updates and updates["total_copies"] is not None:
        issued = book.total_copies - book.available_copies
        if updates["total_copies"] < issued:
            raise HTTPException(
                status_code=400,
                detail=f"Cannot set total copies below {issued} — that many are currently issued",
            )
        # Keep availability consistent with the new total.
        book.available_copies = updates["total_copies"] - issued

    for field, value in updates.items():
        setattr(book, field, value)
    await db.commit()
    await db.refresh(book)
    return _to_book_response(book)


@router.delete("/books/{book_id}")
async def delete_book(
    book_id: str,
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    book = (await db.execute(
        select(LibraryBook).where(LibraryBook.id == book_id, LibraryBook.is_deleted.is_(False))
    )).scalar_one_or_none()
    if not book:
        raise HTTPException(status_code=404, detail="Book not found")
    if book.available_copies < book.total_copies:
        raise HTTPException(status_code=400, detail="Cannot delete: some copies are still issued")

    book.is_deleted = True
    await db.commit()
    return {"ok": True}


# ---------------- Issues ----------------

@router.get("/issues", response_model=list[LibraryIssueResponse])
async def list_issues(
    status: str | None = Query(default=None, description="ISSUED or RETURNED"),
    member_id: str | None = Query(default=None),
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    query = (
        select(LibraryIssue, LibraryBook, User)
        .join(LibraryBook, LibraryIssue.book_id == LibraryBook.id)
        .join(User, LibraryIssue.member_id == User.id)
        .where(LibraryIssue.is_deleted.is_(False))
    )
    if status:
        query = query.where(LibraryIssue.status == status.upper())
    if member_id:
        query = query.where(LibraryIssue.member_id == member_id)
    query = query.order_by(LibraryIssue.issued_on.desc())

    rows = (await db.execute(query)).all()
    today = date.today()
    return [
        LibraryIssueResponse(
            id=issue.id, book_id=issue.book_id, book_title=book.title,
            member_id=issue.member_id, member_name=user.full_name,
            issued_on=issue.issued_on, due_on=issue.due_on, returned_on=issue.returned_on,
            fine_amount=float(issue.fine_amount or 0), status=issue.status,
            is_overdue=(issue.returned_on is None and issue.due_on < today),
            remarks=issue.remarks,
        )
        for issue, book, user in rows
    ]


@router.post("/issues", response_model=LibraryIssueResponse, status_code=201)
async def issue_book(
    payload: LibraryIssueCreate,
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    book = (await db.execute(
        select(LibraryBook).where(LibraryBook.id == payload.book_id, LibraryBook.is_deleted.is_(False))
    )).scalar_one_or_none()
    if not book:
        raise HTTPException(status_code=404, detail="Book not found")
    if book.available_copies <= 0:
        raise HTTPException(status_code=400, detail=f"No copies of '{book.title}' are currently available")

    member = (await db.execute(
        select(User).where(User.id == payload.member_id, User.is_deleted.is_(False))
    )).scalar_one_or_none()
    if not member:
        raise HTTPException(status_code=404, detail="Member not found")

    issued_on = payload.issued_on or date.today()
    due_on = payload.due_on or (issued_on + timedelta(days=DEFAULT_LOAN_DAYS))
    if due_on < issued_on:
        raise HTTPException(status_code=400, detail="Due date cannot be before the issue date")

    issue = LibraryIssue(
        book_id=book.id, member_id=member.id, issued_on=issued_on, due_on=due_on,
        status=IssueStatus.ISSUED.value, fine_amount=0,
    )
    book.available_copies -= 1
    db.add(issue)
    await db.commit()
    await db.refresh(issue)

    return LibraryIssueResponse(
        id=issue.id, book_id=issue.book_id, book_title=book.title,
        member_id=issue.member_id, member_name=member.full_name,
        issued_on=issue.issued_on, due_on=issue.due_on, returned_on=None,
        fine_amount=0, status=issue.status, is_overdue=False, remarks=None,
    )


@router.post("/issues/{issue_id}/return", response_model=LibraryIssueResponse)
async def return_book(
    issue_id: str,
    payload: LibraryReturnRequest,
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    issue = (await db.execute(
        select(LibraryIssue).where(LibraryIssue.id == issue_id, LibraryIssue.is_deleted.is_(False))
    )).scalar_one_or_none()
    if not issue:
        raise HTTPException(status_code=404, detail="Issue record not found")
    if issue.returned_on is not None:
        raise HTTPException(status_code=400, detail="This book has already been returned")

    book = await db.get(LibraryBook, issue.book_id)
    if not book:
        raise HTTPException(status_code=404, detail="Book not found")

    returned_on = payload.returned_on or date.today()
    if returned_on < issue.issued_on:
        raise HTTPException(status_code=400, detail="Return date cannot be before the issue date")

    issue.returned_on = returned_on
    issue.status = IssueStatus.RETURNED.value
    if payload.fine_amount is not None:
        issue.fine_amount = payload.fine_amount
    if payload.remarks:
        issue.remarks = payload.remarks

    # Never exceed the catalogued total, even if data was edited meanwhile.
    book.available_copies = min(book.total_copies, book.available_copies + 1)

    await db.commit()
    await db.refresh(issue)

    member = await db.get(User, issue.member_id)
    return LibraryIssueResponse(
        id=issue.id, book_id=issue.book_id, book_title=book.title,
        member_id=issue.member_id, member_name=member.full_name if member else None,
        issued_on=issue.issued_on, due_on=issue.due_on, returned_on=issue.returned_on,
        fine_amount=float(issue.fine_amount or 0), status=issue.status,
        is_overdue=False, remarks=issue.remarks,
    )
