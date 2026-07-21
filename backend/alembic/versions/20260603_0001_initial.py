"""initial schema (no-op placeholder)

Revision ID: 20260603_0001
Revises:
Create Date: 2026-06-03

HISTORY / WHY THIS IS EMPTY
---------------------------
This revision originally called ``Base.metadata.create_all()``, i.e. it created a
snapshot of whatever the *current* ORM models looked like at the moment it ran.
That is fundamentally incompatible with an ordered migration chain: on a fresh
database it created every table in the model set up front, so every later
migration in the chain (``4f047b6c641c`` add backup models, ``ed0877622267``
system_setting_history, the PF tables, and so on) then failed with
"table ... already exists".

The real, explicit initial schema for this project is revision
``25e8f96d5925_initial`` (a separate root of the chain, which merges back in
later). That one creates tables with real, versioned DDL.

This revision is kept — rather than deleted — because it has already been
applied/stamped in existing deployments, and removing a revision id that exists
in a deployed ``alembic_version`` table would break those databases. Its
``upgrade()`` is now a no-op so the chain is reproducible from empty.
"""

from collections.abc import Sequence

# revision identifiers, used by Alembic.
revision: str = "20260603_0001"
down_revision: str | None = None
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    """No-op. See module docstring — schema comes from 25e8f96d5925 onwards."""


def downgrade() -> None:
    """No-op."""
