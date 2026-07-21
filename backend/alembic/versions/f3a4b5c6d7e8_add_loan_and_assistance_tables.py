"""add_loan_and_assistance_tables

Revision ID: f3a4b5c6d7e8
Revises: e8f9a0b1c2d3
Create Date: 2026-07-18 00:00:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa

# revision identifiers, used by Alembic.
revision: str = 'f3a4b5c6d7e8'
down_revision: Union[str, None] = 'e8f9a0b1c2d3'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    bind = op.get_bind()
    inspector = sa.inspect(bind)
    existing_tables = set(inspector.get_table_names())

    if 'student_loans' not in existing_tables:
        op.create_table(
            'student_loans',
            sa.Column('student_id', sa.String(length=36), nullable=False),
            sa.Column('bank', sa.String(length=128), nullable=False),
            sa.Column('branch', sa.String(length=128), nullable=False),
            sa.Column('sanctioned', sa.Numeric(12, 2), nullable=False),
            sa.Column('interest_rate', sa.Numeric(5, 2), nullable=False),
            sa.Column('emi', sa.Numeric(12, 2), nullable=False),
            sa.Column('outstanding', sa.Numeric(12, 2), nullable=False),
            sa.Column('status', sa.String(length=32), nullable=False, server_default='ACTIVE'),
            sa.Column('id', sa.String(length=36), nullable=False),
            sa.Column('created_at', sa.DateTime(timezone=True), nullable=False),
            sa.Column('updated_at', sa.DateTime(timezone=True), nullable=False),
            sa.Column('is_deleted', sa.Boolean(), nullable=False),
            sa.Column('deleted_at', sa.DateTime(timezone=True), nullable=True),
            sa.ForeignKeyConstraint(['student_id'], ['students.id'], name=op.f('fk_student_loans_student_id_students')),
            sa.PrimaryKeyConstraint('id', name=op.f('pk_student_loans')),
        )

    if 'financial_assistance_requests' not in existing_tables:
        op.create_table(
            'financial_assistance_requests',
            sa.Column('student_id', sa.String(length=36), nullable=False),
            sa.Column('type', sa.String(length=64), nullable=False),
            sa.Column('reason', sa.String(length=2048), nullable=False),
            sa.Column('status', sa.String(length=32), nullable=False, server_default='PENDING'),
            sa.Column('admin_remarks', sa.String(length=1024), nullable=True),
            sa.Column('id', sa.String(length=36), nullable=False),
            sa.Column('created_at', sa.DateTime(timezone=True), nullable=False),
            sa.Column('updated_at', sa.DateTime(timezone=True), nullable=False),
            sa.Column('is_deleted', sa.Boolean(), nullable=False),
            sa.Column('deleted_at', sa.DateTime(timezone=True), nullable=True),
            sa.ForeignKeyConstraint(['student_id'], ['students.id'], name=op.f('fk_financial_assistance_requests_student_id_students')),
            sa.PrimaryKeyConstraint('id', name=op.f('pk_financial_assistance_requests')),
        )


def downgrade() -> None:
    bind = op.get_bind()
    inspector = sa.inspect(bind)
    existing_tables = set(inspector.get_table_names())

    if 'financial_assistance_requests' in existing_tables:
        op.drop_table('financial_assistance_requests')
    if 'student_loans' in existing_tables:
        op.drop_table('student_loans')
