"""add institutional budget line items, expenses and grants tables

Revision ID: 7a1c2f9e4b6d
Revises: 20b1bbbfe133
Create Date: 2026-07-20

NOTE: hand-written on purpose, following the same practice as the ERP
migration this is chained after — `alembic revision --autogenerate` surfaces
unrelated pre-existing model/DB drift on this codebase, so migrations here
are written by hand to touch only the new tables being added.
"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa

# revision identifiers, used by Alembic.
revision: str = '7a1c2f9e4b6d'
down_revision: Union[str, None] = '20b1bbbfe133'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def _base_cols() -> list:
    """Columns contributed by TimestampSoftDeleteMixin."""
    return [
        sa.Column('id', sa.String(length=36), nullable=False),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.text('CURRENT_TIMESTAMP'), nullable=False),
        sa.Column('updated_at', sa.DateTime(timezone=True), server_default=sa.text('CURRENT_TIMESTAMP'), nullable=False),
        sa.Column('is_deleted', sa.Boolean(), server_default='false', nullable=False),
        sa.Column('deleted_at', sa.DateTime(timezone=True), nullable=True),
    ]


def upgrade() -> None:
    op.create_table(
        'budget_line_items',
        sa.Column('fiscal_year', sa.String(length=16), nullable=False),
        sa.Column('title', sa.String(length=255), nullable=False),
        sa.Column('category', sa.String(length=64), nullable=False, server_default='General'),
        sa.Column('department_id', sa.String(length=36), nullable=True),
        sa.Column('allocated_amount', sa.Numeric(14, 2), nullable=False, server_default='0'),
        sa.Column('spent_amount', sa.Numeric(14, 2), nullable=False, server_default='0'),
        sa.Column('status', sa.String(length=16), nullable=False, server_default='ACTIVE'),
        sa.Column('notes', sa.String(length=1024), nullable=True),
        sa.Column('created_by', sa.String(length=36), nullable=False),
        *_base_cols(),
        sa.ForeignKeyConstraint(['department_id'], ['departments.id']),
        sa.ForeignKeyConstraint(['created_by'], ['users.id']),
        sa.PrimaryKeyConstraint('id'),
    )
    op.create_index('ix_budget_line_items_fiscal_year', 'budget_line_items', ['fiscal_year'])

    op.create_table(
        'budget_expenses',
        sa.Column('line_item_id', sa.String(length=36), nullable=False),
        sa.Column('description', sa.String(length=512), nullable=False),
        sa.Column('amount', sa.Numeric(14, 2), nullable=False),
        sa.Column('expense_date', sa.String(length=32), nullable=False),
        sa.Column('recorded_by', sa.String(length=36), nullable=False),
        *_base_cols(),
        sa.ForeignKeyConstraint(['line_item_id'], ['budget_line_items.id']),
        sa.ForeignKeyConstraint(['recorded_by'], ['users.id']),
        sa.PrimaryKeyConstraint('id'),
    )
    op.create_index('ix_budget_expenses_line_item_id', 'budget_expenses', ['line_item_id'])

    op.create_table(
        'grants',
        sa.Column('title', sa.String(length=255), nullable=False),
        sa.Column('funding_agency', sa.String(length=255), nullable=False),
        sa.Column('department_id', sa.String(length=36), nullable=True),
        sa.Column('principal_investigator', sa.String(length=255), nullable=True),
        sa.Column('sanctioned_amount', sa.Numeric(14, 2), nullable=False, server_default='0'),
        sa.Column('disbursed_amount', sa.Numeric(14, 2), nullable=False, server_default='0'),
        sa.Column('status', sa.String(length=16), nullable=False, server_default='PROPOSED'),
        sa.Column('start_date', sa.String(length=32), nullable=True),
        sa.Column('end_date', sa.String(length=32), nullable=True),
        sa.Column('notes', sa.String(length=1024), nullable=True),
        sa.Column('created_by', sa.String(length=36), nullable=False),
        *_base_cols(),
        sa.ForeignKeyConstraint(['department_id'], ['departments.id']),
        sa.ForeignKeyConstraint(['created_by'], ['users.id']),
        sa.PrimaryKeyConstraint('id'),
    )


def downgrade() -> None:
    op.drop_table('grants')
    op.drop_index('ix_budget_expenses_line_item_id', table_name='budget_expenses')
    op.drop_table('budget_expenses')
    op.drop_index('ix_budget_line_items_fiscal_year', table_name='budget_line_items')
    op.drop_table('budget_line_items')
