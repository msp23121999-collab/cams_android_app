"""repair schema: add model columns that no migration ever added

Revision ID: d4e77b1c9a02
Revises: c9f21a7de401
Create Date: 2026-07-21

Companion to c9f21a7de401 (which created six entirely-missing tables). This adds
50 columns that exist on the SQLAlchemy models but that no migration ever added,
so a database built purely from `alembic upgrade head` was missing them.

They were present at runtime only because app startup ran
`Base.metadata.create_all()` plus a hand-rolled block of raw `ALTER TABLE`
statements in the lifespan handler. That startup block is what masked the gap.

Several of these are load-bearing:
  * students.section_id   - faculty roster scoping resolves taught sections through it
  * leaves.hod_status /
    hod_action_by/date    - the HOD -> Principal leave escalation chain
  * students.verification_status and the document_*_url columns - profile verification

Columns are added as NULLABLE even where the model declares NOT NULL: this runs
against tables that may already contain rows, where adding a NOT NULL column with
no default fails outright. Tightening these to NOT NULL requires back-filling
existing rows first and is deliberately left as a separate, data-dependent step.
"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa

revision: str = 'd4e77b1c9a02'
down_revision: Union[str, None] = 'c9f21a7de401'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    bind = op.get_bind()
    inspector = sa.inspect(bind)
    tables = set(inspector.get_table_names())

    # NOTE: string server_defaults must be quoted INSIDE sa.text(), e.g.
    #   sa.text("'DRAFT'")  ->  DEFAULT 'DRAFT'
    #   sa.text("DRAFT")     ->  DEFAULT DRAFT   (Postgres reads this as a
    #                              column reference and rejects it)
    # SQLite accepts the unquoted form, so this only fails on Postgres.
    def _add(table_name, column):
        if table_name not in tables:
            return
        existing = {c["name"] for c in inspector.get_columns(table_name)}
        if column.name not in existing:
            op.add_column(table_name, column)

    _add('courses', sa.Column('degree_id', sa.String(length=36), nullable=True))
    _add('timetable_approvals', sa.Column('rejection_remarks', sa.String(length=2048), nullable=True))
    _add('timetable_approvals', sa.Column('approved_date', sa.String(length=64), nullable=True))
    _add('timetable_approvals', sa.Column('rejected_by', sa.String(length=36), nullable=True))
    _add('timetable_approvals', sa.Column('rejected_date', sa.String(length=64), nullable=True))
    _add('academic_years', sa.Column('degree_id', sa.String(length=36), nullable=True))
    _add('attendance', sa.Column('subject_id', sa.String(length=36), nullable=True))
    _add('attendance', sa.Column('faculty_id', sa.String(length=36), nullable=True))
    _add('attendance', sa.Column('hour', sa.String(length=32), nullable=True))
    _add('attendance', sa.Column('absentee_ids', sa.JSON(), nullable=True))
    _add('attendance', sa.Column('od_ids', sa.JSON(), nullable=True))
    _add('chat_sessions', sa.Column('is_deleted', sa.Boolean(), server_default=sa.text('false'), nullable=True))
    _add('chat_messages', sa.Column('is_deleted', sa.Boolean(), server_default=sa.text('false'), nullable=True))
    _add('notices', sa.Column('event_date', sa.Date(), nullable=True))
    _add('notices', sa.Column('audience_types', sa.String(length=512), nullable=True))
    _add('notices', sa.Column('degree_id', sa.String(length=36), nullable=True))
    _add('notices', sa.Column('batch_id', sa.String(length=64), nullable=True))
    _add('notices', sa.Column('department_id', sa.String(length=36), nullable=True))
    _add('notices', sa.Column('attachment_url', sa.String(length=512), nullable=True))
    _add('faculty_profiles', sa.Column('faculty_id', sa.String(length=64), nullable=True))
    _add('faculty_profiles', sa.Column('community', sa.String(length=64), nullable=True))
    _add('publication_plans', sa.Column('research_area', sa.String(length=255), nullable=True))
    _add('publication_plans', sa.Column('publication_type', sa.String(length=128), nullable=True))
    _add('publication_plans', sa.Column('expected_publication_date', sa.Date(), nullable=True))
    _add('publication_plans', sa.Column('academic_year', sa.String(length=64), nullable=True))
    _add('leaves', sa.Column('hod_status', sa.String(length=32), nullable=True))
    _add('leaves', sa.Column('hod_action_by', sa.String(length=36), nullable=True))
    _add('leaves', sa.Column('hod_action_date', sa.DateTime(), nullable=True))
    _add('leaves', sa.Column('hod_remarks', sa.String(length=1024), nullable=True))
    _add('leaves', sa.Column('principal_action_by', sa.String(length=36), nullable=True))
    _add('leaves', sa.Column('principal_action_date', sa.DateTime(), nullable=True))
    _add('leaves', sa.Column('principal_remarks', sa.String(length=1024), nullable=True))
    _add('students', sa.Column('degree_id', sa.String(length=36), nullable=True))
    _add('students', sa.Column('moot_courts', sa.JSON(), nullable=True))
    _add('students', sa.Column('profile_photo_url', sa.String(length=1000), nullable=True))
    _add('students', sa.Column('section_id', sa.String(length=36), nullable=True))
    _add('students', sa.Column('verification_status', sa.String(length=64), server_default=sa.text("'DRAFT'"), nullable=True))
    _add('students', sa.Column('staff_remarks', sa.String(length=500), nullable=True))
    _add('students', sa.Column('hod_remarks', sa.String(length=500), nullable=True))
    _add('students', sa.Column('document_aadhaar_url', sa.String(length=1000), nullable=True))
    _add('students', sa.Column('document_community_url', sa.String(length=1000), nullable=True))
    _add('students', sa.Column('document_tc_url', sa.String(length=1000), nullable=True))
    _add('students', sa.Column('document_other_url', sa.String(length=1000), nullable=True))
    _add('students', sa.Column('edit_request_status', sa.String(length=64), nullable=True))
    _add('students', sa.Column('edit_request_reason', sa.String(length=1000), nullable=True))
    _add('study_materials', sa.Column('approved_by', sa.String(length=36), nullable=True))
    _add('study_materials', sa.Column('approved_date', sa.String(length=64), nullable=True))
    _add('study_materials', sa.Column('rejected_by', sa.String(length=36), nullable=True))
    _add('study_materials', sa.Column('rejected_date', sa.String(length=64), nullable=True))
    _add('study_materials', sa.Column('rejection_remarks', sa.String(length=2048), nullable=True))


def downgrade() -> None:
    # Intentionally a no-op: these columns should have existed all along, and
    # dropping them would discard data on any database that has been running with
    # them (which is every environment, via the old startup create_all path).
    pass
