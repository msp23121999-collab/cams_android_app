"""add_salary_slip_columns

Revision ID: 7a7b7c7d7e7f
Revises: 9a9e9b9c9d9e
Create Date: 2026-06-09 17:15:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa

# revision identifiers, used by Alembic.
revision: str = '7a7b7c7d7e7f'
down_revision: Union[str, None] = '9a9e9b9c9d9e'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.add_column('salary', sa.Column('employee_id', sa.String(length=64), nullable=True))
    op.add_column('salary', sa.Column('designation', sa.String(length=128), nullable=True))
    op.add_column('salary', sa.Column('working_days', sa.Integer(), nullable=False, server_default='30'))
    op.add_column('salary', sa.Column('leave_days', sa.Integer(), nullable=False, server_default='0'))
    op.add_column('salary', sa.Column('leave_deduction', sa.Numeric(precision=12, scale=2), nullable=False, server_default='0.0'))
    op.add_column('salary', sa.Column('pf_deduction', sa.Numeric(precision=12, scale=2), nullable=False, server_default='0.0'))
    op.add_column('salary', sa.Column('net_salary', sa.Numeric(precision=12, scale=2), nullable=False, server_default='0.0'))


def downgrade() -> None:
    op.drop_column('salary', 'net_salary')
    op.drop_column('salary', 'pf_deduction')
    op.drop_column('salary', 'leave_deduction')
    op.drop_column('salary', 'leave_days')
    op.drop_column('salary', 'working_days')
    op.drop_column('salary', 'designation')
    op.drop_column('salary', 'employee_id')
