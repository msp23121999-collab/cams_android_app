"""Transport management endpoints (Admin/Principal scoped).

Covers routes, the vehicle fleet, and student bus passes.
"""
from datetime import date, timedelta

from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.dependencies import get_db_session, role_required
from app.db.models.student import Student
from app.db.models.transport import (
    PassStatus,
    TransportPass,
    TransportRoute,
    TransportVehicle,
    VehicleStatus,
)
from app.db.models.user import User, UserRole
from app.schemas.erp import (
    TransportPassCreate,
    TransportPassResponse,
    TransportRouteCreate,
    TransportRouteResponse,
    TransportRouteUpdate,
    TransportVehicleCreate,
    TransportVehicleResponse,
    TransportVehicleUpdate,
)

router = APIRouter()

_MANAGE = role_required([UserRole.ADMIN, UserRole.SUPER_ADMIN, UserRole.PRINCIPAL])

DEFAULT_PASS_DAYS = 180


# ---------------- Routes ----------------

@router.get("/routes", response_model=list[TransportRouteResponse])
async def list_routes(
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    routes = (await db.execute(
        select(TransportRoute).where(TransportRoute.is_deleted.is_(False)).order_by(TransportRoute.name)
    )).scalars().all()

    # Counts resolved in two grouped queries rather than per-route lookups.
    vehicle_counts = dict((await db.execute(
        select(TransportVehicle.route_id, func.count(TransportVehicle.id))
        .where(TransportVehicle.is_deleted.is_(False))
        .group_by(TransportVehicle.route_id)
    )).all())
    pass_counts = dict((await db.execute(
        select(TransportPass.route_id, func.count(TransportPass.id))
        .where(TransportPass.is_deleted.is_(False), TransportPass.status == PassStatus.ACTIVE.value)
        .group_by(TransportPass.route_id)
    )).all())

    return [
        TransportRouteResponse(
            id=r.id, name=r.name, code=r.code, start_point=r.start_point, end_point=r.end_point,
            distance_km=float(r.distance_km) if r.distance_km is not None else None,
            fare=float(r.fare) if r.fare is not None else None,
            stops=r.stops,
            vehicle_count=vehicle_counts.get(r.id, 0),
            pass_count=pass_counts.get(r.id, 0),
        )
        for r in routes
    ]


@router.post("/routes", response_model=TransportRouteResponse, status_code=201)
async def create_route(
    payload: TransportRouteCreate,
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    existing = (await db.execute(
        select(TransportRoute).where(TransportRoute.code == payload.code, TransportRoute.is_deleted.is_(False))
    )).scalar_one_or_none()
    if existing:
        raise HTTPException(status_code=409, detail=f"A route with code '{payload.code}' already exists")

    route = TransportRoute(**payload.model_dump())
    db.add(route)
    await db.commit()
    await db.refresh(route)
    return TransportRouteResponse(
        id=route.id, name=route.name, code=route.code, start_point=route.start_point,
        end_point=route.end_point,
        distance_km=float(route.distance_km) if route.distance_km is not None else None,
        fare=float(route.fare) if route.fare is not None else None,
        stops=route.stops,
    )


@router.put("/routes/{route_id}", response_model=TransportRouteResponse)
async def update_route(
    route_id: str,
    payload: TransportRouteUpdate,
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    route = (await db.execute(
        select(TransportRoute).where(TransportRoute.id == route_id, TransportRoute.is_deleted.is_(False))
    )).scalar_one_or_none()
    if not route:
        raise HTTPException(status_code=404, detail="Route not found")

    for field, value in payload.model_dump(exclude_unset=True).items():
        setattr(route, field, value)
    await db.commit()
    await db.refresh(route)
    return TransportRouteResponse(
        id=route.id, name=route.name, code=route.code, start_point=route.start_point,
        end_point=route.end_point,
        distance_km=float(route.distance_km) if route.distance_km is not None else None,
        fare=float(route.fare) if route.fare is not None else None,
        stops=route.stops,
    )


@router.delete("/routes/{route_id}")
async def delete_route(
    route_id: str,
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    route = (await db.execute(
        select(TransportRoute).where(TransportRoute.id == route_id, TransportRoute.is_deleted.is_(False))
    )).scalar_one_or_none()
    if not route:
        raise HTTPException(status_code=404, detail="Route not found")

    active_passes = (await db.execute(
        select(func.count(TransportPass.id)).where(
            TransportPass.route_id == route_id,
            TransportPass.status == PassStatus.ACTIVE.value,
            TransportPass.is_deleted.is_(False),
        )
    )).scalar_one()
    if active_passes:
        raise HTTPException(status_code=400, detail=f"Cannot delete: {active_passes} active pass(es) use this route")

    # Detach vehicles so they aren't left pointing at a deleted route.
    vehicles = (await db.execute(
        select(TransportVehicle).where(TransportVehicle.route_id == route_id, TransportVehicle.is_deleted.is_(False))
    )).scalars().all()
    for v in vehicles:
        v.route_id = None

    route.is_deleted = True
    await db.commit()
    return {"ok": True}


# ---------------- Vehicles ----------------

@router.get("/vehicles", response_model=list[TransportVehicleResponse])
async def list_vehicles(
    route_id: str | None = Query(default=None),
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    query = (
        select(TransportVehicle, TransportRoute)
        .outerjoin(TransportRoute, TransportVehicle.route_id == TransportRoute.id)
        .where(TransportVehicle.is_deleted.is_(False))
    )
    if route_id:
        query = query.where(TransportVehicle.route_id == route_id)
    query = query.order_by(TransportVehicle.registration_no)

    rows = (await db.execute(query)).all()
    return [
        TransportVehicleResponse(
            id=v.id, registration_no=v.registration_no, vehicle_type=v.vehicle_type,
            capacity=v.capacity, driver_name=v.driver_name, driver_phone=v.driver_phone,
            route_id=v.route_id, route_name=r.name if r else None, status=v.status,
        )
        for v, r in rows
    ]


@router.post("/vehicles", response_model=TransportVehicleResponse, status_code=201)
async def create_vehicle(
    payload: TransportVehicleCreate,
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    existing = (await db.execute(
        select(TransportVehicle).where(
            TransportVehicle.registration_no == payload.registration_no,
            TransportVehicle.is_deleted.is_(False),
        )
    )).scalar_one_or_none()
    if existing:
        raise HTTPException(status_code=409, detail=f"Vehicle '{payload.registration_no}' is already registered")

    if payload.status.upper() not in {s.value for s in VehicleStatus}:
        raise HTTPException(status_code=400, detail="Invalid vehicle status")

    if payload.route_id:
        route = (await db.execute(
            select(TransportRoute).where(TransportRoute.id == payload.route_id, TransportRoute.is_deleted.is_(False))
        )).scalar_one_or_none()
        if not route:
            raise HTTPException(status_code=404, detail="Route not found")

    data = payload.model_dump()
    data["status"] = data["status"].upper()
    vehicle = TransportVehicle(**data)
    db.add(vehicle)
    await db.commit()
    await db.refresh(vehicle)

    route = await db.get(TransportRoute, vehicle.route_id) if vehicle.route_id else None
    return TransportVehicleResponse(
        id=vehicle.id, registration_no=vehicle.registration_no, vehicle_type=vehicle.vehicle_type,
        capacity=vehicle.capacity, driver_name=vehicle.driver_name, driver_phone=vehicle.driver_phone,
        route_id=vehicle.route_id, route_name=route.name if route else None, status=vehicle.status,
    )


@router.put("/vehicles/{vehicle_id}", response_model=TransportVehicleResponse)
async def update_vehicle(
    vehicle_id: str,
    payload: TransportVehicleUpdate,
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    vehicle = (await db.execute(
        select(TransportVehicle).where(TransportVehicle.id == vehicle_id, TransportVehicle.is_deleted.is_(False))
    )).scalar_one_or_none()
    if not vehicle:
        raise HTTPException(status_code=404, detail="Vehicle not found")

    updates = payload.model_dump(exclude_unset=True)
    if updates.get("status"):
        if updates["status"].upper() not in {s.value for s in VehicleStatus}:
            raise HTTPException(status_code=400, detail="Invalid vehicle status")
        updates["status"] = updates["status"].upper()

    for field, value in updates.items():
        setattr(vehicle, field, value)
    await db.commit()
    await db.refresh(vehicle)

    route = await db.get(TransportRoute, vehicle.route_id) if vehicle.route_id else None
    return TransportVehicleResponse(
        id=vehicle.id, registration_no=vehicle.registration_no, vehicle_type=vehicle.vehicle_type,
        capacity=vehicle.capacity, driver_name=vehicle.driver_name, driver_phone=vehicle.driver_phone,
        route_id=vehicle.route_id, route_name=route.name if route else None, status=vehicle.status,
    )


@router.delete("/vehicles/{vehicle_id}")
async def delete_vehicle(
    vehicle_id: str,
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    vehicle = (await db.execute(
        select(TransportVehicle).where(TransportVehicle.id == vehicle_id, TransportVehicle.is_deleted.is_(False))
    )).scalar_one_or_none()
    if not vehicle:
        raise HTTPException(status_code=404, detail="Vehicle not found")
    vehicle.is_deleted = True
    await db.commit()
    return {"ok": True}


# ---------------- Passes ----------------

@router.get("/passes", response_model=list[TransportPassResponse])
async def list_passes(
    route_id: str | None = Query(default=None),
    status: str | None = Query(default=None),
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    query = (
        select(TransportPass, TransportRoute, Student, User)
        .join(TransportRoute, TransportPass.route_id == TransportRoute.id)
        .join(Student, TransportPass.student_id == Student.id)
        .join(User, Student.user_id == User.id)
        .where(TransportPass.is_deleted.is_(False))
    )
    if route_id:
        query = query.where(TransportPass.route_id == route_id)
    if status:
        query = query.where(TransportPass.status == status.upper())
    query = query.order_by(TransportPass.valid_from.desc())

    rows = (await db.execute(query)).all()
    return [
        TransportPassResponse(
            id=p.id, route_id=p.route_id, route_name=route.name,
            student_id=p.student_id, student_name=user.full_name, roll_no=student.roll_no,
            pickup_point=p.pickup_point, valid_from=p.valid_from, valid_to=p.valid_to,
            fare_paid=float(p.fare_paid) if p.fare_paid is not None else None,
            status=p.status,
        )
        for p, route, student, user in rows
    ]


@router.post("/passes", response_model=TransportPassResponse, status_code=201)
async def issue_pass(
    payload: TransportPassCreate,
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    route = (await db.execute(
        select(TransportRoute).where(TransportRoute.id == payload.route_id, TransportRoute.is_deleted.is_(False))
    )).scalar_one_or_none()
    if not route:
        raise HTTPException(status_code=404, detail="Route not found")

    student = (await db.execute(
        select(Student).where(Student.id == payload.student_id, Student.is_deleted.is_(False))
    )).scalar_one_or_none()
    if not student:
        raise HTTPException(status_code=404, detail="Student not found")

    existing = (await db.execute(
        select(TransportPass).where(
            TransportPass.student_id == payload.student_id,
            TransportPass.status == PassStatus.ACTIVE.value,
            TransportPass.is_deleted.is_(False),
        )
    )).scalar_one_or_none()
    if existing:
        raise HTTPException(status_code=409, detail="This student already holds an active transport pass")

    valid_from = payload.valid_from or date.today()
    valid_to = payload.valid_to or (valid_from + timedelta(days=DEFAULT_PASS_DAYS))
    if valid_to < valid_from:
        raise HTTPException(status_code=400, detail="Pass end date cannot be before its start date")

    bus_pass = TransportPass(
        route_id=route.id, student_id=student.id, pickup_point=payload.pickup_point,
        valid_from=valid_from, valid_to=valid_to,
        fare_paid=payload.fare_paid if payload.fare_paid is not None else route.fare,
        status=PassStatus.ACTIVE.value,
    )
    db.add(bus_pass)
    await db.commit()
    await db.refresh(bus_pass)

    user = await db.get(User, student.user_id)
    return TransportPassResponse(
        id=bus_pass.id, route_id=bus_pass.route_id, route_name=route.name,
        student_id=bus_pass.student_id, student_name=user.full_name if user else None,
        roll_no=student.roll_no, pickup_point=bus_pass.pickup_point,
        valid_from=bus_pass.valid_from, valid_to=bus_pass.valid_to,
        fare_paid=float(bus_pass.fare_paid) if bus_pass.fare_paid is not None else None,
        status=bus_pass.status,
    )


@router.post("/passes/{pass_id}/cancel")
async def cancel_pass(
    pass_id: str,
    current_user: User = Depends(_MANAGE),
    db: AsyncSession = Depends(get_db_session),
):
    bus_pass = (await db.execute(
        select(TransportPass).where(TransportPass.id == pass_id, TransportPass.is_deleted.is_(False))
    )).scalar_one_or_none()
    if not bus_pass:
        raise HTTPException(status_code=404, detail="Pass not found")
    if bus_pass.status != PassStatus.ACTIVE.value:
        raise HTTPException(status_code=400, detail="Only an active pass can be cancelled")

    bus_pass.status = PassStatus.CANCELLED.value
    await db.commit()
    return {"ok": True}
