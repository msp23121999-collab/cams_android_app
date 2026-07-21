"""Inventory management endpoints (Admin/Principal scoped).

Stock levels are only ever changed through the movement endpoint, which writes
an auditable ledger row alongside the new quantity.
"""
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.dependencies import get_db_session, role_required
from app.db.models.inventory import InventoryItem, InventoryTransaction, StockMovement
from app.db.models.user import User, UserRole
from app.schemas.erp import (
    InventoryItemCreate,
    InventoryItemResponse,
    InventoryItemUpdate,
    InventoryTransactionResponse,
    StockMovementRequest,
)

router = APIRouter()

_MANAGE = role_required([UserRole.ADMIN, UserRole.SUPER_ADMIN, UserRole.PRINCIPAL])


def _to_item_response(item: InventoryItem) -> InventoryItemResponse:
    return InventoryItemResponse(
        id=item.id,
        name=item.name,
        code=item.code,
        category=item.category,
        unit=item.unit,
        quantity=item.quantity,
        min_quantity=item.min_quantity,
        unit_price=float(item.unit_price) if item.unit_price is not None else None,
        location=item.location,
        supplier=item.supplier,
        is_low_stock=item.quantity <= item.min_quantity,
    )


@router.get("/items", response_model=list[InventoryItemResponse])
async def list_items(
    category: str | None = Query(default=None),
    low_stock_only: bool = Query(default=False),
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    query = select(InventoryItem).where(InventoryItem.is_deleted.is_(False))
    if category:
        query = query.where(InventoryItem.category == category)
    query = query.order_by(InventoryItem.name)

    items = (await db.execute(query)).scalars().all()
    responses = [_to_item_response(i) for i in items]
    if low_stock_only:
        responses = [r for r in responses if r.is_low_stock]
    return responses


@router.post("/items", response_model=InventoryItemResponse, status_code=201)
async def create_item(
    payload: InventoryItemCreate,
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    existing = (await db.execute(
        select(InventoryItem).where(InventoryItem.code == payload.code, InventoryItem.is_deleted.is_(False))
    )).scalar_one_or_none()
    if existing:
        raise HTTPException(status_code=409, detail=f"An item with code '{payload.code}' already exists")

    item = InventoryItem(**payload.model_dump())
    db.add(item)
    await db.flush()

    # Opening stock is recorded as an IN movement so the ledger always explains
    # how the current quantity was reached.
    if item.quantity:
        db.add(InventoryTransaction(
            item_id=item.id,
            movement=StockMovement.IN.value,
            quantity=item.quantity,
            resulting_quantity=item.quantity,
            reason="Opening stock",
            performed_by=current_user.id,
        ))

    await db.commit()
    await db.refresh(item)
    return _to_item_response(item)


@router.put("/items/{item_id}", response_model=InventoryItemResponse)
async def update_item(
    item_id: str,
    payload: InventoryItemUpdate,
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    item = (await db.execute(
        select(InventoryItem).where(InventoryItem.id == item_id, InventoryItem.is_deleted.is_(False))
    )).scalar_one_or_none()
    if not item:
        raise HTTPException(status_code=404, detail="Item not found")

    for field, value in payload.model_dump(exclude_unset=True).items():
        setattr(item, field, value)
    await db.commit()
    await db.refresh(item)
    return _to_item_response(item)


@router.delete("/items/{item_id}")
async def delete_item(
    item_id: str,
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    item = (await db.execute(
        select(InventoryItem).where(InventoryItem.id == item_id, InventoryItem.is_deleted.is_(False))
    )).scalar_one_or_none()
    if not item:
        raise HTTPException(status_code=404, detail="Item not found")
    item.is_deleted = True
    await db.commit()
    return {"ok": True}


@router.post("/items/{item_id}/movement", response_model=InventoryItemResponse)
async def record_movement(
    item_id: str,
    payload: StockMovementRequest,
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    item = (await db.execute(
        select(InventoryItem).where(InventoryItem.id == item_id, InventoryItem.is_deleted.is_(False))
    )).scalar_one_or_none()
    if not item:
        raise HTTPException(status_code=404, detail="Item not found")

    movement = payload.movement.upper()
    valid = {m.value for m in StockMovement}
    if movement not in valid:
        raise HTTPException(status_code=400, detail=f"movement must be one of {sorted(valid)}")

    if movement == StockMovement.IN.value:
        new_quantity = item.quantity + payload.quantity
    elif movement == StockMovement.OUT.value:
        if payload.quantity > item.quantity:
            raise HTTPException(
                status_code=400,
                detail=f"Cannot issue {payload.quantity} {item.unit} — only {item.quantity} in stock",
            )
        new_quantity = item.quantity - payload.quantity
    else:  # ADJUST sets an absolute corrected level
        new_quantity = payload.quantity

    item.quantity = new_quantity
    db.add(InventoryTransaction(
        item_id=item.id,
        movement=movement,
        quantity=payload.quantity,
        resulting_quantity=new_quantity,
        reason=payload.reason,
        performed_by=current_user.id,
    ))
    await db.commit()
    await db.refresh(item)
    return _to_item_response(item)


@router.get("/transactions", response_model=list[InventoryTransactionResponse])
async def list_transactions(
    item_id: str | None = Query(default=None),
    limit: int = Query(default=100, le=500),
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    query = (
        select(InventoryTransaction, InventoryItem, User)
        .join(InventoryItem, InventoryTransaction.item_id == InventoryItem.id)
        .outerjoin(User, InventoryTransaction.performed_by == User.id)
        .where(InventoryTransaction.is_deleted.is_(False))
    )
    if item_id:
        query = query.where(InventoryTransaction.item_id == item_id)
    query = query.order_by(InventoryTransaction.created_at.desc()).limit(limit)

    rows = (await db.execute(query)).all()
    return [
        InventoryTransactionResponse(
            id=txn.id,
            item_id=txn.item_id,
            item_name=item.name,
            movement=txn.movement,
            quantity=txn.quantity,
            resulting_quantity=txn.resulting_quantity,
            reason=txn.reason,
            performed_by_name=user.full_name if user else None,
            created_at=txn.created_at.isoformat() if txn.created_at else None,
        )
        for txn, item, user in rows
    ]
