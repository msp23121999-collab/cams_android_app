"""add_razorpay_payment_fields

Revision ID: a3f1c9d2e701
Revises: da9e9b9c9d9f
Create Date: 2026-07-18 00:00:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa

# revision identifiers, used by Alembic.
revision: str = 'a3f1c9d2e701'
down_revision: Union[str, None] = 'da9e9b9c9d9f'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    bind = op.get_bind()
    inspector = sa.inspect(bind)
    columns = [c['name'] for c in inspector.get_columns('payments')]

    if 'razorpay_order_id' not in columns:
        op.add_column('payments', sa.Column('razorpay_order_id', sa.String(length=128), nullable=True))
    if 'razorpay_payment_id' not in columns:
        op.add_column('payments', sa.Column('razorpay_payment_id', sa.String(length=128), nullable=True))
    if 'razorpay_signature' not in columns:
        op.add_column('payments', sa.Column('razorpay_signature', sa.String(length=256), nullable=True))
    if 'status' not in columns:
        op.add_column('payments', sa.Column('status', sa.String(length=32), nullable=False, server_default='created'))


def downgrade() -> None:
    bind = op.get_bind()
    inspector = sa.inspect(bind)
    columns = [c['name'] for c in inspector.get_columns('payments')]

    if 'status' in columns:
        op.drop_column('payments', 'status')
    if 'razorpay_signature' in columns:
        op.drop_column('payments', 'razorpay_signature')
    if 'razorpay_payment_id' in columns:
        op.drop_column('payments', 'razorpay_payment_id')
    if 'razorpay_order_id' in columns:
        op.drop_column('payments', 'razorpay_order_id')
