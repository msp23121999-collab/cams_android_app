"""add device_tokens for push notifications

Revision ID: d1a8c47f62b3
Revises: c2f9a6d80e14
Create Date: 2026-07-21

Stores one FCM registration token per app installation so the server can send push
notifications. Previously the Android app received a token in `onNewToken` and
discarded it, so server-originated push could not work at all.

The token is unique rather than the user: a user may have several devices, and a
device that changes hands is re-pointed at whoever signs in next.
"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa

revision: str = 'd1a8c47f62b3'
down_revision: Union[str, None] = 'c2f9a6d80e14'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    if 'device_tokens' in sa.inspect(op.get_bind()).get_table_names():
        return
    op.create_table(
        'device_tokens',
        sa.Column('user_id', sa.String(length=36), nullable=False),
        sa.Column('token', sa.String(length=512), nullable=False),
        sa.Column('platform', sa.String(length=16), nullable=False),
        sa.Column('last_seen_at', sa.DateTime(timezone=True), nullable=True),
        sa.Column('id', sa.String(length=36), nullable=False),
        sa.Column('created_at', sa.DateTime(timezone=True), nullable=False),
        sa.Column('updated_at', sa.DateTime(timezone=True), nullable=False),
        sa.Column('is_deleted', sa.Boolean(), nullable=False),
        sa.Column('deleted_at', sa.DateTime(timezone=True), nullable=True),
        sa.ForeignKeyConstraint(['user_id'], ['users.id'], name=op.f('fk_device_tokens_user_id_users')),
        sa.PrimaryKeyConstraint('id', name=op.f('pk_device_tokens')),
        sa.UniqueConstraint('token', name=op.f('uq_device_tokens_token')),
    )
    op.create_index(op.f('ix_device_tokens_user_id'), 'device_tokens', ['user_id'])


def downgrade() -> None:
    if 'device_tokens' in sa.inspect(op.get_bind()).get_table_names():
        op.drop_index(op.f('ix_device_tokens_user_id'), table_name='device_tokens')
        op.drop_table('device_tokens')
