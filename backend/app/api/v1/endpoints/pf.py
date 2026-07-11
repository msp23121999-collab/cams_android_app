from datetime import date, datetime, timezone
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, desc, and_, text

from app.core.dependencies import get_db_session, role_required
from app.db.models.user import User, UserRole
from app.db.models.pf import PFConfiguration, PFHistoricalPeriod, PFContribution, PFClaim, PFAuditLog, PFCalculationMethod, PFLeaveExclusion
from app.schemas.pf import (
    PFConfigCreateOrUpdate, PFConfigResponse,
    PFHistoricalPeriodCreate, PFHistoricalPeriodResponse,
    PFClaimCreate, PFClaimResponse,
    PFStatementResponse, PFStatementDetail, PFStatementCumulative,
    PFAuditLogResponse, PFUpdateResponse,
    PFLeaveExclusionCreate, PFLeaveExclusionResponse
)


router = APIRouter()

def to_float(val) -> float:
    return float(val) if val is not None else 0.0

# Helper to log audit actions
async def log_pf_audit(db: AsyncSession, faculty_id: str, action: str, details: str, performed_by: str):
    log = PFAuditLog(
        faculty_id=faculty_id,
        action=action,
        details=details,
        performed_by=performed_by
    )
    db.add(log)
    await db.flush()

@router.post("/config", response_model=PFConfigResponse)
async def create_or_update_pf_config(
    payload: PFConfigCreateOrUpdate,
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.SUPER_ADMIN, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
):
    # Check if faculty user exists
    user_q = await db.execute(select(User).where(User.id == payload.faculty_id))
    user = user_q.scalar_one_or_none()
    if not user:
        raise HTTPException(status_code=404, detail="Faculty user not found")

    # Check for existing configuration
    config_q = await db.execute(select(PFConfiguration).where(PFConfiguration.faculty_id == payload.faculty_id))
    config = config_q.scalar_one_or_none()

    action = "Updated PF Config" if config else "Created PF Config"
    details = f"Method: {payload.calculation_method.value}, Value: {payload.value}, Basic Salary: {payload.basic_salary}, Earned salary toggle: {payload.based_on_earned_salary}"

    if config:
        config.joining_date = payload.joining_date
        config.pf_start_date = payload.pf_start_date
        config.historical_opening_balance = payload.historical_opening_balance
        config.calculation_method = payload.calculation_method
        config.value = payload.value
        config.based_on_earned_salary = payload.based_on_earned_salary
        config.basic_salary = payload.basic_salary
    else:
        config = PFConfiguration(
            faculty_id=payload.faculty_id,
            joining_date=payload.joining_date,
            pf_start_date=payload.pf_start_date,
            historical_opening_balance=payload.historical_opening_balance,
            calculation_method=payload.calculation_method,
            value=payload.value,
            based_on_earned_salary=payload.based_on_earned_salary,
            basic_salary=payload.basic_salary
        )
        db.add(config)

    await log_pf_audit(db, payload.faculty_id, action, details, current_user.id)
    await db.commit()
    await db.refresh(config)

    return {
        "id": config.id,
        "faculty_id": config.faculty_id,
        "joining_date": config.joining_date,
        "pf_start_date": config.pf_start_date,
        "historical_opening_balance": config.historical_opening_balance,
        "calculation_method": config.calculation_method,
        "value": config.value,
        "based_on_earned_salary": config.based_on_earned_salary,
        "basic_salary": to_float(config.basic_salary)
    }

@router.get("/config/{faculty_id}", response_model=PFConfigResponse | None)
async def get_pf_config(
    faculty_id: str,
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.SUPER_ADMIN, UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
):
    config_q = await db.execute(select(PFConfiguration).where(PFConfiguration.faculty_id == faculty_id))
    config = config_q.scalar_one_or_none()
    if not config:
        return None

    return {
        "id": config.id,
        "faculty_id": config.faculty_id,
        "joining_date": config.joining_date,
        "pf_start_date": config.pf_start_date,
        "historical_opening_balance": config.historical_opening_balance,
        "calculation_method": config.calculation_method,
        "value": config.value,
        "based_on_earned_salary": config.based_on_earned_salary,
        "basic_salary": to_float(config.basic_salary)
    }

