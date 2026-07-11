from sqlalchemy.ext.asyncio import AsyncSession
from app.db.repositories.fee_repository import FeeRepository
from app.db.repositories.student_repository import StudentRepository
from app.db.models.fee import FeeRecord, Payment, FeeStatus

class FeeService:
    def __init__(self, db: AsyncSession) -> None:
        self.db = db
        self.fee_repo = FeeRepository(db)
        self.student_repo = StudentRepository(db)

    async def ensure_student_fee_records(self, student_id: str) -> None:
        from app.db.models.student import Student
        from app.db.models.fee import FeeStructure, FeeRecord, FeeStatus
        from sqlalchemy import select
        from datetime import date
        
        student_q = await self.db.execute(
            select(Student).where(Student.id == student_id, Student.is_deleted.is_(False))
        )
        student = student_q.scalar_one_or_none()
        if not student or not student.department_id or not student.semester:
            return

        # Find all fee structures for student's department up to their current semester
        structs_q = await self.db.execute(
            select(FeeStructure).where(
                FeeStructure.dept_id == student.department_id,
                FeeStructure.semester <= student.semester,
                FeeStructure.is_deleted.is_(False)
            )
        )
        matching_structs = list(structs_q.scalars().all())
        
        # Ensure a Tuition Fee structure exists for each semester up to student's current semester
        for sem in range(1, student.semester + 1):
            has_tuition = any(
                s.semester == sem and 
                ("tuition" in s.fee_type.lower() or "tution" in s.fee_type.lower()) 
                for s in matching_structs
            )
            if not has_tuition:
                new_struct = FeeStructure(
                    dept_id=student.department_id,
                    semester=sem,
                    amount=0.0,
                    due_date=date.today(),
                    fee_type="Tuition Fee"
                )
                self.db.add(new_struct)
                await self.db.flush()
                matching_structs.append(new_struct)
        
        # Check and create missing FeeRecords for all matching structures
        for struct in matching_structs:
            record_q = await self.db.execute(
                select(FeeRecord).where(
                    FeeRecord.student_id == student_id,
                    FeeRecord.fee_structure_id == struct.id,
                    FeeRecord.is_deleted.is_(False)
                )
            )
            existing_record = record_q.scalar_one_or_none()
            if not existing_record:
                new_record = FeeRecord(
                    student_id=student_id,
                    fee_structure_id=struct.id,
                    status=FeeStatus.PENDING
                )
                self.db.add(new_record)
        await self.db.commit()

    async def get_student_fee_summary(self, student_id: str) -> dict:
        await self.ensure_student_fee_records(student_id)
        from app.core.json_db_helper import load_db_from_postgres
        from app.db.models.student import Student
        from app.db.models.academic import Degree
        from sqlalchemy import select
        import math
        
        # Fetch student details
        student_q = await self.db.execute(
            select(Student).where(Student.id == student_id, Student.is_deleted.is_(False))
        )
        student = student_q.scalar_one_or_none()
        if not student:
            return {
                "total_fees": 0.0,
                "scholarship_deduction": 0.0,
                "other_deductions": 0.0,
                "net_fees": 0.0,
                "amount_paid": 0.0,
                "pending_balance": 0.0,
                "due_amount": 0.0,
                "due_date": None,
                "records": []
            }
        
        # Determine quota and community
        quota = (student.quota or "Government").lower()
        community = (student.community_category or "General").upper()
        
        # Load blueprints
        blueprints = load_db_from_postgres("fee_blueprints_list.json", lambda: [])
        reg_code = None
        if student.degree_id:
            reg_q = await self.db.execute(select(Degree).where(Degree.id == student.degree_id))
            degree = reg_q.scalar_one_or_none()
            if degree:
                reg_code = degree.code
        
        batch = student.batch_year
        matching_bp = None
        for bp in blueprints:
            bp_code = bp.get("degreeCode") or bp.get("regCode")
            if bp_code == reg_code and str(bp.get("batch")) == str(batch):
                matching_bp = bp
                break
        
        current_year = math.ceil(student.semester / 2)
        tuition_total = 85000.0  # Default fallback tuition fee
        if matching_bp:
            fees_by_year = matching_bp.get("fees", {})
            year_fees = fees_by_year.get(str(current_year)) or fees_by_year.get(current_year)
            if year_fees:
                quota_key = "government"
                if "nri" in quota:
                    quota_key = "nri"
                elif "management" in quota or "mgmt" in quota:
                    quota_key = "management"
                
                amt_str = year_fees.get(quota_key)
                if amt_str:
                    try:
                        tuition_total = float(amt_str)
                    except:
                        pass
        
        # Determine concessions
        concessions = load_db_from_postgres("student_concessions.json", lambda: {})
        student_con = concessions.get(student_id, {})
        
        spec_scholarship = float(student_con.get("scholarship_amount") or 0.0)
        spec_deduction = float(student_con.get("deduction_amount") or 0.0)

        matched_st = None
        # Apply scholarship type reduction if student has one assigned
        if student.scholarship_type_id:
            scholarship_types = load_db_from_postgres("scholarship_types_list.json", lambda: [])
            for st in scholarship_types:
                if st.get("id") == student.scholarship_type_id or st.get("name") == student.scholarship_type_id:
                    matched_st = st
                    break
            
            if matched_st:
                # Check scope: all_batches or specific_batch
                scope = matched_st.get("scope", "all_batches")
                batch_ok = True
                if scope == "specific_batch":
                    target_batch = str(matched_st.get("batch_year", ""))
                    batch_ok = target_batch == str(student.batch_year)
                    
                    target_dept = matched_st.get("department_id")
                    if target_dept and target_dept != "all":
                        batch_ok = batch_ok and (str(target_dept) == str(student.department_id))
                
                # Check program level filter
                level_filter = matched_st.get("program_level", "all")
                if level_filter != "all" and student.degree_id:
                    from app.db.models.academic import Degree as DegreeModel
                    deg_q = await self.db.execute(
                        select(DegreeModel).where(DegreeModel.id == student.degree_id)
                    )
                    deg = deg_q.scalar_one_or_none()
                    if deg and deg.program_level != level_filter:
                        batch_ok = False

                if batch_ok:
                    reduction_type = matched_st.get("reduction_type", "flat")
                    reduction_value = float(matched_st.get("reduction_value") or 0.0)
                    if reduction_type == "percentage":
                        # Apply % on top of the total tuition
                        percentage_amount = (reduction_value / 100.0) * tuition_total
                        spec_scholarship += percentage_amount
                    else:
                        # Flat ₹ reduction
                        spec_scholarship += reduction_value

        # ST community students get 100% free scholarship on Tuition Fees
        st_scholarship = 0.0
        if community == "ST":
            st_scholarship = tuition_total
            
        total_scholarship = st_scholarship + spec_scholarship
        total_deduction = spec_deduction
        
        # Load records and payments
        records = await self.fee_repo.get_student_fee_records(student_id)
        structures = await self.fee_repo.get_fee_structures()
        struct_map = {s.id: s for s in structures}
        
        details = []
        total_fees = 0.0
        amount_paid = 0.0
        
        seen_fee_keys = set()
        for r in records:
            struct = struct_map.get(r.fee_structure_id)
            if not struct or struct.semester > student.semester:
                continue
                
            is_tuition = "tuition" in struct.fee_type.lower() or "tution" in struct.fee_type.lower()
            if not is_tuition:
                continue
                
            norm_type = "tuition fee"
            key = (norm_type, struct.semester)
            if key in seen_fee_keys:
                continue
            seen_fee_keys.add(key)
            
            st_sc = 0.0  # Reset to prevent scope pollution across loop iterations
            payments = await self.fee_repo.get_payments_by_record(r.id)
            record_paid = float(sum(p.amount for p in payments))
            amount_paid += record_paid
            
            is_tuition = "tuition" in struct.fee_type.lower() or "tution" in struct.fee_type.lower()
            if is_tuition:
                # Tuition is overall fees, only charged in Semester 1
                if struct.semester == 1:
                    struct_tuition_total = 85000.0  # Default fallback tuition fee
                    if matching_bp:
                        fees_by_year = matching_bp.get("fees", {})
                        year_fees = fees_by_year.get("1") or fees_by_year.get(1)
                        if year_fees:
                            quota_key = "government"
                            if "nri" in quota:
                                quota_key = "nri"
                            elif "management" in quota or "mgmt" in quota:
                                quota_key = "management"
                            
                            amt_str = year_fees.get(quota_key)
                            if amt_str:
                                try:
                                    struct_tuition_total = float(amt_str)
                                except:
                                    pass
                    
                    struct_amt = struct_tuition_total
                    st_sc = struct_tuition_total if community == "ST" else 0.0
                    record_net = max(0.0, struct_amt - (st_sc + spec_scholarship + spec_deduction))
                else:
                    struct_amt = 0.0
                    record_net = 0.0
            else:
                struct_amt = float(struct.amount)
                record_net = struct_amt
                
            total_fees += struct_amt
            record_remaining = max(0.0, record_net - record_paid)
            
            if record_paid >= record_net:
                status_val = "paid"
            elif record_paid > 0:
                status_val = "partially_paid"
            else:
                status_val = "pending"
                
            details.append({
                "record_id": r.id,
                "fee_type": struct.fee_type,
                "amount": record_net,
                "due_date": struct.due_date,
                "status": status_val,
                "paid_amount": record_paid,
                "remaining_amount": record_remaining,
                "gross_amount": struct_amt,
                "scholarship_amount": st_sc + (spec_scholarship if is_tuition and struct.semester == 1 else 0.0),
                "scholarship_name": "ST Community 100% Concession" if st_sc > 0 else (matched_st.get("name") if (matched_st and is_tuition and struct.semester == 1) else ""),
                "deduction_amount": spec_deduction if is_tuition and struct.semester == 1 else 0.0,
                "deduction_reason": student_con.get("deduction_reason", "") if (is_tuition and struct.semester == 1) else ""
            })
            
        total_scholarship = sum(d["scholarship_amount"] for d in details)
        total_deduction = sum(d["deduction_amount"] for d in details)
        net_fees = sum(d["amount"] for d in details)
        pending_balance = sum(d["remaining_amount"] for d in details)
        
        upcoming_due_date = None
        pending_recs = [d for d in details if d["status"] != "paid"]
        if pending_recs:
            upcoming_due_date = min(d["due_date"] for d in pending_recs)
            
        return {
            "total_fees": total_fees,
            "scholarship_deduction": total_scholarship,
            "other_deductions": total_deduction,
            "net_fees": net_fees,
            "amount_paid": amount_paid,
            "pending_balance": pending_balance,
            "due_amount": pending_balance,
            "due_date": upcoming_due_date,
            "assigned_scholarship_type_id": student.scholarship_type_id,
            "records": details
        }

    async def pay_fee(self, record_id: str, amount: float, mode: str, txn_id: str) -> Payment:
        payment = await self.fee_repo.add_payment(record_id, amount, mode, txn_id)
        record = await self.fee_repo.get_fee_record_by_id(record_id)
        if record:
            summary = await self.get_student_fee_summary(record.student_id)
            rec_detail = next((item for item in summary["records"] if item["record_id"] == record_id), None)
            if rec_detail and rec_detail["status"] == "paid":
                await self.fee_repo.update_fee_record_status(record_id, FeeStatus.PAID)
        return payment
