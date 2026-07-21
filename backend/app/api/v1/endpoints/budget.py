"""Institutional Budget & Grants endpoints (Principal/Admin scoped)."""
from datetime import datetime

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.dependencies import get_db_session, role_required
from app.db.models.academic import Department
from app.db.models.budget import BudgetExpense, BudgetLineItem, BudgetStatus, Grant, GrantStatus
from app.db.models.user import User, UserRole
from app.schemas.budget import (
    BudgetExpenseCreate,
    BudgetExpenseResponse,
    BudgetLineItemCreate,
    BudgetLineItemResponse,
    BudgetLineItemUpdate,
    BudgetSummaryResponse,
    GrantCreate,
    GrantResponse,
    GrantUpdate,
)

router = APIRouter()

_MANAGE = role_required([UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.SUPER_ADMIN])


async def _dept_names(db: AsyncSession) -> dict[str, str]:
    rows = (await db.execute(select(Department.id, Department.name))).all()
    return {dept_id: name for dept_id, name in rows}


def _line_item_response(item: BudgetLineItem, dept_names: dict[str, str]) -> BudgetLineItemResponse:
    allocated = float(item.allocated_amount)
    spent = float(item.spent_amount)
    return BudgetLineItemResponse(
        id=item.id,
        fiscal_year=item.fiscal_year,
        title=item.title,
        category=item.category,
        department_id=item.department_id,
        department_name=dept_names.get(item.department_id) if item.department_id else None,
        allocated_amount=allocated,
        spent_amount=spent,
        remaining_amount=allocated - spent,
        status=item.status,
        notes=item.notes,
        created_at=item.created_at.isoformat(),
    )


def _grant_response(grant: Grant, dept_names: dict[str, str]) -> GrantResponse:
    return GrantResponse(
        id=grant.id,
        title=grant.title,
        funding_agency=grant.funding_agency,
        department_id=grant.department_id,
        department_name=dept_names.get(grant.department_id) if grant.department_id else None,
        principal_investigator=grant.principal_investigator,
        sanctioned_amount=float(grant.sanctioned_amount),
        disbursed_amount=float(grant.disbursed_amount),
        status=grant.status,
        start_date=grant.start_date,
        end_date=grant.end_date,
        notes=grant.notes,
        created_at=grant.created_at.isoformat(),
    )


# ---------------- Summary ----------------