@router.post("/historical", response_model=PFHistoricalPeriodResponse)
async def create_historical_period(
    payload: PFHistoricalPeriodCreate,
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.SUPER_ADMIN, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
):
    if payload.from_date >= payload.to_date:
        raise HTTPException(status_code=400, detail="From Date must be before To Date")

    # Calculate months between from_date and to_date
    months = (payload.to_date.year - payload.from_date.year) * 12 + payload.to_date.month - payload.from_date.month + 1
    total_amount = months * payload.amount_per_month

    period = PFHistoricalPeriod(
        faculty_id=payload.faculty_id,
        from_date=payload.from_date,
        to_date=payload.to_date,
        amount_per_month=payload.amount_per_month,
        months=months,
        total_amount=total_amount
    )
    db.add(period)
    await db.flush()

    # Automatically generate monthly PFContribution records for this historical period
    current_year = payload.from_date.year
    current_month = payload.from_date.month
    
    while (current_year < payload.to_date.year) or (current_year == payload.to_date.year and current_month <= payload.to_date.month):
        # Insert historical contribution
        contrib = PFContribution(
            faculty_id=payload.faculty_id,
            month=current_month,
            year=current_year,
            amount=payload.amount_per_month,
            employer_amount=0.0, # Default matching contribution
            is_historical=True
        )
        db.add(contrib)
        
        current_month += 1
        if current_month > 12:
            current_month = 1
            current_year += 1

    details = f"Period: {payload.from_date} to {payload.to_date}, Rate: {payload.amount_per_month}/mo, Months: {months}, Total: {total_amount}"
    await log_pf_audit(db, payload.faculty_id, "Added Historical PF Period", details, current_user.id)
    await db.commit()
    await db.refresh(period)
    return period

@router.get("/historical/{faculty_id}", response_model=list[PFHistoricalPeriodResponse])
async def list_historical_periods(
    faculty_id: str,
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.SUPER_ADMIN, UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
):
    periods_q = await db.execute(
        select(PFHistoricalPeriod)
        .where(PFHistoricalPeriod.faculty_id == faculty_id)
        .order_by(PFHistoricalPeriod.from_date)
    )
    return periods_q.scalars().all()

@router.post("/claims", response_model=PFClaimResponse)
async def record_pf_claim(
    payload: PFClaimCreate,
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.SUPER_ADMIN, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
):
    claim = PFClaim(
        faculty_id=payload.faculty_id,
        claim_date=payload.claim_date,
        amount=payload.amount,
        reference_number=payload.reference_number,
        remarks=payload.remarks
    )
    db.add(claim)

    details = f"Amount: {payload.amount}, Ref: {payload.reference_number}, Remarks: {payload.remarks}"
    await log_pf_audit(db, payload.faculty_id, "Recorded PF Claim", details, current_user.id)
    await db.commit()
    await db.refresh(claim)
    return claim

@router.get("/claims/{faculty_id}", response_model=list[PFClaimResponse])
async def list_pf_claims(
    faculty_id: str,
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.SUPER_ADMIN, UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
):
    claims_q = await db.execute(
        select(PFClaim)
        .where(PFClaim.faculty_id == faculty_id)
        .order_by(PFClaim.claim_date.desc())
    )
    return claims_q.scalars().all()

from app.db.models.payroll import Salary

