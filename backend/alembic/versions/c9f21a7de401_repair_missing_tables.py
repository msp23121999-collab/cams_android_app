"""repair schema: create tables that no migration ever created

Revision ID: c9f21a7de401
Revises: 7a1c2f9e4b6d
Create Date: 2026-07-21

Six tables were defined in the SQLAlchemy models but created by NO migration, so
`alembic upgrade head` produced an incomplete schema:

    attendance_corrections, degrees, faculty_profile_update_requests,
    internal_marks, mentorship_records, subject_allocations

They existed in running environments only because app startup called
`Base.metadata.create_all(checkfirst=True)`, which silently papered over the gap.
`internal_marks` backs faculty marks entry and `subject_allocations` backs subject
allocation, so a database built purely from migrations was missing core tables.

(`3ded23219de6_add_subject_allocations_table.py` exists but its upgrade() body is
`pass` -- the revision was created and never implemented.)

Each create is guarded, so this is safe on existing databases where create_all had
already produced the tables.
"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa

revision: str = 'c9f21a7de401'
down_revision: Union[str, None] = '7a1c2f9e4b6d'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None

_TABLES = [
    'attendance_corrections',
    'degrees',
    'faculty_profile_update_requests',
    'internal_marks',
    'mentorship_records',
    'subject_allocations',
]


def upgrade() -> None:
    existing = set(sa.inspect(op.get_bind()).get_table_names())

    if 'attendance_corrections' not in existing:
        op.create_table(
            'attendance_corrections',
            sa.Column('student_reg_no', sa.String(length=64), nullable=False),
            sa.Column('student_name', sa.String(length=256), nullable=False),
            sa.Column('subject', sa.String(length=256), nullable=False),
            sa.Column('date', sa.Date(), nullable=False),
            sa.Column('previous_status', sa.String(length=64), nullable=False),
            sa.Column('updated_status', sa.String(length=64), nullable=False),
            sa.Column('reason', sa.String(length=1024), nullable=False),
            sa.Column('status', sa.String(length=64), nullable=False),
            sa.Column('remarks', sa.String(length=1024), nullable=True),
            sa.Column('id', sa.String(length=36), nullable=False),
            sa.Column('created_at', sa.DateTime(timezone=True), nullable=False),
            sa.Column('updated_at', sa.DateTime(timezone=True), nullable=False),
            sa.Column('is_deleted', sa.Boolean(), nullable=False),
            sa.Column('deleted_at', sa.DateTime(timezone=True), nullable=True),
            sa.PrimaryKeyConstraint('id', name=op.f('pk_attendance_corrections')),
        )

    if 'degrees' not in existing:
        op.create_table(
            'degrees',
            sa.Column('code', sa.String(length=32), nullable=False),
            sa.Column('name', sa.String(length=255), nullable=False),
            sa.Column('applicable_batch', sa.String(length=128), nullable=False),
            sa.Column('program_level', sa.String(length=32), nullable=False),
            sa.Column('duration_years', sa.Integer(), nullable=False),
            sa.Column('dept_id', sa.String(length=36), nullable=True),
            sa.Column('credit_pattern', sa.String(length=255), nullable=True),
            sa.Column('exam_formula', sa.String(length=255), nullable=True),
            sa.Column('passing_marks', sa.Integer(), nullable=False),
            sa.Column('grade_boundaries', sa.String(length=2000), nullable=True),
            sa.Column('id', sa.String(length=36), nullable=False),
            sa.Column('created_at', sa.DateTime(timezone=True), nullable=False),
            sa.Column('updated_at', sa.DateTime(timezone=True), nullable=False),
            sa.Column('is_deleted', sa.Boolean(), nullable=False),
            sa.Column('deleted_at', sa.DateTime(timezone=True), nullable=True),
            sa.ForeignKeyConstraint(['dept_id'], ['departments.id'], name=op.f('fk_degrees_dept_id_departments')),
            sa.PrimaryKeyConstraint('id', name=op.f('pk_degrees')),
        )

    if 'faculty_profile_update_requests' not in existing:
        op.create_table(
            'faculty_profile_update_requests',
            sa.Column('user_id', sa.String(length=36), nullable=False),
            sa.Column('status', sa.String(length=64), nullable=False),
            sa.Column('faculty_id', sa.String(length=64), nullable=True),
            sa.Column('employee_code', sa.String(length=64), nullable=True),
            sa.Column('official_email', sa.String(length=255), nullable=True),
            sa.Column('official_phone', sa.String(length=32), nullable=True),
            sa.Column('gender', sa.String(length=16), nullable=True),
            sa.Column('date_of_birth', sa.Date(), nullable=True),
            sa.Column('blood_group', sa.String(length=16), nullable=True),
            sa.Column('nationality', sa.String(length=64), nullable=True),
            sa.Column('designation', sa.String(length=64), nullable=True),
            sa.Column('department_name', sa.String(length=255), nullable=True),
            sa.Column('comments', sa.String(length=512), nullable=True),
            sa.Column('processed_at', sa.DateTime(timezone=True), nullable=True),
            sa.Column('processed_by', sa.String(length=36), nullable=True),
            sa.Column('id', sa.String(length=36), nullable=False),
            sa.Column('created_at', sa.DateTime(timezone=True), nullable=False),
            sa.Column('updated_at', sa.DateTime(timezone=True), nullable=False),
            sa.Column('is_deleted', sa.Boolean(), nullable=False),
            sa.Column('deleted_at', sa.DateTime(timezone=True), nullable=True),
            sa.ForeignKeyConstraint(['processed_by'], ['users.id'], name=op.f('fk_faculty_profile_update_requests_processed_by_users')),
            sa.ForeignKeyConstraint(['user_id'], ['users.id'], name=op.f('fk_faculty_profile_update_requests_user_id_users')),
            sa.PrimaryKeyConstraint('id', name=op.f('pk_faculty_profile_update_requests')),
        )

    if 'internal_marks' not in existing:
        op.create_table(
            'internal_marks',
            sa.Column('student_id', sa.String(length=36), nullable=False),
            sa.Column('section_id', sa.String(length=36), nullable=False),
            sa.Column('subject_id', sa.String(length=36), nullable=False),
            sa.Column('academic_year', sa.String(length=32), nullable=False),
            sa.Column('semester', sa.String(length=32), nullable=True),
            sa.Column('internal_exam_mark', sa.Numeric(precision=5, scale=2), nullable=False),
            sa.Column('assignment_mark', sa.Numeric(precision=5, scale=2), nullable=False),
            sa.Column('presentation_mark', sa.Numeric(precision=5, scale=2), nullable=False),
            sa.Column('viva_voice_mark', sa.Numeric(precision=5, scale=2), nullable=False),
            sa.Column('attendance_mark', sa.Numeric(precision=5, scale=2), nullable=False),
            sa.Column('total_mark', sa.Numeric(precision=5, scale=2), nullable=False),
            sa.Column('status', sa.String(length=32), nullable=False),
            sa.Column('hod_message', sa.String(length=1024), nullable=True),
            sa.Column('faculty_reply', sa.String(length=1024), nullable=True),
            sa.Column('is_message_visible_to_student', sa.Boolean(), nullable=False),
            sa.Column('id', sa.String(length=36), nullable=False),
            sa.Column('created_at', sa.DateTime(timezone=True), nullable=False),
            sa.Column('updated_at', sa.DateTime(timezone=True), nullable=False),
            sa.Column('is_deleted', sa.Boolean(), nullable=False),
            sa.Column('deleted_at', sa.DateTime(timezone=True), nullable=True),
            sa.ForeignKeyConstraint(['student_id'], ['students.id'], name=op.f('fk_internal_marks_student_id_students')),
            sa.ForeignKeyConstraint(['section_id'], ['sections.id'], name=op.f('fk_internal_marks_section_id_sections')),
            sa.ForeignKeyConstraint(['subject_id'], ['courses.id'], name=op.f('fk_internal_marks_subject_id_courses')),
            sa.PrimaryKeyConstraint('id', name=op.f('pk_internal_marks')),
        )

    if 'mentorship_records' not in existing:
        op.create_table(
            'mentorship_records',
            sa.Column('student_id', sa.String(length=36), nullable=False),
            sa.Column('mentor_id', sa.String(length=36), nullable=False),
            sa.Column('meeting_log', sa.String(length=4000), nullable=True),
            sa.Column('academic_review', sa.String(length=4000), nullable=True),
            sa.Column('improvement_plan', sa.String(length=4000), nullable=True),
            sa.Column('remarks', sa.String(length=4000), nullable=True),
            sa.Column('follow_up', sa.String(length=4000), nullable=True),
            sa.Column('id', sa.String(length=36), nullable=False),
            sa.Column('created_at', sa.DateTime(timezone=True), nullable=False),
            sa.Column('updated_at', sa.DateTime(timezone=True), nullable=False),
            sa.Column('is_deleted', sa.Boolean(), nullable=False),
            sa.Column('deleted_at', sa.DateTime(timezone=True), nullable=True),
            sa.ForeignKeyConstraint(['student_id'], ['students.id'], name=op.f('fk_mentorship_records_student_id_students')),
            sa.ForeignKeyConstraint(['mentor_id'], ['users.id'], name=op.f('fk_mentorship_records_mentor_id_users')),
            sa.PrimaryKeyConstraint('id', name=op.f('pk_mentorship_records')),
        )

    if 'subject_allocations' not in existing:
        op.create_table(
            'subject_allocations',
            sa.Column('academic_year_id', sa.String(length=36), nullable=False),
            sa.Column('course_id', sa.String(length=36), nullable=False),
            sa.Column('section_id', sa.String(length=36), nullable=False),
            sa.Column('faculty_id', sa.String(length=36), nullable=False),
            sa.Column('department_id', sa.String(length=36), nullable=False),
            sa.Column('semester', sa.Integer(), nullable=True),
            sa.Column('allocated_by', sa.String(length=36), nullable=True),
            sa.Column('is_active', sa.Boolean(), nullable=False),
            sa.Column('id', sa.String(length=36), nullable=False),
            sa.Column('created_at', sa.DateTime(timezone=True), nullable=False),
            sa.Column('updated_at', sa.DateTime(timezone=True), nullable=False),
            sa.Column('is_deleted', sa.Boolean(), nullable=False),
            sa.Column('deleted_at', sa.DateTime(timezone=True), nullable=True),
            sa.ForeignKeyConstraint(['faculty_id'], ['users.id'], name=op.f('fk_subject_allocations_faculty_id_users')),
            sa.ForeignKeyConstraint(['section_id'], ['sections.id'], name=op.f('fk_subject_allocations_section_id_sections')),
            sa.ForeignKeyConstraint(['department_id'], ['departments.id'], name=op.f('fk_subject_allocations_department_id_departments')),
            sa.ForeignKeyConstraint(['course_id'], ['courses.id'], name=op.f('fk_subject_allocations_course_id_courses')),
            sa.ForeignKeyConstraint(['allocated_by'], ['users.id'], name=op.f('fk_subject_allocations_allocated_by_users')),
            sa.ForeignKeyConstraint(['academic_year_id'], ['academic_years.id'], name=op.f('fk_subject_allocations_academic_year_id_academic_years')),
            sa.PrimaryKeyConstraint('id', name=op.f('pk_subject_allocations')),
        )


def downgrade() -> None:
    existing = set(sa.inspect(op.get_bind()).get_table_names())
    for name in reversed(_TABLES):
        if name in existing:
            op.drop_table(name)
