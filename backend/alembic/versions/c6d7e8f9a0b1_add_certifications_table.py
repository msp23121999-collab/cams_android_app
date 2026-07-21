"""add_certifications_table

Revision ID: c6d7e8f9a0b1
Revises: b5c6d7e8f9a0
Create Date: 2026-07-18 00:00:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa

# revision identifiers, used by Alembic.
revision: str = 'c6d7e8f9a0b1'
down_revision: Union[str, None] = 'b5c6d7e8f9a0'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    bind = op.get_bind()
    inspector = sa.inspect(bind)
    existing_tables = set(inspector.get_table_names())

    if 'certifications' not in existing_tables:
        op.create_table(
            'certifications',
            sa.Column('student_id', sa.String(length=36), nullable=False),
            sa.Column('title', sa.String(length=255), nullable=False),
            sa.Column('issuer', sa.String(length=255), nullable=False),
            sa.Column('date', sa.String(length=32), nullable=False),
            sa.Column('category', sa.String(length=64), nullable=False),
            sa.Column('type', sa.String(length=32), nullable=False, server_default='training'),
            sa.Column('is_verified', sa.Boolean(), nullable=False, server_default='0'),
            sa.Column('file_url', sa.String(length=512), nullable=True),
            sa.Column('id', sa.String(length=36), nullable=False),
            sa.Column('created_at', sa.DateTime(timezone=True), nullable=False),
            sa.Column('updated_at', sa.DateTime(timezone=True), nullable=False),
            sa.Column('is_deleted', sa.Boolean(), nullable=False),
            sa.Column('deleted_at', sa.DateTime(timezone=True), nullable=True),
            sa.ForeignKeyConstraint(['student_id'], ['students.id'], name=op.f('fk_certifications_student_id_students')),
            sa.PrimaryKeyConstraint('id', name=op.f('pk_certifications')),
        )


def downgrade() -> None:
    bind = op.get_bind()
    inspector = sa.inspect(bind)
    if 'certifications' in set(inspector.get_table_names()):
        op.drop_table('certifications')
