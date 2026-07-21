"""add_hall_tickets_table

Revision ID: b4a2d8e93c12
Revises: a3f1c9d2e701
Create Date: 2026-07-18 00:05:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa

# revision identifiers, used by Alembic.
revision: str = 'b4a2d8e93c12'
down_revision: Union[str, None] = 'a3f1c9d2e701'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    bind = op.get_bind()
    inspector = sa.inspect(bind)
    if 'hall_tickets' in inspector.get_table_names():
        return

    op.create_table(
        'hall_tickets',
        sa.Column('id', sa.String(length=36), primary_key=True),
        sa.Column('created_at', sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now()),
        sa.Column('updated_at', sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now()),
        sa.Column('is_deleted', sa.Boolean(), nullable=False, server_default='false'),
        sa.Column('deleted_at', sa.DateTime(timezone=True), nullable=True),
        sa.Column('student_id', sa.String(length=36), sa.ForeignKey('students.id'), nullable=False),
        sa.Column('exam_id', sa.String(length=36), sa.ForeignKey('exams.id'), nullable=True),
        sa.Column('exam_name', sa.String(length=255), nullable=False),
        sa.Column('is_eligible', sa.Boolean(), nullable=False, server_default='true'),
        sa.Column('ineligibility_reason', sa.String(length=1024), nullable=True),
        sa.Column('is_issued', sa.Boolean(), nullable=False, server_default='false'),
        sa.Column('file_url', sa.String(length=512), nullable=True),
        sa.Column('issued_at', sa.DateTime(timezone=True), nullable=True),
        sa.Column('exam_center', sa.String(length=255), nullable=True),
        sa.Column('exam_date', sa.String(length=64), nullable=True),
        sa.Column('student_signature_url', sa.String(length=512), nullable=True),
        sa.Column('principal_signature_url', sa.String(length=512), nullable=True),
        sa.Column('coe_signature_url', sa.String(length=512), nullable=True),
    )


def downgrade() -> None:
    bind = op.get_bind()
    inspector = sa.inspect(bind)
    if 'hall_tickets' in inspector.get_table_names():
        op.drop_table('hall_tickets')
