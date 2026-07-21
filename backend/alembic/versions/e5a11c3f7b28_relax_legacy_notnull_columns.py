"""relax legacy NOT NULL columns the models no longer define

Revision ID: e5a11c3f7b28
Revises: d4e77b1c9a02
Create Date: 2026-07-21

Three columns survive in the database as NOT NULL but are no longer part of the
SQLAlchemy models:

    attendance.student_id, attendance.status, academic_years.regulation_id

`attendance` moved from one-row-per-student to a bulk representation
(`absentee_ids` / `od_ids` on a section+subject+hour row), so nothing populates
`student_id` or `status` any more. Their NOT NULL constraints make every insert in
the current format fail with:

    IntegrityError: NOT NULL constraint failed: attendance.student_id

which is exactly what a fresh `alembic upgrade head` + seed hit. This was invisible
while app startup rebuilt tables from the models via create_all.

The columns are relaxed to NULLABLE rather than dropped: dropping would discard
any historical rows still holding that data, and a nullable legacy column is
harmless. Dropping them is a separate decision once the data is confirmed migrated.
"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa

revision: str = 'e5a11c3f7b28'
down_revision: Union[str, None] = 'd4e77b1c9a02'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None

_TARGETS = [
    ('attendance', 'student_id', sa.String(length=36)),
    ('attendance', 'status', sa.String(length=32)),
    ('academic_years', 'regulation_id', sa.String(length=36)),
]


def upgrade() -> None:
    bind = op.get_bind()
    inspector = sa.inspect(bind)
    tables = set(inspector.get_table_names())
    for table_name, column_name, coltype in _TARGETS:
        if table_name not in tables:
            continue
        cols = {c["name"]: c for c in inspector.get_columns(table_name)}
        if column_name not in cols or cols[column_name].get("nullable", True):
            continue
        # batch_alter_table so this also works on SQLite, which cannot ALTER a
        # column in place and needs a table rebuild.
        with op.batch_alter_table(table_name) as batch:
            batch.alter_column(column_name, existing_type=coltype, nullable=True)


def downgrade() -> None:
    # Deliberately not restoring NOT NULL: rows written since this migration
    # legitimately leave these legacy columns empty, so re-adding the constraint
    # would fail.
    pass
