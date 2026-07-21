"""add_club_announcements_table

Revision ID: b5c6d7e8f9a0
Revises: a4b5c6d7e8f9
Create Date: 2026-07-18 00:00:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa

# revision identifiers, used by Alembic.
revision: str = 'b5c6d7e8f9a0'
down_revision: Union[str, None] = 'a4b5c6d7e8f9'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    bind = op.get_bind()
    inspector = sa.inspect(bind)
    existing_tables = set(inspector.get_table_names())

    if 'club_announcements' not in existing_tables:
        op.create_table(
            'club_announcements',
            sa.Column('club_id', sa.String(length=36), nullable=False),
            sa.Column('posted_by', sa.String(length=36), nullable=False),
            sa.Column('title', sa.String(length=255), nullable=False),
            sa.Column('is_urgent', sa.Boolean(), nullable=False, server_default='0'),
            sa.Column('id', sa.String(length=36), nullable=False),
            sa.Column('created_at', sa.DateTime(timezone=True), nullable=False),
            sa.Column('updated_at', sa.DateTime(timezone=True), nullable=False),
            sa.Column('is_deleted', sa.Boolean(), nullable=False),
            sa.Column('deleted_at', sa.DateTime(timezone=True), nullable=True),
            sa.ForeignKeyConstraint(['club_id'], ['clubs.id'], name=op.f('fk_club_announcements_club_id_clubs')),
            sa.ForeignKeyConstraint(['posted_by'], ['users.id'], name=op.f('fk_club_announcements_posted_by_users')),
            sa.PrimaryKeyConstraint('id', name=op.f('pk_club_announcements')),
        )


def downgrade() -> None:
    bind = op.get_bind()
    inspector = sa.inspect(bind)
    if 'club_announcements' in set(inspector.get_table_names()):
        op.drop_table('club_announcements')