@router.get("/summary", response_model=BudgetSummaryResponse)
async def get_budget_summary(
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    items = (await db.execute(
        select(BudgetLineItem).where(BudgetLineItem.is_deleted.is_(False))
    )).scalars().all()
    grants = (await db.execute(
        select(Grant).where(Grant.is_deleted.is_(False))
    )).scalars().all()

    return BudgetSummaryResponse(
        total_allocated=sum(float(i.allocated_amount) for i in items),
        total_spent=sum(float(i.spent_amount) for i in items),
        total_remaining=sum(float(i.allocated_amount) - float(i.spent_amount) for i in items),
        total_grants_sanctioned=sum(float(g.sanctioned_amount) for g in grants),
        total_grants_disbursed=sum(float(g.disbursed_amount) for g in grants),
        active_grants_count=sum(1 for g in grants if g.status in (GrantStatus.APPROVED.value, GrantStatus.DISBURSED.value)),
    )


# ---------------- Budget line items ----------------

@router.get("/line-items", response_model=list[BudgetLineItemResponse])
async def list_line_items(
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    items = (await db.execute(
        select(BudgetLineItem).where(BudgetLineItem.is_deleted.is_(False)).order_by(BudgetLineItem.fiscal_year.desc(), BudgetLineItem.title)
    )).scalars().all()
    dept_names = await _dept_names(db)
    return [_line_item_response(i, dept_names) for i in items]


@router.post("/line-items", response_model=BudgetLineItemResponse)
async def create_line_item(
    payload: BudgetLineItemCreate,
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    item = BudgetLineItem(
        fiscal_year=payload.fiscal_year,
        title=payload.title,
        category=payload.category,
        department_id=payload.department_id,
        allocated_amount=payload.allocated_amount,
        spent_amount=0,
        status=BudgetStatus.ACTIVE.value,
        notes=payload.notes,
        created_by=current_user.id,
    )
    db.add(item)
    await db.commit()
    await db.refresh(item)
    dept_names = await _dept_names(db)
    return _line_item_response(item, dept_names)


@router.put("/line-items/{item_id}", response_model=BudgetLineItemResponse)
async def update_line_item(
    item_id: str,
    payload: BudgetLineItemUpdate,
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    item = (await db.execute(
        select(BudgetLineItem).where(BudgetLineItem.id == item_id, BudgetLineItem.is_deleted.is_(False))
    )).scalar_one_or_none()
    if not item:
        raise HTTPException(status_code=404, detail="Budget line item not found")

    if payload.title is not None:
        item.title = payload.title
    if payload.category is not None:
        item.category = payload.category
    if payload.allocated_amount is not None:
        if payload.allocated_amount < float(item.spent_amount):
            raise HTTPException(status_code=400, detail="Allocated amount cannot be less than amount already spent")
        item.allocated_amount = payload.allocated_amount
    if payload.status is not None:
        item.status = payload.status
    if payload.notes is not None:
        item.notes = payload.notes

    await db.commit()
    await db.refresh(item)
    dept_names = await _dept_names(db)
    return _line_item_response(item, dept_names)


@router.delete("/line-items/{item_id}")
async def delete_line_item(
    item_id: str,
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    item = (await db.execute(
        select(BudgetLineItem).where(BudgetLineItem.id == item_id, BudgetLineItem.is_deleted.is_(False))
    )).scalar_one_or_none()
    if not item:
        raise HTTPException(status_code=404, detail="Budget line item not found")
    if float(item.spent_amount) > 0:
        raise HTTPException(status_code=400, detail="Cannot delete a budget line item that already has recorded expenses")

    item.is_deleted = True
    item.deleted_at = datetime.utcnow()
    await db.commit()
    return {"detail": "Budget line item deleted"}


@router.post("/line-items/{item_id}/expenses", response_model=BudgetExpenseResponse)
async def record_expense(
    item_id: str,
    payload: BudgetExpenseCreate,
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    item = (await db.execute(
        select(BudgetLineItem).where(BudgetLineItem.id == item_id, BudgetLineItem.is_deleted.is_(False))
    )).scalar_one_or_none()
    if not item:
        raise HTTPException(status_code=404, detail="Budget line item not found")

    remaining = float(item.allocated_amount) - float(item.spent_amount)
    if payload.amount > remaining:
        raise HTTPException(status_code=400, detail=f"Expense exceeds remaining budget (available: {remaining:.2f})")

    expense = BudgetExpense(
        line_item_id=item_id,
        description=payload.description,
        amount=payload.amount,
        expense_date=payload.expense_date,
        recorded_by=current_user.id,
    )
    db.add(expense)
    item.spent_amount = float(item.spent_amount) + payload.amount
    await db.commit()
    await db.refresh(expense)
    return BudgetExpenseResponse(
        id=expense.id,
        line_item_id=expense.line_item_id,
        description=expense.description,
        amount=float(expense.amount),
        expense_date=expense.expense_date,
        recorded_by_name=current_user.full_name,
        created_at=expense.created_at.isoformat(),
    )


@router.get("/line-items/{item_id}/expenses", response_model=list[BudgetExpenseResponse])
async def list_expenses(
    item_id: str,
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    item = (await db.execute(
        select(BudgetLineItem).where(BudgetLineItem.id == item_id, BudgetLineItem.is_deleted.is_(False))
    )).scalar_one_or_none()
    if not item:
        raise HTTPException(status_code=404, detail="Budget line item not found")

    rows = (await db.execute(
        select(BudgetExpense, User)
        .join(User, BudgetExpense.recorded_by == User.id)
        .where(BudgetExpense.line_item_id == item_id, BudgetExpense.is_deleted.is_(False))
        .order_by(BudgetExpense.expense_date.desc())
    )).all()
    return [
        BudgetExpenseResponse(
            id=exp.id,
            line_item_id=exp.line_item_id,
            description=exp.description,
            amount=float(exp.amount),
            expense_date=exp.expense_date,
            recorded_by_name=recorder.full_name,
            created_at=exp.created_at.isoformat(),
        )
        for exp, recorder in rows
    ]


# ---------------- Grants ----------------

@router.get("/grants", response_model=list[GrantResponse])
async def list_grants(
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    grants = (await db.execute(
        select(Grant).where(Grant.is_deleted.is_(False)).order_by(Grant.created_at.desc())
    )).scalars().all()
    dept_names = await _dept_names(db)
    return [_grant_response(g, dept_names) for g in grants]


@router.post("/grants", response_model=GrantResponse)
async def create_grant(
    payload: GrantCreate,
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    grant = Grant(
        title=payload.title,
        funding_agency=payload.funding_agency,
        department_id=payload.department_id,
        principal_investigator=payload.principal_investigator,
        sanctioned_amount=payload.sanctioned_amount,
        disbursed_amount=0,
        status=GrantStatus.PROPOSED.value,
        start_date=payload.start_date,
        end_date=payload.end_date,
        notes=payload.notes,
        created_by=current_user.id,
    )
    db.add(grant)
    await db.commit()
    await db.refresh(grant)
    dept_names = await _dept_names(db)
    return _grant_response(grant, dept_names)


@router.put("/grants/{grant_id}", response_model=GrantResponse)
async def update_grant(
    grant_id: str,
    payload: GrantUpdate,
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    grant = (await db.execute(
        select(Grant).where(Grant.id == grant_id, Grant.is_deleted.is_(False))
    )).scalar_one_or_none()
    if not grant:
        raise HTTPException(status_code=404, detail="Grant not found")

    if payload.status is not None:
        grant.status = payload.status
    if payload.disbursed_amount is not None:
        if payload.disbursed_amount > float(grant.sanctioned_amount):
            raise HTTPException(status_code=400, detail="Disbursed amount cannot exceed sanctioned amount")
        grant.disbursed_amount = payload.disbursed_amount
    if payload.notes is not None:
        grant.notes = payload.notes

    await db.commit()
    await db.refresh(grant)
    dept_names = await _dept_names(db)
    return _grant_response(grant, dept_names)


@router.delete("/grants/{grant_id}")
async def delete_grant(
    grant_id: str,
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    grant = (await db.execute(
        select(Grant).where(Grant.id == grant_id, Grant.is_deleted.is_(False))
    )).scalar_one_or_none()
    if not grant:
        raise HTTPException(status_code=404, detail="Grant not found")
    if float(grant.disbursed_amount) > 0:
        raise HTTPException(status_code=400, detail="Cannot delete a grant that already has disbursed funds")

    grant.is_deleted = True
    grant.deleted_at = datetime.utcnow()
    await db.commit()
    return {"detail": "Grant deleted"}
