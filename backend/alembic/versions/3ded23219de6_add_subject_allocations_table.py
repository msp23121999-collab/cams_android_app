"""Add subject_allocations table

Revision ID: 3ded23219de6
Revises: bc7600b612d3
Create Date: 2026-06-13 21:28:13.202217

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = '3ded23219de6'
down_revision: Union[str, None] = '25e8f96d5925'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    pass


def downgrade() -> None:
    pass
