"""add missing salary columns (employee_id, designation, working_days, leave_days, net_salary, leave_deduction, pf_deduction, joining_date)

Revision ID: 9be8582a22ac
Revises: dbfacc59505d
Create Date: 2026-07-19
"""
from alembic import op
import sqlalchemy as sa

revision = "9be8582a22ac"
down_revision = "dbfacc59505d"
branch_labels = None
depends_on = None


def upgrade() -> None:
    with op.batch_alter_table("salary") as batch_op:
        batch_op.add_column(sa.Column("employee_id", sa.String(length=64), nullable=True))
        batch_op.add_column(sa.Column("designation", sa.String(length=128), nullable=True))
        batch_op.add_column(sa.Column("working_days", sa.Integer(), nullable=False, server_default="30"))
        batch_op.add_column(sa.Column("leave_days", sa.Integer(), nullable=False, server_default="0"))
        batch_op.add_column(sa.Column("net_salary", sa.Numeric(12, 2), nullable=False, server_default="0.0"))
        batch_op.add_column(sa.Column("leave_deduction", sa.Numeric(12, 2), nullable=True))
        batch_op.add_column(sa.Column("pf_deduction", sa.Numeric(12, 2), nullable=True))
        batch_op.add_column(sa.Column("joining_date", sa.Date(), nullable=True))


def downgrade() -> None:
    with op.batch_alter_table("salary") as batch_op:
        batch_op.drop_column("joining_date")
        batch_op.drop_column("pf_deduction")
        batch_op.drop_column("leave_deduction")
        batch_op.drop_column("net_salary")
        batch_op.drop_column("leave_days")
        batch_op.drop_column("working_days")
        batch_op.drop_column("designation")
        batch_op.drop_column("employee_id")
