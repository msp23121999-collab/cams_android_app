from datetime import datetime, timezone
from sqlalchemy import select, update
from sqlalchemy.ext.asyncio import AsyncSession
from app.db.models.fee import FeeStructure, FeeRecord, Payment, FeeStatus

class FeeRepository:
    def __init__(self, db: AsyncSession) -> None:
        self.db = db

    async def get_fee_structures(self) -> list[FeeStructure]:
        result = await self.db.execute(
            select(FeeStructure).where(FeeStructure.is_deleted.is_(False))
        )
        return list(result.scalars().all())

    async def get_student_fee_records(self, student_id: str) -> list[FeeRecord]:
        result = await self.db.execute(
            select(FeeRecord).where(FeeRecord.student_id == student_id, FeeRecord.is_deleted.is_(False))
        )
        return list(result.scalars().all())

    async def get_fee_record_by_id(self, record_id: str) -> FeeRecord | None:
        result = await self.db.execute(
            select(FeeRecord).where(FeeRecord.id == record_id, FeeRecord.is_deleted.is_(False))
        )
        return result.scalar_one_or_none()

    async def get_payments_by_record(self, fee_record_id: str) -> list[Payment]:
        result = await self.db.execute(
            select(Payment).where(Payment.fee_record_id == fee_record_id, Payment.is_deleted.is_(False))
        )
        return list(result.scalars().all())

    async def add_payment(self, fee_record_id: str, amount: float, mode: str, txn_id: str) -> Payment:
        payment = Payment(
            fee_record_id=fee_record_id,
            amount=amount,
            mode=mode,
            txn_id=txn_id,
            receipt_url=f"/uploads/receipts/{txn_id}.pdf",
            paid_at=datetime.now(timezone.utc)
        )
        self.db.add(payment)
        await self.db.flush()
        return payment

    async def update_fee_record_status(self, record_id: str, status: FeeStatus) -> None:
        await self.db.execute(
            update(FeeRecord)
            .where(FeeRecord.id == record_id)
            .values(status=status)
        )
        await self.db.flush()
