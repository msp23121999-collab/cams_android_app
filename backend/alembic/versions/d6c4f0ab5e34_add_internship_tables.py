"""add_internship_tables

Revision ID: d6c4f0ab5e34
Revises: c5b3e9fa4d23
Create Date: 2026-07-18 00:15:00.000000

"""
import uuid
from datetime import datetime, timezone
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa

# revision identifiers, used by Alembic.
revision: str = 'd6c4f0ab5e34'
down_revision: Union[str, None] = 'c5b3e9fa4d23'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None

# Continuity seed matching the previous hardcoded DEFAULT_DRIVES list from the
# JSON-file-based internship_drives endpoint.
DEFAULT_DRIVES = [
    {"company_name": "Shardul Amarchand Mangaldas", "role": "Associate - Corporate", "package": "₹18-20 LPA", "status": "Hiring"},
    {"company_name": "Trilegal", "role": "Disputes Intern", "package": "₹25k / Month", "status": "Closed"},
    {"company_name": "L&T Infrastructure", "role": "In-house Legal Counsel", "package": "₹12-15 LPA", "status": "Hiring"},
    {"company_name": "High Court of Delhi", "role": "Judicial Clerkship", "package": "₹40k / Month", "status": "Hiring"},
]


def upgrade() -> None:
    bind = op.get_bind()
    inspector = sa.inspect(bind)
    existing = inspector.get_table_names()

    if 'internship_drives' not in existing:
        op.create_table(
            'internship_drives',
            sa.Column('id', sa.String(length=36), primary_key=True),
            sa.Column('created_at', sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now()),
            sa.Column('updated_at', sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now()),
            sa.Column('is_deleted', sa.Boolean(), nullable=False, server_default='false'),
            sa.Column('deleted_at', sa.DateTime(timezone=True), nullable=True),
            sa.Column('company_name', sa.String(length=255), nullable=False),
            sa.Column('role', sa.String(length=255), nullable=False),
            sa.Column('package', sa.String(length=128), nullable=True),
            sa.Column('drive_date', sa.Date(), nullable=True),
            sa.Column('status', sa.String(length=32), nullable=False, server_default='Hiring'),
            sa.Column('description', sa.String(length=2048), nullable=True),
        )

        drives_table = sa.table(
            'internship_drives',
            sa.column('id', sa.String),
            # timezone=True must match the real column type. Declared naive, the
            # INSERT is cast to TIMESTAMP WITHOUT TIME ZONE and Postgres rejects the
            # timezone-aware values below ("can't subtract offset-naive and
            # offset-aware datetimes"). SQLite ignores the distinction entirely,
            # so this only failed on Postgres.
            sa.column('created_at', sa.DateTime(timezone=True)),
            sa.column('updated_at', sa.DateTime(timezone=True)),
            sa.column('is_deleted', sa.Boolean),
            sa.column('company_name', sa.String),
            sa.column('role', sa.String),
            sa.column('package', sa.String),
            sa.column('status', sa.String),
        )
        now = datetime.now(timezone.utc)
        op.bulk_insert(drives_table, [
            {
                'id': str(uuid.uuid4()),
                'created_at': now,
                'updated_at': now,
                'is_deleted': False,
                'company_name': d['company_name'],
                'role': d['role'],
                'package': d['package'],
                'status': d['status'],
            }
            for d in DEFAULT_DRIVES
        ])

    if 'internship_applications' not in existing:
        op.create_table(
            'internship_applications',
            sa.Column('id', sa.String(length=36), primary_key=True),
            sa.Column('created_at', sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now()),
            sa.Column('updated_at', sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now()),
            sa.Column('is_deleted', sa.Boolean(), nullable=False, server_default='false'),
            sa.Column('deleted_at', sa.DateTime(timezone=True), nullable=True),
            sa.Column('drive_id', sa.String(length=36), sa.ForeignKey('internship_drives.id'), nullable=False),
            sa.Column('student_id', sa.String(length=36), sa.ForeignKey('students.id'), nullable=False),
            sa.Column('status', sa.String(length=32), nullable=False, server_default='Applied'),
            sa.UniqueConstraint('drive_id', 'student_id', name='uq_internship_application_drive_student'),
        )


def downgrade() -> None:
    bind = op.get_bind()
    inspector = sa.inspect(bind)
    existing = inspector.get_table_names()
    if 'internship_applications' in existing:
        op.drop_table('internship_applications')
    if 'internship_drives' in existing:
        op.drop_table('internship_drives')
