"""Pydantic schemas for the Hostel / Inventory / Library / Transport ERP modules."""
from datetime import date
from typing import Optional

from pydantic import BaseModel, Field


# ---------------- Hostel ----------------

class HostelBlockCreate(BaseModel):
    name: str = Field(min_length=1, max_length=128)
    code: str = Field(min_length=1, max_length=32)
    hostel_type: str = "BOYS"
    warden_name: Optional[str] = None
    warden_phone: Optional[str] = None
    address: Optional[str] = None


class HostelBlockUpdate(BaseModel):
    name: Optional[str] = None
    hostel_type: Optional[str] = None
    warden_name: Optional[str] = None
    warden_phone: Optional[str] = None
    address: Optional[str] = None


class HostelBlockResponse(BaseModel):
    id: str
    name: str
    code: str
    hostel_type: str
    warden_name: Optional[str] = None
    warden_phone: Optional[str] = None
    address: Optional[str] = None
    total_rooms: int = 0
    total_capacity: int = 0
    occupied: int = 0


class HostelRoomCreate(BaseModel):
    block_id: str
    room_number: str = Field(min_length=1, max_length=32)
    floor: int = 0
    capacity: int = Field(default=2, ge=1)
    room_type: Optional[str] = None
    monthly_rent: Optional[float] = None


class HostelRoomResponse(BaseModel):
    id: str
    block_id: str
    block_name: Optional[str] = None
    room_number: str
    floor: int
    capacity: int
    room_type: Optional[str] = None
    monthly_rent: Optional[float] = None
    occupied: int = 0
    available: int = 0


class HostelAllocationCreate(BaseModel):
    room_id: str
    student_id: str
    allocated_on: Optional[date] = None
    remarks: Optional[str] = None


class HostelAllocationResponse(BaseModel):
    id: str
    room_id: str
    room_number: Optional[str] = None
    block_name: Optional[str] = None
    student_id: str
    student_name: Optional[str] = None
    roll_no: Optional[str] = None
    allocated_on: date
    vacated_on: Optional[date] = None
    status: str
    remarks: Optional[str] = None


# ---------------- Inventory ----------------

class InventoryItemCreate(BaseModel):
    name: str = Field(min_length=1, max_length=255)
    code: str = Field(min_length=1, max_length=64)
    category: Optional[str] = None
    unit: str = "pcs"
    quantity: int = Field(default=0, ge=0)
    min_quantity: int = Field(default=0, ge=0)
    unit_price: Optional[float] = None
    location: Optional[str] = None
    supplier: Optional[str] = None


class InventoryItemUpdate(BaseModel):
    name: Optional[str] = None
    category: Optional[str] = None
    unit: Optional[str] = None
    min_quantity: Optional[int] = None
    unit_price: Optional[float] = None
    location: Optional[str] = None
    supplier: Optional[str] = None


class InventoryItemResponse(BaseModel):
    id: str
    name: str
    code: str
    category: Optional[str] = None
    unit: str
    quantity: int
    min_quantity: int
    unit_price: Optional[float] = None
    location: Optional[str] = None
    supplier: Optional[str] = None
    is_low_stock: bool = False


class StockMovementRequest(BaseModel):
    movement: str = Field(description="IN, OUT or ADJUST")
    quantity: int = Field(gt=0)
    reason: Optional[str] = None


class InventoryTransactionResponse(BaseModel):
    id: str
    item_id: str
    item_name: Optional[str] = None
    movement: str
    quantity: int
    resulting_quantity: int
    reason: Optional[str] = None
    performed_by_name: Optional[str] = None
    created_at: Optional[str] = None


# ---------------- Library ----------------

class LibraryBookCreate(BaseModel):
    title: str = Field(min_length=1, max_length=512)
    accession_no: str = Field(min_length=1, max_length=64)
    author: Optional[str] = None
    isbn: Optional[str] = None
    category: Optional[str] = None
    publisher: Optional[str] = None
    published_year: Optional[int] = None
    shelf_location: Optional[str] = None
    total_copies: int = Field(default=1, ge=1)


