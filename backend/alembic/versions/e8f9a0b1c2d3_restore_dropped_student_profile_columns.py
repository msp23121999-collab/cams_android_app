"""restore_dropped_student_profile_columns

Revision 5d1bb6155b93 auto-generated a diff that dropped ~31 student
personal-info columns (full_name, cgpa, date_of_birth, mentor_id, parent /
emergency-contact fields, etc.) that app/db/models/student.py still declares
and every student-facing endpoint relies on. That drop already ran against
the real dev database, so every query touching the Student model was
failing with "no such column" errors. This migration adds them back,
matching the model's current column definitions exactly.

Revision ID: e8f9a0b1c2d3
Revises: d2e3f4a5b6c7
Create Date: 2026-07-18 00:00:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa

# revision identifiers, used by Alembic.
revision: str = 'e8f9a0b1c2d3'
down_revision: Union[str, None] = 'd2e3f4a5b6c7'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


_COLUMNS = [
    ("mentor_id", sa.String(length=36), True, None),
    ("full_name", sa.String(length=128), True, None),
    ("date_of_birth", sa.Date(), True, None),
    ("gender", sa.String(length=16), True, None),
    ("blood_group", sa.String(length=8), True, None),
    ("nationality", sa.String(length=64), True, None),
    ("mobile_number", sa.String(length=20), True, None),
    ("current_address", sa.String(length=256), True, None),
    ("permanent_address", sa.String(length=256), True, None),
    ("aadhaar_number", sa.String(length=20), True, None),
    ("passport_number", sa.String(length=20), True, None),
    ("community_category", sa.String(length=64), True, None),
    ("religion", sa.String(length=64), True, None),
    ("emergency_contact_name", sa.String(length=128), True, None),
    ("emergency_contact_relationship", sa.String(length=64), True, None),
    ("emergency_contact_number", sa.String(length=20), True, None),
    ("father_name", sa.String(length=128), True, None),
    ("father_occupation", sa.String(length=128), True, None),
    ("father_mobile", sa.String(length=20), True, None),
    ("father_email", sa.String(length=128), True, None),
    ("father_office_address", sa.String(length=256), True, None),
    ("mother_name", sa.String(length=128), True, None),
    ("mother_occupation", sa.String(length=128), True, None),
    ("mother_mobile", sa.String(length=20), True, None),
    ("mother_email", sa.String(length=128), True, None),
    ("mother_office_address", sa.String(length=256), True, None),
    ("parent_annual_income", sa.String(length=64), True, None),
    ("languages_known", sa.JSON(), True, None),
    ("hobbies_interests", sa.JSON(), True, None),
    ("special_skills", sa.JSON(), True, None),
    ("medical_info", sa.String(length=256), True, None),
    ("cgpa", sa.Numeric(3, 2), True, None),
    ("skills", sa.JSON(), True, None),
]


def upgrade() -> None:
    bind = op.get_bind()
    inspector = sa.inspect(bind)
    existing_columns = {col["name"] for col in inspector.get_columns("students")}

    for name, col_type, nullable, default in _COLUMNS:
        if name not in existing_columns:
            op.add_column("students", sa.Column(name, col_type, nullable=nullable, server_default=default))

    if bind.dialect.name != "sqlite":
        existing_fks = {fk["name"] for fk in inspector.get_foreign_keys("students")}
        if "fk_students_mentor_id_users" not in existing_fks:
            op.create_foreign_key(
                "fk_students_mentor_id_users", "students", "users", ["mentor_id"], ["id"]
            )


def downgrade() -> None:
    bind = op.get_bind()
    inspector = sa.inspect(bind)
    existing_columns = {col["name"] for col in inspector.get_columns("students")}

    if bind.dialect.name != "sqlite":
        existing_fks = {fk["name"] for fk in inspector.get_foreign_keys("students")}
        if "fk_students_mentor_id_users" in existing_fks:
            op.drop_constraint("fk_students_mentor_id_users", "students", type_="foreignkey")

    for name, _, _, _ in _COLUMNS:
        if name in existing_columns:
            op.drop_column("students", name)
