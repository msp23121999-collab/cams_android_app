"""add class diaries table

Revision ID: 226a1129c308
Revises: c6d7e8f9a0b1
Create Date: 2026-07-19 13:10:28.858347

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa

# revision identifiers, used by Alembic.
revision: str = '226a1129c308'
down_revision: Union[str, None] = 'c6d7e8f9a0b1'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.create_table('class_diaries',
    sa.Column('faculty_id', sa.String(length=36), nullable=False),
    sa.Column('date', sa.String(length=16), nullable=False),
    sa.Column('subject', sa.String(length=255), nullable=False),
    sa.Column('course', sa.String(length=255), nullable=True),
    sa.Column('semester', sa.String(length=32), nullable=True),
    sa.Column('section', sa.String(length=64), nullable=True),
    sa.Column('hour', sa.String(length=32), nullable=True),
    sa.Column('year', sa.String(length=16), nullable=True),
    sa.Column('unit', sa.String(length=255), nullable=True),
    sa.Column('topic', sa.String(length=1024), nullable=True),
    sa.Column('subtopic', sa.String(length=1024), nullable=True),
    sa.Column('teaching_method', sa.String(length=255), nullable=True),
    sa.Column('learning_outcome', sa.String(length=2048), nullable=True),
    sa.Column('class_activity', sa.String(length=2048), nullable=True),
    sa.Column('remarks', sa.String(length=2048), nullable=True),
    sa.Column('status', sa.String(length=32), server_default='Draft', nullable=False),
    sa.Column('deviation_reason', sa.String(length=1024), nullable=True),
    sa.Column('revised_date', sa.String(length=16), nullable=True),
    sa.Column('attachment_url', sa.String(length=512), nullable=True),
    sa.Column('attachment_name', sa.String(length=255), nullable=True),
    sa.Column('completion_status', sa.String(length=32), server_default='Completed', nullable=True),
    sa.Column('json_entry_id', sa.String(length=64), nullable=True),
    sa.Column('id', sa.String(length=36), nullable=False),
    sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.text('(CURRENT_TIMESTAMP)'), nullable=False),
    sa.Column('updated_at', sa.DateTime(timezone=True), server_default=sa.text('(CURRENT_TIMESTAMP)'), nullable=False),
    sa.Column('is_deleted', sa.Boolean(), server_default='false', nullable=False),
    sa.Column('deleted_at', sa.DateTime(timezone=True), nullable=True),
    sa.ForeignKeyConstraint(['faculty_id'], ['users.id'], name=op.f('fk_class_diaries_faculty_id_users')),
    sa.PrimaryKeyConstraint('id', name=op.f('pk_class_diaries')),
    sa.UniqueConstraint('json_entry_id', name=op.f('uq_class_diaries_json_entry_id'))
    )


def downgrade() -> None:
    op.drop_table('class_diaries')
