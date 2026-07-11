from datetime import date, datetime
from pydantic import BaseModel
from app.db.models.leave import LeaveStatus


class LeaveBalanceResponse(BaseModel):
    casual_leave: float
    sick_leave: float
    earned_leave: float
    on_duty_leave: float

    class Config:
        from_attributes = True


class LeaveApplyRequest(BaseModel):
    type: str
    from_date: date
    to_date: date
    reason: str
    emergency_contact: str
    attachment_url: str | None = None


class LeaveResponse(BaseModel):
    id: str
    user_id: str
    type: str
    from_date: date
    to_date: date
    num_days: float
    reason: str
    emergency_contact: str | None = None
    attachment_url: str | None = None
    status: LeaveStatus
    user_name: str | None = None

    # Legacy single-approver fields (kept for backward compatibility)
    remarks: str | None = None
    approved_by_name: str | None = None

    # HOD stage audit trail
    hod_status: str | None = None
    hod_action_by_name: str | None = None
    hod_action_date: datetime | None = None
    hod_remarks: str | None = None

    # Principal stage audit trail
    principal_action_by_name: str | None = None
    principal_action_date: datetime | None = None
    principal_remarks: str | None = None

    class Config:
        from_attributes = True


class LeaveApprovalRequest(BaseModel):
    status: LeaveStatus
    remarks: str | None = None
