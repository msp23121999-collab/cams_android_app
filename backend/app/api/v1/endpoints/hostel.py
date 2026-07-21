"""Hostel management endpoints (Admin/Principal scoped).

Covers blocks, rooms and student allocations. Occupancy is always derived from
live ACTIVE allocations rather than a denormalised counter, so the numbers can
never drift out of sync with the allocation ledger.
"""
from datetime import date

from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.dependencies import get_db_session, role_required
from app.db.models.hostel import AllocationStatus, HostelAllocation, HostelBlock, HostelRoom
from app.db.models.student import Student
from app.db.models.user import User, UserRole
from app.schemas.erp import (
    HostelAllocationCreate,
    HostelAllocationResponse,
    HostelBlockCreate,
    HostelBlockResponse,
    HostelBlockUpdate,
    HostelRoomCreate,
    HostelRoomResponse,
)

router = APIRouter()

_MANAGE = role_required([UserRole.ADMIN, UserRole.SUPER_ADMIN, UserRole.PRINCIPAL])


async def _occupancy_by_room(db: AsyncSession) -> dict[str, int]:
    """Active occupant count per room, computed in one grouped query (no N+1)."""
    rows = (await db.execute(
        select(HostelAllocation.room_id, func.count(HostelAllocation.id))
        .where(
            HostelAllocation.status == AllocationStatus.ACTIVE.value,
            HostelAllocation.is_deleted.is_(False),
        )
        .group_by(HostelAllocation.room_id)
    )).all()
    return {room_id: count for room_id, count in rows}


# ---------------- Blocks ----------------

