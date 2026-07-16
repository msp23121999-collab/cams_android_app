"""initial schema

Revision ID: 20260603_0001
Revises:
Create Date: 2026-06-03
"""

from collections.abc import Sequence

from alembic import op

from app.db.base import Base
from app.db import models  # noqa: F401

# revision identifiers, used by Alembic.
revision: str = "20260603_0001"
down_revision: str | None = None
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    bind = op.get_bind()
    Base.metadata.create_all(bind=bind)


def downgrade() -> None:
    bind = op.get_bind()
    Base.metadata.drop_all(bind=bind)
