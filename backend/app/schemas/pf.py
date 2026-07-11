from datetime import date, datetime
from pydantic import BaseModel, Field
from app.db.models.pf import PFCalculationMethod

class PFConfigCreateOrUpdate(BaseModel):
    faculty_id: str
    joining_date: date
    pf_start_date: date
    historical_opening_balance: float = 0.0
    calculation_method: PFCalculationMethod
    value: float
    based_on_earned_salary: bool = False
    basic_salary: float = 0.0

class PFConfigResponse(BaseModel):
    id: str
    faculty_id: str
    joining_date: date
    pf_start_date: date
    historical_opening_balance: float
    calculation_method: PFCalculationMethod
    value: float
    based_on_earned_salary: bool
    basic_salary: float = 0.0

    class Config:
        from_attributes = True

class PFHistoricalPeriodCreate(BaseModel):
    faculty_id: str
    from_date: date
    to_date: date
    amount_per_month: float

class PFHistoricalPeriodResponse(BaseModel):
    id: str
    faculty_id: str
    from_date: date
    to_date: date
    amount_per_month: float
    months: int
    total_amount: float

    class Config:
        from_attributes = True

class PFClaimCreate(BaseModel):
    faculty_id: str
    claim_date: date
    amount: float
    reference_number: str
    remarks: str | None = None

class PFClaimResponse(BaseModel):
    id: str
    faculty_id: str
    claim_date: date
    amount: float
    reference_number: str
    remarks: str | None

    class Config:
        from_attributes = True

class PFContributionResponse(BaseModel):
    id: str
    month: int
    year: int
    amount: float
    employer_amount: float
    is_historical: bool

    class Config:
        from_attributes = True

class PFStatementDetail(BaseModel):
    month_name: str
    month: int
    year: int
    amount: float
    employer_amount: float
    type: str # "HISTORICAL" or "PAYROLL"

class PFStatementCumulative(BaseModel):
    from_year: int
    to_year: int
    months: int
    amount_per_month: float
    total_amount: float

class PFStatementResponse(BaseModel):
    historical_opening_balance: float
    total_contributions: float
    total_employer_contributions: float
    total_claims: float
    remaining_balance: float
    detailed: list[PFStatementDetail]
    cumulative: list[PFStatementCumulative]
    claims: list[PFClaimResponse]

class PFAuditLogResponse(BaseModel):
    id: str
    action: str
    details: str | None
    created_at: datetime
    performed_by_name: str

    class Config:
        from_attributes = True

class PFUpdateResponse(BaseModel):
    status: str
    message: str

class PFLeaveExclusionCreate(BaseModel):
    faculty_id: str
    from_date: date
    to_date: date
    reason: str | None = None

class PFLeaveExclusionResponse(BaseModel):
    id: str
    faculty_id: str
    from_date: date
    to_date: date
    reason: str | None

    class Config:
        from_attributes = True

