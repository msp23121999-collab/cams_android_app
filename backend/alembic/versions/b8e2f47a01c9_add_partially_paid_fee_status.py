"""add PARTIALLY_PAID to the fee status enum

Revision ID: b8e2f47a01c9
Revises: a7c4e01b58d3
Create Date: 2026-07-21

The fee summary service computes and returns "partially_paid", but the stored
`fee_records.status` column had no such value, so a part-settled record had to sit
in PENDING. The two views of the same record therefore disagreed, and screens that
read the stored column could not distinguish "nothing paid yet" from "half paid".

Adding the value lets the stored status match the computed one. PostgreSQL enforces
enum membership, so the type must be extended before any code writes it — without
this migration the write fails with:

    invalid input value for enum fee_status: "partially_paid"

No-op on SQLite, which stores enums as plain text.
"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa

revision: str = 'b8e2f47a01c9'
down_revision: Union[str, None] = 'a7c4e01b58d3'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None

_ENUM_CANDIDATES = ('fee_status', 'feestatus')
# SQLAlchemy's Enum type persists member NAMES by default, which is why the
# existing labels are PAID / PENDING / OVERDUE rather than paid / pending /
# overdue. The new label must follow that convention or the column will reject
# what the ORM writes. Both spellings are added so databases that already ran an
# earlier revision of this migration converge on the same state.
_VALUES = ('PARTIALLY_PAID', 'partially_paid')


def upgrade() -> None:
    bind = op.get_bind()
    if bind.dialect.name != 'postgresql':
        return
    for type_name in _ENUM_CANDIDATES:
        exists = bind.execute(
            sa.text("SELECT 1 FROM pg_type WHERE typname = :n"), {"n": type_name}
        ).scalar()
        if exists:
            for value in _VALUES:
                op.execute(
                    sa.text(f"ALTER TYPE {type_name} ADD VALUE IF NOT EXISTS '{value}'")
                    .execution_options(autocommit=True)
                )


def downgrade() -> None:
    # PostgreSQL cannot remove a value from an enum type once rows may reference it.
    pass
