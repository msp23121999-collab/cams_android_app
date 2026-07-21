"""Add notice fields, notice_acknowledgements, class_advisors; update regulation constraint

Revision ID: a1b2c3d4e5f6
Revises: 846e783a199c
Create Date: 2026-06-16 19:59:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = 'a1b2c3d4e5f6'
down_revision: Union[str, None] = '846e783a199c'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    bind = op.get_bind()
    inspector = sa.inspect(bind)

    # ── 1. Add new columns to `notices` table (Preethi's changes) ──────────────
    notice_columns = {c['name'] for c in inspector.get_columns('notices')}
    with op.batch_alter_table('notices', schema=None) as batch_op:
        if 'category' not in notice_columns:
            batch_op.add_column(sa.Column('category', sa.String(length=64), nullable=True))
        if 'expiry_date' not in notice_columns:
            batch_op.add_column(sa.Column('expiry_date', sa.Date(), nullable=True))
        if 'priority' not in notice_columns:
            batch_op.add_column(sa.Column('priority', sa.String(length=32), nullable=True))
        if 'status' not in notice_columns:
            batch_op.add_column(sa.Column('status', sa.String(length=32), nullable=True))
        if 'publisher_role' not in notice_columns:
            batch_op.add_column(sa.Column('publisher_role', sa.String(length=32), nullable=True))
        if 'target_department' not in notice_columns:
            batch_op.add_column(sa.Column('target_department', sa.String(length=64), nullable=True))
        if 'target_semester' not in notice_columns:
            batch_op.add_column(sa.Column('target_semester', sa.Integer(), nullable=True))
        if 'target_section' not in notice_columns:
            batch_op.add_column(sa.Column('target_section', sa.String(length=64), nullable=True))

    # ── 2. Create `notice_acknowledgements` table (Preethi's changes) ──────────
    if 'notice_acknowledgements' not in inspector.get_table_names():
        op.create_table(
            'notice_acknowledgements',
            sa.Column('notice_id', sa.String(length=36), nullable=False),
            sa.Column('user_id', sa.String(length=36), nullable=False),
            sa.Column('is_read', sa.Boolean(), nullable=False),
            sa.Column('read_at', sa.DateTime(), nullable=True),
            sa.Column('is_acknowledged', sa.Boolean(), nullable=False),
            sa.Column('acknowledged_at', sa.DateTime(), nullable=True),
            sa.Column('status', sa.String(length=32), nullable=False),
            sa.Column('is_archived', sa.Boolean(), nullable=False),
            sa.Column('archived_at', sa.DateTime(), nullable=True),
            sa.Column('id', sa.String(length=36), nullable=False),
            sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
            sa.Column('updated_at', sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
            sa.Column('is_deleted', sa.Boolean(), nullable=False, server_default=sa.text('false')),
            sa.Column('deleted_at', sa.DateTime(timezone=True), nullable=True),
            sa.ForeignKeyConstraint(['notice_id'], ['notices.id'], name=op.f('fk_notice_acknowledgements_notice_id_notices')),
            sa.ForeignKeyConstraint(['user_id'], ['users.id'], name=op.f('fk_notice_acknowledgements_user_id_users')),
            sa.PrimaryKeyConstraint('id', name=op.f('pk_notice_acknowledgements')),
        )

    # ── 3. Create `class_advisors` table (Bhuvanesh's changes) ─────────────────
    if 'class_advisors' not in inspector.get_table_names():
        op.create_table(
            'class_advisors',
            sa.Column('academic_year_id', sa.String(length=36), nullable=False),
            sa.Column('faculty_id', sa.String(length=36), nullable=False),
            sa.Column('department_id', sa.String(length=36), nullable=False),
            sa.Column('batch', sa.String(length=128), nullable=False),
            sa.Column('section_name', sa.String(length=32), nullable=False),
            sa.Column('id', sa.String(length=36), nullable=False),
            sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
            sa.Column('updated_at', sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
            sa.Column('is_deleted', sa.Boolean(), nullable=False, server_default=sa.text('false')),
            sa.Column('deleted_at', sa.DateTime(timezone=True), nullable=True),
            sa.ForeignKeyConstraint(['academic_year_id'], ['academic_years.id'], name=op.f('fk_class_advisors_academic_year_id_academic_years')),
            sa.ForeignKeyConstraint(['department_id'], ['departments.id'], name=op.f('fk_class_advisors_department_id_departments')),
            sa.ForeignKeyConstraint(['faculty_id'], ['users.id'], name=op.f('fk_class_advisors_faculty_id_users')),
            sa.PrimaryKeyConstraint('id', name=op.f('pk_class_advisors')),
        )

    # ── 4. Update Regulation unique constraint (Bhuvanesh's changes) ────────────
    # Drop old constraint and add new one that includes program_level.
    # NOTE: the real dev DB never actually had the 'regulations' table created
    # (the 25e8f96d5925_initial migration that defines it was stamped as already
    # applied without ever having run against this DB), so guard table-creation
    # here too — creating it directly with the final desired unique constraint
    # name, since there is no old constraint to rename in that case.
    if 'regulations' not in inspector.get_table_names():
        op.create_table(
            'regulations',
            sa.Column('code', sa.String(length=32), nullable=False),
            sa.Column('name', sa.String(length=255), nullable=False),
            sa.Column('applicable_batch', sa.String(length=128), nullable=False),
            sa.Column('program_level', sa.String(length=32), nullable=False),
            sa.Column('duration_years', sa.Integer(), nullable=False),
            sa.Column('credit_pattern', sa.String(length=255), nullable=True),
            sa.Column('exam_formula', sa.String(length=255), nullable=True),
            sa.Column('passing_marks', sa.Integer(), nullable=False),
            sa.Column('grade_boundaries', sa.String(length=2000), nullable=True),
            sa.Column('id', sa.String(length=36), nullable=False),
            sa.Column('created_at', sa.DateTime(timezone=True), nullable=False),
            sa.Column('updated_at', sa.DateTime(timezone=True), nullable=False),
            sa.Column('is_deleted', sa.Boolean(), nullable=False),
            sa.Column('deleted_at', sa.DateTime(timezone=True), nullable=True),
            sa.PrimaryKeyConstraint('id', name=op.f('pk_regulations')),
            sa.UniqueConstraint('code', 'program_level', 'applicable_batch', name='uq_regulation_code_program_batch'),
        )
    else:
        existing_uqs = {uq['name'] for uq in inspector.get_unique_constraints('regulations')}
        if 'uq_regulation_code_program_batch' not in existing_uqs:
            with op.batch_alter_table('regulations', schema=None) as batch_op:
                # Drop the old unique constraint (name may vary — try both possible names)
                if 'uq_regulation_code_batch' in existing_uqs:
                    batch_op.drop_constraint('uq_regulation_code_batch', type_='unique')
                batch_op.create_unique_constraint(
                    'uq_regulation_code_program_batch',
                    ['code', 'program_level', 'applicable_batch']
                )


def downgrade() -> None:
    # ── 4. Revert Regulation constraint ────────────────────────────────────────
    with op.batch_alter_table('regulations', schema=None) as batch_op:
        batch_op.drop_constraint('uq_regulation_code_program_batch', type_='unique')
        batch_op.create_unique_constraint(
            'uq_regulation_code_batch',
            ['code', 'applicable_batch']
        )

    # ── 3. Drop class_advisors ──────────────────────────────────────────────────
    op.drop_table('class_advisors')

    # ── 2. Drop notice_acknowledgements ────────────────────────────────────────
    op.drop_table('notice_acknowledgements')

    # ── 1. Remove new notice columns ───────────────────────────────────────────
    with op.batch_alter_table('notices', schema=None) as batch_op:
        batch_op.drop_column('target_section')
        batch_op.drop_column('target_semester')
        batch_op.drop_column('target_department')
        batch_op.drop_column('publisher_role')
        batch_op.drop_column('status')
        batch_op.drop_column('priority')
        batch_op.drop_column('expiry_date')
        batch_op.drop_column('category')
