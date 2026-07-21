"""add local_json_db, the table backing the JSON document stores

Revision ID: c2f9a6d80e14
Revises: b8e2f47a01c9
Create Date: 2026-07-21

`local_json_db` holds the JSON document stores (student papers, student council,
parent inquiries, college info, and the teaching-logs store) keyed by the legacy
filename. It was previously created by a raw `CREATE TABLE IF NOT EXISTS` issued
at runtime by app/core/json_db_helper.py, which put it outside Alembic's control
and outside every schema check.

Creating it here means the schema is owned entirely by migrations, consistent with
the rest of the database.

Those stores also used to be read and written as plain files inside the
application directory. That does not survive a container redeploy — the filesystem
is replaced, so submitted papers and parent inquiries were silently lost — and
concurrent writers overwrote each other's whole file. They now read and write this
table instead, seeding a row from the existing file on first access, so deployed
data persists.
"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa
from sqlalchemy.dialects import postgresql

revision: str = 'c2f9a6d80e14'
down_revision: Union[str, None] = 'b8e2f47a01c9'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    bind = op.get_bind()
    if 'local_json_db' in sa.inspect(bind).get_table_names():
        return
    # JSONB on PostgreSQL; TEXT elsewhere, matching what the helper writes.
    data_type = postgresql.JSONB() if bind.dialect.name == 'postgresql' else sa.Text()
    op.create_table(
        'local_json_db',
        sa.Column('key', sa.String(length=255), nullable=False),
        sa.Column('data', data_type, nullable=True),
        sa.Column('updated_at', sa.DateTime(), server_default=sa.func.now(), nullable=True),
        sa.PrimaryKeyConstraint('key', name=op.f('pk_local_json_db')),
    )


def downgrade() -> None:
    # Dropping this discards the JSON document stores (papers, council, parent
    # inquiries, teaching logs), so it is intentionally not automatic.
    pass
