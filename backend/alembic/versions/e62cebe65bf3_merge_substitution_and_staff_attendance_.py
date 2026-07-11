"""merge substitution and staff attendance branches

Revision ID: e62cebe65bf3
Revises: 22fdcf152b35, 5eebaa36d735
Create Date: 2026-06-12 13:14:48.461709

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = 'e62cebe65bf3'
down_revision: Union[str, None] = ('22fdcf152b35', '5eebaa36d735')
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    pass


def downgrade() -> None:
    pass
