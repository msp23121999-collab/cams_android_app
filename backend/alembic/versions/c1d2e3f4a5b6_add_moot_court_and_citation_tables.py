"""add_moot_court_and_citation_tables

Revision ID: c1d2e3f4a5b6
Revises: b7c1a2d3e4f5
Create Date: 2026-07-18 00:00:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa

# revision identifiers, used by Alembic.
revision: str = 'c1d2e3f4a5b6'
down_revision: Union[str, None] = 'b7c1a2d3e4f5'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    bind = op.get_bind()
    inspector = sa.inspect(bind)
    existing_tables = inspector.get_table_names()

    if 'moot_court_memorials' not in existing_tables:
        op.create_table(
            'moot_court_memorials',
            sa.Column('id', sa.String(length=36), primary_key=True),
            sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
            sa.Column('updated_at', sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
            sa.Column('is_deleted', sa.Boolean(), server_default='false', nullable=False),
            sa.Column('deleted_at', sa.DateTime(timezone=True), nullable=True),
            sa.Column('student_id', sa.String(length=36), sa.ForeignKey('students.id'), nullable=False),
            sa.Column('title', sa.String(length=255), nullable=False),
            sa.Column('case_name', sa.String(length=255), nullable=True),
            sa.Column('content', sa.Text(), nullable=False),
            sa.Column('status', sa.String(length=32), server_default='draft', nullable=False),
        )

    if 'saved_citations' not in existing_tables:
        op.create_table(
            'saved_citations',
            sa.Column('id', sa.String(length=36), primary_key=True),
            sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
            sa.Column('updated_at', sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
            sa.Column('is_deleted', sa.Boolean(), server_default='false', nullable=False),
            sa.Column('deleted_at', sa.DateTime(timezone=True), nullable=True),
            sa.Column('student_id', sa.String(length=36), sa.ForeignKey('students.id'), nullable=False),
            sa.Column('case_name', sa.String(length=255), nullable=False),
            sa.Column('citation_text', sa.String(length=255), nullable=False),
            sa.Column('note', sa.Text(), nullable=True),
        )


def downgrade() -> None:
    bind = op.get_bind()
    inspector = sa.inspect(bind)
    existing_tables = inspector.get_table_names()

    if 'saved_citations' in existing_tables:
        op.drop_table('saved_citations')
    if 'moot_court_memorials' in existing_tables:
        op.drop_table('moot_court_memorials')
