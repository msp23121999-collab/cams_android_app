"""add_scholarship_type_id

Revision ID: da9e9b9c9d9f
Revises: 80e6227c72e6
Create Date: 2026-06-24 15:22:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa

# revision identifiers, used by Alembic.
revision: str = 'da9e9b9c9d9f'
down_revision: Union[str, None] = '80e6227c72e6'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    bind = op.get_bind()
    inspector = sa.inspect(bind)
    columns = [c['name'] for c in inspector.get_columns('students')]
    
    if 'scholarship_type_id' not in columns:
        op.add_column('students', sa.Column('scholarship_type_id', sa.String(length=128), nullable=True))


def downgrade() -> None:
    bind = op.get_bind()
    inspector = sa.inspect(bind)
    columns = [c['name'] for c in inspector.get_columns('students')]
    
    if 'scholarship_type_id' in columns:
        op.drop_column('students', 'scholarship_type_id')
