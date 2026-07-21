"""repair schema: create smart-classroom tables

Revision ID: a7c4e01b58d3
Revises: f6b3d92a4c17
Create Date: 2026-07-21

Three more tables defined on the models that no migration ever created:

    classroom_activities, session_summaries, student_interactions

Same root cause as c9f21a7de401 -- they existed only because app startup called
Base.metadata.create_all(). Found by tests/test_schema_integrity.py, which builds
a database from migrations alone and diffs it against the model metadata.
"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa

revision: str = 'a7c4e01b58d3'
down_revision: Union[str, None] = 'f6b3d92a4c17'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None

_TABLES = ['classroom_activities', 'session_summaries', 'student_interactions']


def upgrade() -> None:
    existing = set(sa.inspect(op.get_bind()).get_table_names())

    if 'classroom_activities' not in existing:
        op.create_table(
            'classroom_activities',
            sa.Column('faculty_id', sa.String(length=36), nullable=False),
            sa.Column('section_id', sa.String(length=36), nullable=False),
            sa.Column('activity_type', sa.String(length=64), nullable=False),
            sa.Column('topic', sa.String(length=255), nullable=False),
            sa.Column('duration_minutes', sa.Integer(), nullable=False),
            sa.Column('remarks', sa.Text(), nullable=True),
            sa.Column('id', sa.String(length=36), nullable=False),
            sa.Column('created_at', sa.DateTime(timezone=True), nullable=False),
            sa.Column('updated_at', sa.DateTime(timezone=True), nullable=False),
            sa.Column('is_deleted', sa.Boolean(), nullable=False),
            sa.Column('deleted_at', sa.DateTime(timezone=True), nullable=True),
            sa.ForeignKeyConstraint(['faculty_id'], ['users.id'], name=op.f('fk_classroom_activities_faculty_id_users')),
            sa.ForeignKeyConstraint(['section_id'], ['sections.id'], name=op.f('fk_classroom_activities_section_id_sections')),
            sa.PrimaryKeyConstraint('id', name=op.f('pk_classroom_activities')),
        )

    if 'session_summaries' not in existing:
        op.create_table(
            'session_summaries',
            sa.Column('faculty_id', sa.String(length=36), nullable=False),
            sa.Column('section_id', sa.String(length=36), nullable=False),
            sa.Column('subject_code', sa.String(length=32), nullable=False),
            sa.Column('topic_covered', sa.String(length=255), nullable=False),
            sa.Column('subtopic_covered', sa.String(length=255), nullable=True),
            sa.Column('teaching_method', sa.String(length=128), nullable=False),
            sa.Column('resources_used', sa.JSON(), nullable=True),
            sa.Column('remarks', sa.Text(), nullable=True),
            sa.Column('date', sa.Date(), nullable=False),
            sa.Column('id', sa.String(length=36), nullable=False),
            sa.Column('created_at', sa.DateTime(timezone=True), nullable=False),
            sa.Column('updated_at', sa.DateTime(timezone=True), nullable=False),
            sa.Column('is_deleted', sa.Boolean(), nullable=False),
            sa.Column('deleted_at', sa.DateTime(timezone=True), nullable=True),
            sa.ForeignKeyConstraint(['faculty_id'], ['users.id'], name=op.f('fk_session_summaries_faculty_id_users')),
            sa.ForeignKeyConstraint(['section_id'], ['sections.id'], name=op.f('fk_session_summaries_section_id_sections')),
            sa.PrimaryKeyConstraint('id', name=op.f('pk_session_summaries')),
        )

    if 'student_interactions' not in existing:
        op.create_table(
            'student_interactions',
            sa.Column('faculty_id', sa.String(length=36), nullable=False),
            sa.Column('section_id', sa.String(length=36), nullable=False),
            sa.Column('type', sa.String(length=32), nullable=False),
            sa.Column('question_text', sa.Text(), nullable=False),
            sa.Column('options', sa.JSON(), nullable=True),
            sa.Column('responses_count', sa.Integer(), nullable=False),
            sa.Column('is_active', sa.Boolean(), nullable=False),
            sa.Column('id', sa.String(length=36), nullable=False),
            sa.Column('created_at', sa.DateTime(timezone=True), nullable=False),
            sa.Column('updated_at', sa.DateTime(timezone=True), nullable=False),
            sa.Column('is_deleted', sa.Boolean(), nullable=False),
            sa.Column('deleted_at', sa.DateTime(timezone=True), nullable=True),
            sa.ForeignKeyConstraint(['faculty_id'], ['users.id'], name=op.f('fk_student_interactions_faculty_id_users')),
            sa.ForeignKeyConstraint(['section_id'], ['sections.id'], name=op.f('fk_student_interactions_section_id_sections')),
            sa.PrimaryKeyConstraint('id', name=op.f('pk_student_interactions')),
        )


def downgrade() -> None:
    existing = set(sa.inspect(op.get_bind()).get_table_names())
    for name in reversed(_TABLES):
        if name in existing:
            op.drop_table(name)
