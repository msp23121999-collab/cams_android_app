"""add missing faculty profile and research columns

Revision ID: dbfacc59505d
Revises: 226a1129c308
Create Date: 2026-07-19 13:33:44.689583

Backfills columns that the FacultyProfile / FacultyResearch SQLAlchemy
models have declared for a while, but which were never actually added to
the live table via a migration (found while auditing Faculty Screen 2 —
GET /faculty/profile was crashing with `no such column:
faculty_profiles.employee_code`). Scoped to only these two tables; the
same class of drift also exists on publication_plans/salary/students/
study_materials but those are out of scope for this screen and will be
picked up when their respective screens are audited.
"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa

# revision identifiers, used by Alembic.
revision: str = 'dbfacc59505d'
down_revision: Union[str, None] = '226a1129c308'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.add_column('faculty_profiles', sa.Column('employee_code', sa.String(length=64), nullable=True))
    op.add_column('faculty_profiles', sa.Column('gender', sa.String(length=16), nullable=True))
    op.add_column('faculty_profiles', sa.Column('date_of_birth', sa.Date(), nullable=True))
    op.add_column('faculty_profiles', sa.Column('blood_group', sa.String(length=16), nullable=True))
    op.add_column('faculty_profiles', sa.Column('marital_status', sa.String(length=32), nullable=True))
    op.add_column('faculty_profiles', sa.Column('nationality', sa.String(length=64), nullable=True))
    op.add_column('faculty_profiles', sa.Column('alternate_phone', sa.String(length=32), nullable=True))
    op.add_column('faculty_profiles', sa.Column('personal_email', sa.String(length=255), nullable=True))
    op.add_column('faculty_profiles', sa.Column('current_address', sa.String(length=512), nullable=True))
    op.add_column('faculty_profiles', sa.Column('permanent_address', sa.String(length=512), nullable=True))
    op.add_column('faculty_profiles', sa.Column('city', sa.String(length=128), nullable=True))
    op.add_column('faculty_profiles', sa.Column('state', sa.String(length=128), nullable=True))
    op.add_column('faculty_profiles', sa.Column('pincode', sa.String(length=16), nullable=True))
    op.add_column('faculty_profiles', sa.Column('profile_photo_url', sa.String(length=512), nullable=True))
    op.add_column('faculty_profiles', sa.Column('faculty_type', sa.String(length=64), nullable=True))
    op.add_column('faculty_profiles', sa.Column('employment_category', sa.String(length=64), nullable=True))
    op.add_column('faculty_profiles', sa.Column('date_of_joining', sa.Date(), nullable=True))
    op.add_column('faculty_profiles', sa.Column('employment_status', sa.String(length=64), nullable=True))
    op.add_column('faculty_profiles', sa.Column('reporting_hod_id', sa.String(length=36), nullable=True))
    op.add_column('faculty_profiles', sa.Column('reporting_principal_id', sa.String(length=36), nullable=True))
    op.add_column('faculty_profiles', sa.Column('confirmation_date', sa.Date(), nullable=True))
    op.add_column('faculty_profiles', sa.Column('educational_qualifications', sa.JSON(), nullable=True))
    op.add_column('faculty_profiles', sa.Column('experience_details', sa.JSON(), nullable=True))
    op.add_column('faculty_profiles', sa.Column('academic_responsibilities', sa.JSON(), nullable=True))
    op.add_column('faculty_profiles', sa.Column('certifications_achievements', sa.JSON(), nullable=True))
    op.add_column('faculty_profiles', sa.Column('promotion_history', sa.JSON(), nullable=True))
    op.add_column('faculty_profiles', sa.Column('increment_history', sa.JSON(), nullable=True))
    op.add_column('faculty_profiles', sa.Column('documents_repository', sa.JSON(), nullable=True))
    op.add_column('faculty_profiles', sa.Column('notification_preferences', sa.JSON(), nullable=True))
    op.add_column('faculty_research', sa.Column('publisher', sa.String(length=255), nullable=True))
    op.add_column('faculty_research', sa.Column('publication_date', sa.Date(), nullable=True))
    op.add_column('faculty_research', sa.Column('isbn_issn', sa.String(length=64), nullable=True))
    op.add_column('faculty_research', sa.Column('research_type', sa.String(length=64), nullable=True))


def downgrade() -> None:
    op.drop_column('faculty_research', 'research_type')
    op.drop_column('faculty_research', 'isbn_issn')
    op.drop_column('faculty_research', 'publication_date')
    op.drop_column('faculty_research', 'publisher')
    op.drop_column('faculty_profiles', 'notification_preferences')
    op.drop_column('faculty_profiles', 'documents_repository')
    op.drop_column('faculty_profiles', 'increment_history')
    op.drop_column('faculty_profiles', 'promotion_history')
    op.drop_column('faculty_profiles', 'certifications_achievements')
    op.drop_column('faculty_profiles', 'academic_responsibilities')
    op.drop_column('faculty_profiles', 'experience_details')
    op.drop_column('faculty_profiles', 'educational_qualifications')
    op.drop_column('faculty_profiles', 'confirmation_date')
    op.drop_column('faculty_profiles', 'reporting_principal_id')
    op.drop_column('faculty_profiles', 'reporting_hod_id')
    op.drop_column('faculty_profiles', 'employment_status')
    op.drop_column('faculty_profiles', 'date_of_joining')
    op.drop_column('faculty_profiles', 'employment_category')
    op.drop_column('faculty_profiles', 'faculty_type')
    op.drop_column('faculty_profiles', 'profile_photo_url')
    op.drop_column('faculty_profiles', 'pincode')
    op.drop_column('faculty_profiles', 'state')
    op.drop_column('faculty_profiles', 'city')
    op.drop_column('faculty_profiles', 'permanent_address')
    op.drop_column('faculty_profiles', 'current_address')
    op.drop_column('faculty_profiles', 'personal_email')
    op.drop_column('faculty_profiles', 'alternate_phone')
    op.drop_column('faculty_profiles', 'nationality')
    op.drop_column('faculty_profiles', 'marital_status')
    op.drop_column('faculty_profiles', 'blood_group')
    op.drop_column('faculty_profiles', 'date_of_birth')
    op.drop_column('faculty_profiles', 'gender')
    op.drop_column('faculty_profiles', 'employee_code')
