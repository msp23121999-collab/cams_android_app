"""Idempotency helpers for migrations.

Historically this project had two migration roots and a `create_all()`-style
"initial" migration, which meant several tables/columns are created by more than
one revision. Rather than rewriting already-deployed revisions, later revisions
use these helpers so that `alembic upgrade head` is safe on both a genuinely
empty database and on a database whose schema predates the chain repair.
"""

from __future__ import annotations

import sqlalchemy as sa
from alembic import op


def _inspector():
    return sa.inspect(op.get_bind())


def has_table(name: str) -> bool:
    return _inspector().has_table(name)


def has_column(table: str, column: str) -> bool:
    insp = _inspector()
    if not insp.has_table(table):
        return False
    return column in {c["name"] for c in insp.get_columns(table)}


def has_index(table: str, index: str) -> bool:
    insp = _inspector()
    if not insp.has_table(table):
        return False
    return index in {i["name"] for i in insp.get_indexes(table)}
