"""add_email_change_and_password_reset

Revision ID: b7c1a2d3e4f5
Revises: e7d5a1bc6f45
Create Date: 2026-07-18 00:00:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa

# revision identifiers, used by Alembic.
revision: str = 'b7c1a2d3e4f5'
down_revision: Union[str, None] = 'e7d5a1bc6f45'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    bind = op.get_bind()
    inspector = sa.inspect(bind)

    # --- users: email-change verification columns ---
    user_columns = [c['name'] for c in inspector.get_columns('users')]

    if 'pending_email' not in user_columns:
        op.add_column('users', sa.Column('pending_email', sa.String(length=255), nullable=True))
    if 'email_change_token' not in user_columns:
        op.add_column('users', sa.Column('email_change_token', sa.String(length=128), nullable=True))
        op.create_index(
            op.f('ix_users_email_change_token'), 'users', ['email_change_token'], unique=False
        )
    if 'email_change_token_expires_at' not in user_columns:
        op.add_column(
            'users', sa.Column('email_change_token_expires_at', sa.DateTime(timezone=True), nullable=True)
        )

    # --- password_reset_tokens table ---
    existing_tables = inspector.get_table_names()
    if 'password_reset_tokens' not in existing_tables:
        op.create_table(
            'password_reset_tokens',
            sa.Column('id', sa.String(length=36), primary_key=True),
            sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
            sa.Column('updated_at', sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
            sa.Column('is_deleted', sa.Boolean(), server_default='false', nullable=False),
            sa.Column('deleted_at', sa.DateTime(timezone=True), nullable=True),
            sa.Column('user_id', sa.String(length=36), sa.ForeignKey('users.id'), nullable=False),
            sa.Column('token', sa.String(length=128), nullable=False),
            sa.Column('expires_at', sa.DateTime(timezone=True), nullable=False),
            sa.Column('used_at', sa.DateTime(timezone=True), nullable=True),
        )
        op.create_index(
            op.f('ix_password_reset_tokens_token'), 'password_reset_tokens', ['token'], unique=True
        )


def downgrade() -> None:
    bind = op.get_bind()
    inspector = sa.inspect(bind)

    existing_tables = inspector.get_table_names()
    if 'password_reset_tokens' in existing_tables:
        op.drop_index(op.f('ix_password_reset_tokens_token'), table_name='password_reset_tokens')
        op.drop_table('password_reset_tokens')

    user_columns = [c['name'] for c in inspector.get_columns('users')]
    if 'email_change_token_expires_at' in user_columns:
        op.drop_column('users', 'email_change_token_expires_at')
    if 'email_change_token' in user_columns:
        op.drop_index(op.f('ix_users_email_change_token'), table_name='users')
        op.drop_column('users', 'email_change_token')
    if 'pending_email' in user_columns:
        op.drop_column('users', 'pending_email')