@router.get("/blocks", response_model=list[HostelBlockResponse])
async def list_blocks(
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    blocks = (await db.execute(
        select(HostelBlock).where(HostelBlock.is_deleted.is_(False)).order_by(HostelBlock.name)
    )).scalars().all()

    rooms = (await db.execute(
        select(HostelRoom).where(HostelRoom.is_deleted.is_(False))
    )).scalars().all()
    occupancy = await _occupancy_by_room(db)

    rooms_by_block: dict[str, list[HostelRoom]] = {}
    for room in rooms:
        rooms_by_block.setdefault(room.block_id, []).append(room)

    result = []
    for block in blocks:
        block_rooms = rooms_by_block.get(block.id, [])
        result.append(HostelBlockResponse(
            id=block.id,
            name=block.name,
            code=block.code,
            hostel_type=block.hostel_type,
            warden_name=block.warden_name,
            warden_phone=block.warden_phone,
            address=block.address,
            total_rooms=len(block_rooms),
            total_capacity=sum(r.capacity for r in block_rooms),
            occupied=sum(occupancy.get(r.id, 0) for r in block_rooms),
        ))
    return result


@router.post("/blocks", response_model=HostelBlockResponse, status_code=201)
async def create_block(
    payload: HostelBlockCreate,
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    existing = (await db.execute(
        select(HostelBlock).where(HostelBlock.code == payload.code, HostelBlock.is_deleted.is_(False))
    )).scalar_one_or_none()
    if existing:
        raise HTTPException(status_code=409, detail=f"A hostel block with code '{payload.code}' already exists")

    block = HostelBlock(**payload.model_dump())
    db.add(block)
    await db.commit()
    await db.refresh(block)
    return HostelBlockResponse(
        id=block.id, name=block.name, code=block.code, hostel_type=block.hostel_type,
        warden_name=block.warden_name, warden_phone=block.warden_phone, address=block.address,
    )


@router.put("/blocks/{block_id}", response_model=HostelBlockResponse)
async def update_block(
    block_id: str,
    payload: HostelBlockUpdate,
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    block = (await db.execute(
        select(HostelBlock).where(HostelBlock.id == block_id, HostelBlock.is_deleted.is_(False))
    )).scalar_one_or_none()
    if not block:
        raise HTTPException(status_code=404, detail="Hostel block not found")

    for field, value in payload.model_dump(exclude_unset=True).items():
        setattr(block, field, value)
    await db.commit()
    await db.refresh(block)
    return HostelBlockResponse(
        id=block.id, name=block.name, code=block.code, hostel_type=block.hostel_type,
        warden_name=block.warden_name, warden_phone=block.warden_phone, address=block.address,
    )


@router.delete("/blocks/{block_id}")
async def delete_block(
    block_id: str,
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    block = (await db.execute(
        select(HostelBlock).where(HostelBlock.id == block_id, HostelBlock.is_deleted.is_(False))
    )).scalar_one_or_none()
    if not block:
        raise HTTPException(status_code=404, detail="Hostel block not found")

    # Refuse while students are still allocated to rooms in this block.
    active = (await db.execute(
        select(func.count(HostelAllocation.id))
        .join(HostelRoom, HostelAllocation.room_id == HostelRoom.id)
        .where(
            HostelRoom.block_id == block_id,
            HostelAllocation.status == AllocationStatus.ACTIVE.value,
            HostelAllocation.is_deleted.is_(False),
        )
    )).scalar_one()
    if active:
        raise HTTPException(status_code=400, detail=f"Cannot delete: {active} student(s) are still allocated in this block")

    block.is_deleted = True
    await db.commit()
    return {"ok": True}


# ---------------- Rooms ----------------

@router.get("/rooms", response_model=list[HostelRoomResponse])
async def list_rooms(
    block_id: str | None = Query(default=None),
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    query = select(HostelRoom, HostelBlock).join(HostelBlock, HostelRoom.block_id == HostelBlock.id).where(
        HostelRoom.is_deleted.is_(False)
    )
    if block_id:
        query = query.where(HostelRoom.block_id == block_id)
    query = query.order_by(HostelBlock.name, HostelRoom.room_number)

    rows = (await db.execute(query)).all()
    occupancy = await _occupancy_by_room(db)
    return [
        HostelRoomResponse(
            id=room.id, block_id=room.block_id, block_name=block.name,
            room_number=room.room_number, floor=room.floor, capacity=room.capacity,
            room_type=room.room_type,
            monthly_rent=float(room.monthly_rent) if room.monthly_rent is not None else None,
            occupied=occupancy.get(room.id, 0),
            available=max(0, room.capacity - occupancy.get(room.id, 0)),
        )
        for room, block in rows
    ]


@router.post("/rooms", response_model=HostelRoomResponse, status_code=201)
async def create_room(
    payload: HostelRoomCreate,
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    block = (await db.execute(
        select(HostelBlock).where(HostelBlock.id == payload.block_id, HostelBlock.is_deleted.is_(False))
    )).scalar_one_or_none()
    if not block:
        raise HTTPException(status_code=404, detail="Hostel block not found")

    duplicate = (await db.execute(
        select(HostelRoom).where(
            HostelRoom.block_id == payload.block_id,
            HostelRoom.room_number == payload.room_number,
            HostelRoom.is_deleted.is_(False),
        )
    )).scalar_one_or_none()
    if duplicate:
        raise HTTPException(status_code=409, detail=f"Room {payload.room_number} already exists in {block.name}")

    room = HostelRoom(**payload.model_dump())
    db.add(room)
    await db.commit()
    await db.refresh(room)
    return HostelRoomResponse(
        id=room.id, block_id=room.block_id, block_name=block.name, room_number=room.room_number,
        floor=room.floor, capacity=room.capacity, room_type=room.room_type,
        monthly_rent=float(room.monthly_rent) if room.monthly_rent is not None else None,
        occupied=0, available=room.capacity,
    )


@router.delete("/rooms/{room_id}")
async def delete_room(
    room_id: str,
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    room = (await db.execute(
        select(HostelRoom).where(HostelRoom.id == room_id, HostelRoom.is_deleted.is_(False))
    )).scalar_one_or_none()
    if not room:
        raise HTTPException(status_code=404, detail="Room not found")

    active = (await db.execute(
        select(func.count(HostelAllocation.id)).where(
            HostelAllocation.room_id == room_id,
            HostelAllocation.status == AllocationStatus.ACTIVE.value,
            HostelAllocation.is_deleted.is_(False),
        )
    )).scalar_one()
    if active:
        raise HTTPException(status_code=400, detail=f"Cannot delete: {active} student(s) are still allocated to this room")

    room.is_deleted = True
    await db.commit()
    return {"ok": True}


# ---------------- Allocations ----------------

@router.get("/allocations", response_model=list[HostelAllocationResponse])
async def list_allocations(
    status: str | None = Query(default=None, description="ACTIVE or VACATED"),
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    query = (
        select(HostelAllocation, HostelRoom, HostelBlock, Student, User)
        .join(HostelRoom, HostelAllocation.room_id == HostelRoom.id)
        .join(HostelBlock, HostelRoom.block_id == HostelBlock.id)
        .join(Student, HostelAllocation.student_id == Student.id)
        .join(User, Student.user_id == User.id)
        .where(HostelAllocation.is_deleted.is_(False))
    )
    if status:
        query = query.where(HostelAllocation.status == status.upper())
    query = query.order_by(HostelAllocation.allocated_on.desc())

    rows = (await db.execute(query)).all()
    return [
        HostelAllocationResponse(
            id=alloc.id, room_id=alloc.room_id, room_number=room.room_number, block_name=block.name,
            student_id=alloc.student_id, student_name=user.full_name, roll_no=student.roll_no,
            allocated_on=alloc.allocated_on, vacated_on=alloc.vacated_on,
            status=alloc.status, remarks=alloc.remarks,
        )
        for alloc, room, block, student, user in rows
    ]


@router.post("/allocations", response_model=HostelAllocationResponse, status_code=201)
async def allocate_room(
    payload: HostelAllocationCreate,
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    room = (await db.execute(
        select(HostelRoom).where(HostelRoom.id == payload.room_id, HostelRoom.is_deleted.is_(False))
    )).scalar_one_or_none()
    if not room:
        raise HTTPException(status_code=404, detail="Room not found")

    student = (await db.execute(
        select(Student).where(Student.id == payload.student_id, Student.is_deleted.is_(False))
    )).scalar_one_or_none()
    if not student:
        raise HTTPException(status_code=404, detail="Student not found")

    # A student may only hold one active allocation at a time.
    existing = (await db.execute(
        select(HostelAllocation).where(
            HostelAllocation.student_id == payload.student_id,
            HostelAllocation.status == AllocationStatus.ACTIVE.value,
            HostelAllocation.is_deleted.is_(False),
        )
    )).scalar_one_or_none()
    if existing:
        raise HTTPException(status_code=409, detail="This student already has an active hostel allocation")

    occupancy = await _occupancy_by_room(db)
    if occupancy.get(room.id, 0) >= room.capacity:
        raise HTTPException(status_code=400, detail=f"Room {room.room_number} is already full ({room.capacity}/{room.capacity})")

    alloc = HostelAllocation(
        room_id=payload.room_id,
        student_id=payload.student_id,
        allocated_on=payload.allocated_on or date.today(),
        status=AllocationStatus.ACTIVE.value,
        remarks=payload.remarks,
    )
    db.add(alloc)
    await db.commit()
    await db.refresh(alloc)

    block = await db.get(HostelBlock, room.block_id)
    user = await db.get(User, student.user_id)
    return HostelAllocationResponse(
        id=alloc.id, room_id=alloc.room_id, room_number=room.room_number,
        block_name=block.name if block else None, student_id=alloc.student_id,
        student_name=user.full_name if user else None, roll_no=student.roll_no,
        allocated_on=alloc.allocated_on, vacated_on=None, status=alloc.status, remarks=alloc.remarks,
    )


@router.post("/allocations/{allocation_id}/vacate")
async def vacate_allocation(
    allocation_id: str,
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    alloc = (await db.execute(
        select(HostelAllocation).where(HostelAllocation.id == allocation_id, HostelAllocation.is_deleted.is_(False))
    )).scalar_one_or_none()
    if not alloc:
        raise HTTPException(status_code=404, detail="Allocation not found")
    if alloc.status == AllocationStatus.VACATED.value:
        raise HTTPException(status_code=400, detail="This allocation is already vacated")

    alloc.status = AllocationStatus.VACATED.value
    alloc.vacated_on = date.today()
    await db.commit()
    return {"ok": True, "vacated_on": alloc.vacated_on.isoformat()}
