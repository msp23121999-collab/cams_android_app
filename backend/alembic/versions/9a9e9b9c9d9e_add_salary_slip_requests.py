"""add_salary_slip_requests

Revision ID: 9a9e9b9c9d9e
Revises: f1d6fedfeb1a
Create Date: 2026-06-09 17:00:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa

# revision identifiers, used by Alembic.
revision: str = '9a9e9b9c9d9e'
down_revision: Union[str, None] = '846e783a199c'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    bind = op.get_bind()
    inspector = sa.inspect(bind)
    if 'salary_slip_requests' in inspector.get_table_names():
        return
    op.create_table(
        'salary_slip_requests',
        sa.Column('id', sa.String(length=36), nullable=False),
        sa.Column('faculty_id', sa.String(length=36), nullable=False),
        sa.Column('request_type', sa.String(length=64), nullable=False),
        sa.Column('month', sa.Integer(), nullable=False),
        sa.Column('year', sa.Integer(), nullable=False),
        sa.Column('remarks', sa.String(length=1024), nullable=True),
        sa.Column('status', sa.String(length=32), nullable=False, server_default='PENDING'),
        sa.Column('admin_remarks', sa.String(length=1024), nullable=True),
        sa.Column('salary_slip_id', sa.String(length=36), nullable=True),
        sa.Column('created_at', sa.DateTime(timezone=True), nullable=False),
        sa.Column('updated_at', sa.DateTime(timezone=True), nullable=False),
        sa.Column('is_deleted', sa.Boolean(), nullable=False, server_default='false'),
        sa.Column('deleted_at', sa.DateTime(timezone=True), nullable=True),
        sa.ForeignKeyConstraint(['faculty_id'], ['users.id'], name=op.f('fk_salary_slip_requests_faculty_id_users')),
        sa.ForeignKeyConstraint(['salary_slip_id'], ['salary_slips.id'], name=op.f('fk_salary_slip_requests_salary_slip_id_salary_slips')),
        sa.PrimaryKeyConstraint('id', name=op.f('pk_salary_slip_requests'))
    )


def downgrade() -> None:
    op.drop_table('salary_slip_requests')
