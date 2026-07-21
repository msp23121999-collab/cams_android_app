"""Pydantic schemas for institutional Budget & Grants tracking."""
from typing import Optional

from pydantic import BaseModel, Field


# ---------------- Budget line items ----------------

class BudgetLineItemCreate(BaseModel):
    fiscal_year: str = Field(min_length=1, max_length=16)
    title: str = Field(min_length=1, max_length=255)
    category: str = "General"
    department_id: Optional[str] = None
    allocated_amount: float = 0
    notes: Optional[str] = None


class BudgetLineItemUpdate(BaseModel):
    title: Optional[str] = None
    category: Optional[str] = None
    allocated_amount: Optional[float] = None
    status: Optional[str] = None
    notes: Optional[str] = None


class BudgetLineItemResponse(BaseModel):
    id: str
    fiscal_year: str
    title: str
    category: str
    department_id: Optional[str] = None
    department_name: Optional[str] = None
    allocated_amount: float
    spent_amount: float
    remaining_amount: float
    status: str
    notes: Optional[str] = None
    created_at: str


class BudgetExpenseCreate(BaseModel):
    description: str = Field(min_length=1, max_length=512)
    amount: float = Field(gt=0)
    expense_date: str


class BudgetExpenseResponse(BaseModel):
    id: str
    line_item_id: str
    description: str
    amount: float
    expense_date: str
    recorded_by_name: Optional[str] = None
    created_at: str


# ---------------- Grants ----------------

class GrantCreate(BaseModel):
    title: str = Field(min_length=1, max_length=255)
    funding_agency: str = Field(min_length=1, max_length=255)
    department_id: Optional[str] = None
    principal_investigator: Optional[str] = None
    sanctioned_amount: float = 0
    start_date: Optional[str] = None
    end_date: Optional[str] = None
    notes: Optional[str] = None


class GrantUpdate(BaseModel):
    status: Optional[str] = None
    disbursed_amount: Optional[float] = None
    notes: Optional[str] = None


class GrantResponse(BaseModel):
    id: str
    title: str
    funding_agency: str
    department_id: Optional[str] = None
    department_name: Optional[str] = None
    principal_investigator: Optional[str] = None
    sanctioned_amount: float
    disbursed_amount: float
    status: str
    start_date: Optional[str] = None
    end_date: Optional[str] = None
    notes: Optional[str] = None
    created_at: str


class BudgetSummaryResponse(BaseModel):
    total_allocated: float
    total_spent: float
    total_remaining: float
    total_grants_sanctioned: float
    total_grants_disbursed: float
    active_grants_count: int
