"""sync PostgreSQL enum types with the values the models actually use

Revision ID: f6b3d92a4c17
Revises: e5a11c3f7b28
Create Date: 2026-07-21

Three enum types in the database were missing values that the models (and the
application code) actively write:

    deduction_type          missing: PF
    leave_status            missing: the entire HOD/Principal workflow
    leave_approval_status   missing: the entire HOD/Principal workflow

This is a Postgres-only failure mode and a serious one. PostgreSQL enforces enum
membership, so writing e.g. 'APPROVED_BY_HOD' raises

    InvalidTextRepresentationError: invalid input value for enum leave_status

meaning the HOD -> Principal leave escalation could not work at all on Postgres.
SQLite stores enums as plain text and accepts any string, so every one of these
went unnoticed in development, where the app runs on SQLite.

`ALTER TYPE ... ADD VALUE IF NOT EXISTS` is used so the migration is idempotent.
It is a no-op on SQLite, which has no enum types.
"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa

revision: str = 'f6b3d92a4c17'
down_revision: Union[str, None] = 'e5a11c3f7b28'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None

_WORKFLOW_STATUSES = [
    'PENDING_HOD',
    'PENDING_PRINCIPAL',
    'APPROVED_BY_HOD',
    'REJECTED_BY_HOD',
    'HOD_REJECTED',
    'REJECTED_BY_PRINCIPAL',
    'PRINCIPAL_REJECTED',
    'REJECTED_BY_ADVISOR',
    'REJECTED_BY_FACULTY',
    'FINAL_APPROVED',
    'CANCELLED',
]

_ENUM_VALUES = {
    'deduction_type': ['PF'],
    'leave_status': _WORKFLOW_STATUSES,
    'leave_approval_status': _WORKFLOW_STATUSES,
}


def upgrade() -> None:
    bind = op.get_bind()
    if bind.dialect.name != 'postgresql':
        # SQLite (and other backends here) represent enums as VARCHAR.
        return

    for type_name, values in _ENUM_VALUES.items():
        exists = bind.execute(
            sa.text("SELECT 1 FROM pg_type WHERE typname = :n"), {"n": type_name}
        ).scalar()
        if not exists:
            continue
        for value in values:
            # Quoting via a literal is required: ADD VALUE does not accept bind
            # parameters. Values here are hard-coded constants, not user input.
            op.execute(
                sa.text(
                    f"ALTER TYPE {type_name} ADD VALUE IF NOT EXISTS '{value}'"
                ).execution_options(autocommit=True)
            )


def downgrade() -> None:
    # PostgreSQL cannot remove a value from an enum type; rows may already use it.
    pass