class LibraryBookUpdate(BaseModel):
    title: Optional[str] = None
    author: Optional[str] = None
    isbn: Optional[str] = None
    category: Optional[str] = None
    publisher: Optional[str] = None
    published_year: Optional[int] = None
    shelf_location: Optional[str] = None
    total_copies: Optional[int] = None


class LibraryBookResponse(BaseModel):
    id: str
    title: str
    author: Optional[str] = None
    accession_no: str
    isbn: Optional[str] = None
    category: Optional[str] = None
    publisher: Optional[str] = None
    published_year: Optional[int] = None
    shelf_location: Optional[str] = None
    total_copies: int
    available_copies: int


class LibraryIssueCreate(BaseModel):
    book_id: str
    member_id: str
    issued_on: Optional[date] = None
    due_on: Optional[date] = None


class LibraryReturnRequest(BaseModel):
    returned_on: Optional[date] = None
    fine_amount: Optional[float] = None
    remarks: Optional[str] = None


class LibraryIssueResponse(BaseModel):
    id: str
    book_id: str
    book_title: Optional[str] = None
    member_id: str
    member_name: Optional[str] = None
    issued_on: date
    due_on: date
    returned_on: Optional[date] = None
    fine_amount: float = 0
    status: str
    is_overdue: bool = False
    remarks: Optional[str] = None


# ---------------- Transport ----------------

class TransportRouteCreate(BaseModel):
    name: str = Field(min_length=1, max_length=255)
    code: str = Field(min_length=1, max_length=32)
    start_point: str
    end_point: str
    distance_km: Optional[float] = None
    fare: Optional[float] = None
    stops: Optional[str] = None


class TransportRouteUpdate(BaseModel):
    name: Optional[str] = None
    start_point: Optional[str] = None
    end_point: Optional[str] = None
    distance_km: Optional[float] = None
    fare: Optional[float] = None
    stops: Optional[str] = None


class TransportRouteResponse(BaseModel):
    id: str
    name: str
    code: str
    start_point: str
    end_point: str
    distance_km: Optional[float] = None
    fare: Optional[float] = None
    stops: Optional[str] = None
    vehicle_count: int = 0
    pass_count: int = 0


class TransportVehicleCreate(BaseModel):
    registration_no: str = Field(min_length=1, max_length=32)
    vehicle_type: Optional[str] = None
    capacity: int = Field(default=40, ge=1)
    driver_name: Optional[str] = None
    driver_phone: Optional[str] = None
    route_id: Optional[str] = None
    status: str = "ACTIVE"


class TransportVehicleUpdate(BaseModel):
    vehicle_type: Optional[str] = None
    capacity: Optional[int] = None
    driver_name: Optional[str] = None
    driver_phone: Optional[str] = None
    route_id: Optional[str] = None
    status: Optional[str] = None


class TransportVehicleResponse(BaseModel):
    id: str
    registration_no: str
    vehicle_type: Optional[str] = None
    capacity: int
    driver_name: Optional[str] = None
    driver_phone: Optional[str] = None
    route_id: Optional[str] = None
    route_name: Optional[str] = None
    status: str


class TransportPassCreate(BaseModel):
    route_id: str
    student_id: str
    pickup_point: Optional[str] = None
    valid_from: Optional[date] = None
    valid_to: Optional[date] = None
    fare_paid: Optional[float] = None


class TransportPassResponse(BaseModel):
    id: str
    route_id: str
    route_name: Optional[str] = None
    student_id: str
    student_name: Optional[str] = None
    roll_no: Optional[str] = None
    pickup_point: Optional[str] = None
    valid_from: date
    valid_to: date
    fare_paid: Optional[float] = None
    status: str
