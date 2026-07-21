"""expand_assignments_and_submissions

Revision ID: e7d5a1bc6f45
Revises: d6c4f0ab5e34
Create Date: 2026-07-18 00:20:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa

# revision identifiers, used by Alembic.
revision: str = 'e7d5a1bc6f45'
down_revision: Union[str, None] = 'd6c4f0ab5e34'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


NEW_ASSIGNMENT_COLUMNS = [
    ('type', sa.String(length=64)),
    ('subject', sa.String(length=255)),
    ('unit', sa.String(length=128)),
    ('topic', sa.String(length=255)),
    ('description', sa.String(length=4000)),
    ('instructions', sa.String(length=4000)),
    ('total_marks', sa.Integer()),
    ('semester', sa.String(length=32)),
    ('section', sa.String(length=64)),
    ('attachments', sa.String(length=4000)),
]


def upgrade() -> None:
    bind = op.get_bind()
    inspector = sa.inspect(bind)
    existing_tables = inspector.get_table_names()

    if 'assignments' in existing_tables:
        columns = [c['name'] for c in inspector.get_columns('assignments')]
        for col_name, col_type in NEW_ASSIGNMENT_COLUMNS:
            if col_name not in columns:
                op.add_column('assignments', sa.Column(col_name, col_type, nullable=True))
        if 'status' not in columns:
            op.add_column('assignments', sa.Column('status', sa.String(length=32), nullable=False, server_default='Draft'))

    if 'assignment_submissions' not in existing_tables:
        op.create_table(
            'assignment_submissions',
            sa.Column('id', sa.String(length=36), primary_key=True),
            sa.Column('created_at', sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now()),
            sa.Column('updated_at', sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now()),
            sa.Column('is_deleted', sa.Boolean(), nullable=False, server_default='false'),
            sa.Column('deleted_at', sa.DateTime(timezone=True), nullable=True),
            sa.Column('assignment_id', sa.String(length=36), sa.ForeignKey('assignments.id'), nullable=False),
            sa.Column('student_id', sa.String(length=36), sa.ForeignKey('students.id'), nullable=False),
            sa.Column('submitted_file_url', sa.String(length=512), nullable=True),
            sa.Column('submitted_text', sa.String(length=4000), nullable=True),
            sa.Column('marks_obtained', sa.Float(), nullable=True),
            sa.Column('grade', sa.String(length=16), nullable=True),
            sa.Column('feedback', sa.String(length=2048), nullable=True),
            sa.Column('remarks', sa.String(length=2048), nullable=True),
            sa.Column('status', sa.String(length=32), nullable=False, server_default='Submitted'),
            sa.Column('submitted_at', sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now()),
        )


def downgrade() -> None:
    bind = op.get_bind()
    inspector = sa.inspect(bind)
    existing_tables = inspector.get_table_names()

    if 'assignment_submissions' in existing_tables:
        op.drop_table('assignment_submissions')

    if 'assignments' in existing_tables:
        columns = [c['name'] for c in inspector.get_columns('assignments')]
        if 'status' in columns:
            op.drop_column('assignments', 'status')
        for col_name, _ in NEW_ASSIGNMENT_COLUMNS:
            if col_name in columns:
                op.drop_column('assignments', col_name)
