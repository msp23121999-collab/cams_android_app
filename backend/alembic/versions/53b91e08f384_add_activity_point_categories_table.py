"""add activity point categories table

Revision ID: 53b91e08f384
Revises: cf96ca47c70d
Create Date: 2026-07-19

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa

revision: str = '53b91e08f384'
down_revision: Union[str, None] = 'cf96ca47c70d'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.create_table('activity_point_categories',
    sa.Column('code', sa.String(length=64), nullable=False),
    sa.Column('name', sa.String(length=255), nullable=False),
    sa.Column('max_points', sa.Numeric(precision=6, scale=2), nullable=False),
    sa.Column('description', sa.String(length=1024), nullable=True),
    sa.Column('id', sa.String(length=36), nullable=False),
    sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.text('(CURRENT_TIMESTAMP)'), nullable=False),
    sa.Column('updated_at', sa.DateTime(timezone=True), server_default=sa.text('(CURRENT_TIMESTAMP)'), nullable=False),
    sa.Column('is_deleted', sa.Boolean(), server_default='false', nullable=False),
    sa.Column('deleted_at', sa.DateTime(timezone=True), nullable=True),
    sa.PrimaryKeyConstraint('id', name=op.f('pk_activity_point_categories')),
    sa.UniqueConstraint('code', name=op.f('uq_activity_point_categories_code'))
    )


def downgrade() -> None:
    op.drop_table('activity_point_categories')
