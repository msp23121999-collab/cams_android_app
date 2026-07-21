"""add_grievance_subject_priority_resolution

Revision ID: a4b5c6d7e8f9
Revises: f3a4b5c6d7e8
Create Date: 2026-07-18 00:00:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa

# revision identifiers, used by Alembic.
revision: str = 'a4b5c6d7e8f9'
down_revision: Union[str, None] = 'f3a4b5c6d7e8'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    bind = op.get_bind()
    inspector = sa.inspect(bind)
    existing_columns = {col["name"] for col in inspector.get_columns("grievances")}

    if "subject" not in existing_columns:
        op.add_column("grievances", sa.Column("subject", sa.String(length=255), nullable=False, server_default="General"))
    if "priority" not in existing_columns:
        op.add_column("grievances", sa.Column("priority", sa.String(length=16), nullable=False, server_default="Medium"))
    if "resolution_date" not in existing_columns:
        op.add_column("grievances", sa.Column("resolution_date", sa.String(length=32), nullable=True))
    if "resolution_rating" not in existing_columns:
        op.add_column("grievances", sa.Column("resolution_rating", sa.Integer(), nullable=True))
    if "resolution_feedback" not in existing_columns:
        op.add_column("grievances", sa.Column("resolution_feedback", sa.String(length=2000), nullable=True))


def downgrade() -> None:
    bind = op.get_bind()
    inspector = sa.inspect(bind)
    existing_columns = {col["name"] for col in inspector.get_columns("grievances")}

    for col in ["resolution_feedback", "resolution_rating", "resolution_date", "priority", "subject"]:
        if col in existing_columns:
            op.drop_column("grievances", col)