@router.get("/statement/{faculty_id}", response_model=PFStatementResponse)
async def get_pf_statement(
    faculty_id: str,
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.SUPER_ADMIN, UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
):
    # Fetch PF Configuration
    config_q = await db.execute(select(PFConfiguration).where(PFConfiguration.faculty_id == faculty_id))
    config = config_q.scalar_one_or_none()

    # Fetch Historical Periods
    periods_q = await db.execute(
        select(PFHistoricalPeriod)
        .where(PFHistoricalPeriod.faculty_id == faculty_id)
        .order_by(PFHistoricalPeriod.from_date)
    )
    periods = periods_q.scalars().all()

    # Historical opening balance is taken from the sum of historical periods
    opening_bal = sum(to_float(p.total_amount) for p in periods)
    if opening_bal == 0.0 and config:
        opening_bal = to_float(config.historical_opening_balance)

    # Fetch Claims
    claims_q = await db.execute(select(PFClaim).where(PFClaim.faculty_id == faculty_id).order_by(PFClaim.claim_date.desc()))
    claims = claims_q.scalars().all()
    total_claims = sum(to_float(c.amount) for c in claims)

    # Fetch Leave Exclusions
    exclusions_q = await db.execute(
        select(PFLeaveExclusion)
        .where(PFLeaveExclusion.faculty_id == faculty_id)
    )
    exclusions = exclusions_q.scalars().all()
    excluded_months = set()
    for excl in exclusions:
        ey, em = excl.from_date.year, excl.from_date.month
        while (ey, em) <= (excl.to_date.year, excl.to_date.month):
            excluded_months.add((ey, em))
            em += 1
            if em > 12:
                em = 1
                ey += 1

    # Get latest claim date to filter statement calculations if needed
    latest_claim_date = None
    if claims:
        latest_claim_date = claims[0].claim_date

    # Fetch saved contributions from database
    contrib_q = await db.execute(
        select(PFContribution)
        .where(PFContribution.faculty_id == faculty_id)
        .order_by(PFContribution.year, PFContribution.month)
    )
    all_contributions = contrib_q.scalars().all()

    # Distinguish historical vs saved payroll contributions
    historical_contribs = [c for c in all_contributions if c.is_historical]
    saved_payroll_contribs = {(c.year, c.month): c for c in all_contributions if not c.is_historical}

    detailed_list = []
    total_cont = 0.0
    total_emp_cont = 0.0
    month_names = ["", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"]

    # 1. Add historical contributions (do not sum into total_cont / total_emp_cont because these represent opening balance details)
    for c in historical_contribs:
        detailed_list.append(
            PFStatementDetail(
                month_name=month_names[c.month],
                month=c.month,
                year=c.year,
                amount=to_float(c.amount),
                employer_amount=to_float(c.employer_amount),
                type="HISTORICAL"
            )
        )

    # 2. Add payroll contributions (saved in DB or dynamically generated from config.pf_start_date up to current month)
    if config:
        # Determine accumulation start month (start at config.pf_start_date)
        y, m = config.pf_start_date.year, config.pf_start_date.month

        today = date.today()
        # Stop at last completed month (exclude current month — contributions for this month aren't finalised yet)
        cutoff_year, cutoff_month = (today.year, today.month - 1) if today.month > 1 else (today.year - 1, 12)
        while (y < cutoff_year) or (y == cutoff_year and m <= cutoff_month):
            if (y, m) not in excluded_months:
                if (y, m) in saved_payroll_contribs:
                    db_contrib = saved_payroll_contribs[(y, m)]
                    amount = to_float(db_contrib.amount)
                    emp_amount = to_float(db_contrib.employer_amount)
                else:
                    # Calculate dynamically based on config method
                    amount = 0.0
                    if config.calculation_method == PFCalculationMethod.FIXED:
                        amount = to_float(config.value)
                    elif config.calculation_method == PFCalculationMethod.PERCENTAGE:
                        # Use payroll salary if available, else fall back to config.basic_salary
                        salary_q = await db.execute(
                            select(Salary.basic)
                            .where(
                                Salary.faculty_id == faculty_id,
                                Salary.year == y,
                                Salary.month == m
                            )
                        )
                        latest_basic = salary_q.scalar_one_or_none()
                        # fallback to manually entered basic_salary on config
                        effective_basic = to_float(latest_basic) if latest_basic is not None else to_float(config.basic_salary or 0.0)
                        amount = round(effective_basic * (to_float(config.value) / 100.0), 2)
                    emp_amount = 0.0

                if amount > 0:
                    total_cont += amount
                    total_emp_cont += emp_amount
                    detailed_list.append(
                        PFStatementDetail(
                            month_name=month_names[m],
                            month=m,
                            year=y,
                            amount=amount,
                            employer_amount=emp_amount,
                            type="PAYROLL"
                        )
                    )

            m += 1
            if m > 12:
                m = 1
                y += 1

    # Calculate remaining balance by adding opening balance and current contributions, then subtracting claims
    remaining_balance = opening_bal + total_cont + total_emp_cont - total_claims

    # Fetch Cumulative periods from configuration
    cumulative_list = []
    for p in periods:
        cumulative_list.append(
            PFStatementCumulative(
                from_year=p.from_date.year,
                to_year=p.to_date.year,
                months=p.months,
                amount_per_month=to_float(p.amount_per_month),
                total_amount=to_float(p.total_amount)
            )
        )

    # If there are payroll records (detailed entries with type PAYROLL), group them as an active cumulative period
    payroll_detailed = [d for d in detailed_list if d.type == "PAYROLL"]
    if payroll_detailed:
        min_c = min(payroll_detailed, key=lambda x: (x.year, x.month))
        max_c = max(payroll_detailed, key=lambda x: (x.year, x.month))
        months_count = len(payroll_detailed)
        total_p_amt = sum(d.amount for d in payroll_detailed)
        avg_rate = total_p_amt / months_count if months_count > 0 else 0
        cumulative_list.append(
            PFStatementCumulative(
                from_year=min_c.year,
                to_year=max_c.year,
                months=months_count,
                amount_per_month=round(avg_rate, 2),
                total_amount=total_p_amt
            )
        )

    return PFStatementResponse(
        historical_opening_balance=opening_bal,
        total_contributions=total_cont,
        total_employer_contributions=total_emp_cont,
        total_claims=total_claims,
        remaining_balance=remaining_balance,
        detailed=detailed_list,
        cumulative=cumulative_list,
        claims=[PFClaimResponse.model_validate(c) for c in claims]
    )

@router.get("/dashboard/{faculty_id}")
async def get_pf_dashboard(
    faculty_id: str,
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.SUPER_ADMIN, UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
):
    # Re-use the statement calculation to get dashboard values
    stmt = await get_pf_statement(faculty_id, current_user, db)
    
    last_claim_date = None
    if stmt.claims:
        last_claim_date = stmt.claims[0].claim_date

    return {
        "current_balance": stmt.remaining_balance,
        "total_contribution": stmt.total_contributions,
        "employer_contribution": stmt.total_employer_contributions,
        "claim_history_count": len(stmt.claims),
        "last_claim_date": last_claim_date,
        "total_pf_amount": stmt.historical_opening_balance + stmt.total_contributions + stmt.total_employer_contributions,
        "total_claimed": stmt.total_claims
    }

@router.get("/audit-logs", response_model=list[PFAuditLogResponse])
async def get_pf_audit_logs(
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.SUPER_ADMIN, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
):
    logs_q = await db.execute(
        select(PFAuditLog, User.full_name)
        .join(User, PFAuditLog.performed_by == User.id)
        .order_by(desc(PFAuditLog.created_at))
    )
    
    results = []
    for log, name in logs_q.all():
        results.append(
            PFAuditLogResponse(
                id=log.id,
                action=log.action,
                details=log.details,
                created_at=log.created_at,
                performed_by_name=name
            )
        )
    return results

@router.post("/leave-exclusions", response_model=PFLeaveExclusionResponse)
async def create_leave_exclusion(
    payload: PFLeaveExclusionCreate,
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.SUPER_ADMIN, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
):
    if payload.from_date >= payload.to_date:
        raise HTTPException(status_code=400, detail="From Date must be before To Date")

    exclusion = PFLeaveExclusion(
        faculty_id=payload.faculty_id,
        from_date=payload.from_date,
        to_date=payload.to_date,
        reason=payload.reason
    )
    db.add(exclusion)

    details = f"Excluded: {payload.from_date} to {payload.to_date}, Reason: {payload.reason}"
    await log_pf_audit(db, payload.faculty_id, "Added PF Leave Exclusion", details, current_user.id)
    await db.commit()
    await db.refresh(exclusion)
    return exclusion

@router.get("/leave-exclusions/{faculty_id}", response_model=list[PFLeaveExclusionResponse])
async def list_leave_exclusions(
    faculty_id: str,
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.SUPER_ADMIN, UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
):
    exclusions_q = await db.execute(
        select(PFLeaveExclusion)
        .where(PFLeaveExclusion.faculty_id == faculty_id)
        .order_by(PFLeaveExclusion.from_date)
    )
    return exclusions_q.scalars().all()

@router.delete("/leave-exclusions/{exclusion_id}", response_model=PFUpdateResponse)
async def delete_leave_exclusion(
    exclusion_id: str,
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.SUPER_ADMIN, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
):
    excl_q = await db.execute(select(PFLeaveExclusion).where(PFLeaveExclusion.id == exclusion_id))
    exclusion = excl_q.scalar_one_or_none()
    if not exclusion:
        raise HTTPException(status_code=404, detail="Leave Exclusion not found")

    details = f"Removed Exclusion: {exclusion.from_date} to {exclusion.to_date}, Reason: {exclusion.reason}"
    await log_pf_audit(db, exclusion.faculty_id, "Removed PF Leave Exclusion", details, current_user.id)
    
    await db.delete(exclusion)
    await db.commit()

    return {"status": "success", "message": "Leave exclusion removed successfully"}

