"""add_club_tables

Revision ID: c5b3e9fa4d23
Revises: b4a2d8e93c12
Create Date: 2026-07-18 00:10:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa

# revision identifiers, used by Alembic.
revision: str = 'c5b3e9fa4d23'
down_revision: Union[str, None] = 'b4a2d8e93c12'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    bind = op.get_bind()
    inspector = sa.inspect(bind)
    existing = inspector.get_table_names()

    if 'clubs' not in existing:
        op.create_table(
            'clubs',
            sa.Column('id', sa.String(length=36), primary_key=True),
            sa.Column('created_at', sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now()),
            sa.Column('updated_at', sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now()),
            sa.Column('is_deleted', sa.Boolean(), nullable=False, server_default='false'),
            sa.Column('deleted_at', sa.DateTime(timezone=True), nullable=True),
            sa.Column('name', sa.String(length=255), nullable=False),
            sa.Column('description', sa.String(length=2048), nullable=True),
            sa.Column('category', sa.String(length=128), nullable=True),
            sa.Column('member_count', sa.Integer(), nullable=False, server_default='0'),
        )

    if 'club_memberships' not in existing:
        op.create_table(
            'club_memberships',
            sa.Column('id', sa.String(length=36), primary_key=True),
            sa.Column('created_at', sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now()),
            sa.Column('updated_at', sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now()),
            sa.Column('is_deleted', sa.Boolean(), nullable=False, server_default='false'),
            sa.Column('deleted_at', sa.DateTime(timezone=True), nullable=True),
            sa.Column('club_id', sa.String(length=36), sa.ForeignKey('clubs.id'), nullable=False),
            sa.Column('user_id', sa.String(length=36), sa.ForeignKey('users.id'), nullable=False),
            sa.Column('role', sa.String(length=32), nullable=False, server_default='Member'),
            sa.UniqueConstraint('club_id', 'user_id', name='uq_club_membership_club_user'),
        )


def downgrade() -> None:
    bind = op.get_bind()
    inspector = sa.inspect(bind)
    existing = inspector.get_table_names()
    if 'club_memberships' in existing:
        op.drop_table('club_memberships')
    if 'clubs' in existing:
        op.drop_table('clubs')
