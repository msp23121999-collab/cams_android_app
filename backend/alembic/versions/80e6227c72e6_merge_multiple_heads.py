"""merge multiple heads

Revision ID: 80e6227c72e6
Revises: 25a299395265, 3a8c1f9e2b10, 3ded23219de6, 5904bfd0e404, 5d1bb6155b93, a1b2c3d4e5f6
Create Date: 2026-06-17 06:33:24.115400

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = '80e6227c72e6'
down_revision: Union[str, None] = ('25a299395265', '3a8c1f9e2b10', '3ded23219de6', '5904bfd0e404', '5d1bb6155b93', 'a1b2c3d4e5f6')
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    pass


def downgrade() -> None:
    pass
