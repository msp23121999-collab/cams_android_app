"""add partner companies table

Revision ID: cf96ca47c70d
Revises: 9be8582a22ac
Create Date: 2026-07-19 16:16:29.697210

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa

# revision identifiers, used by Alembic.
revision: str = 'cf96ca47c70d'
down_revision: Union[str, None] = '9be8582a22ac'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.create_table('partner_companies',
    sa.Column('name', sa.String(length=255), nullable=False),
    sa.Column('industry', sa.String(length=128), nullable=False),
    sa.Column('status', sa.String(length=32), server_default='Active', nullable=False),
    sa.Column('contact_email', sa.String(length=255), nullable=True),
    sa.Column('contact_phone', sa.String(length=32), nullable=True),
    sa.Column('notes', sa.String(length=1024), nullable=True),
    sa.Column('id', sa.String(length=36), nullable=False),
    sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.text('(CURRENT_TIMESTAMP)'), nullable=False),
    sa.Column('updated_at', sa.DateTime(timezone=True), server_default=sa.text('(CURRENT_TIMESTAMP)'), nullable=False),
    sa.Column('is_deleted', sa.Boolean(), server_default='false', nullable=False),
    sa.Column('deleted_at', sa.DateTime(timezone=True), nullable=True),
    sa.PrimaryKeyConstraint('id', name=op.f('pk_partner_companies'))
    )


def downgrade() -> None:
    op.drop_table('partner_companies')
