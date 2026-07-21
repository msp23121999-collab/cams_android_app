"""add hostel, inventory, library and transport ERP modules

Revision ID: 20b1bbbfe133
Revises: 53b91e08f384
Create Date: 2026-07-20

NOTE: hand-written on purpose. `alembic revision --autogenerate` also picked up
pre-existing drift between the models and the live database — it proposed
dropping `clubs`, `hall_tickets`, `session_summaries`, `student_loans` and
`club_announcements`, among other unrelated changes. Those drops would destroy
live data, so this migration creates only the new ERP tables.
"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa

# revision identifiers, used by Alembic.
revision: str = '20b1bbbfe133'
down_revision: Union[str, None] = '53b91e08f384'
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
    # ---------------- Hostel ----------------
    op.create_table(
        'hostel_blocks',
        sa.Column('name', sa.String(length=128), nullable=False),
        sa.Column('code', sa.String(length=32), nullable=False),
        sa.Column('hostel_type', sa.String(length=16), nullable=False),
        sa.Column('warden_name', sa.String(length=128), nullable=True),
        sa.Column('warden_phone', sa.String(length=20), nullable=True),
        sa.Column('address', sa.String(length=512), nullable=True),
        *_base_cols(),
        sa.PrimaryKeyConstraint('id'),
        sa.UniqueConstraint('code', name='uq_hostel_block_code'),
    )
    op.create_index('ix_hostel_blocks_code', 'hostel_blocks', ['code'])

    op.create_table(
        'hostel_rooms',
        sa.Column('block_id', sa.String(length=36), nullable=False),
        sa.Column('room_number', sa.String(length=32), nullable=False),
        sa.Column('floor', sa.Integer(), nullable=False),
        sa.Column('capacity', sa.Integer(), nullable=False),
        sa.Column('room_type', sa.String(length=64), nullable=True),
        sa.Column('monthly_rent', sa.Numeric(precision=12, scale=2), nullable=True),
        *_base_cols(),
        sa.ForeignKeyConstraint(['block_id'], ['hostel_blocks.id']),
        sa.PrimaryKeyConstraint('id'),
        sa.UniqueConstraint('block_id', 'room_number', name='uq_hostel_room_block_number'),
    )
    op.create_index('ix_hostel_rooms_block_id', 'hostel_rooms', ['block_id'])

    op.create_table(
        'hostel_allocations',
        sa.Column('room_id', sa.String(length=36), nullable=False),
        sa.Column('student_id', sa.String(length=36), nullable=False),
        sa.Column('allocated_on', sa.Date(), nullable=False),
        sa.Column('vacated_on', sa.Date(), nullable=True),
        sa.Column('status', sa.String(length=16), nullable=False),
        sa.Column('remarks', sa.String(length=512), nullable=True),
        *_base_cols(),
        sa.ForeignKeyConstraint(['room_id'], ['hostel_rooms.id']),
        sa.ForeignKeyConstraint(['student_id'], ['students.id']),
        sa.PrimaryKeyConstraint('id'),
    )
    op.create_index('ix_hostel_allocations_room_id', 'hostel_allocations', ['room_id'])
    op.create_index('ix_hostel_allocations_student_id', 'hostel_allocations', ['student_id'])
    op.create_index('ix_hostel_allocations_status', 'hostel_allocations', ['status'])

    # ---------------- Inventory ----------------
    op.create_table(
        'inventory_items',
        sa.Column('name', sa.String(length=255), nullable=False),
        sa.Column('code', sa.String(length=64), nullable=False),
        sa.Column('category', sa.String(length=128), nullable=True),
        sa.Column('unit', sa.String(length=32), nullable=False),
        sa.Column('quantity', sa.Integer(), nullable=False),
        sa.Column('min_quantity', sa.Integer(), nullable=False),
        sa.Column('unit_price', sa.Numeric(precision=12, scale=2), nullable=True),
        sa.Column('location', sa.String(length=255), nullable=True),
        sa.Column('supplier', sa.String(length=255), nullable=True),
        *_base_cols(),
        sa.PrimaryKeyConstraint('id'),
        sa.UniqueConstraint('code', name='uq_inventory_item_code'),
    )
    op.create_index('ix_inventory_items_code', 'inventory_items', ['code'])
    op.create_index('ix_inventory_items_category', 'inventory_items', ['category'])

    op.create_table(
        'inventory_transactions',
        sa.Column('item_id', sa.String(length=36), nullable=False),
        sa.Column('movement', sa.String(length=16), nullable=False),
        sa.Column('quantity', sa.Integer(), nullable=False),
        sa.Column('resulting_quantity', sa.Integer(), nullable=False),
        sa.Column('reason', sa.String(length=512), nullable=True),
        sa.Column('performed_by', sa.String(length=36), nullable=True),
        *_base_cols(),
        sa.ForeignKeyConstraint(['item_id'], ['inventory_items.id']),
        sa.ForeignKeyConstraint(['performed_by'], ['users.id']),
        sa.PrimaryKeyConstraint('id'),
    )
    op.create_index('ix_inventory_transactions_item_id', 'inventory_transactions', ['item_id'])

    # ---------------- Library ----------------
    op.create_table(
        'library_books',
        sa.Column('title', sa.String(length=512), nullable=False),
        sa.Column('author', sa.String(length=255), nullable=True),
        sa.Column('accession_no', sa.String(length=64), nullable=False),
        sa.Column('isbn', sa.String(length=32), nullable=True),
        sa.Column('category', sa.String(length=128), nullable=True),
        sa.Column('publisher', sa.String(length=255), nullable=True),
        sa.Column('published_year', sa.Integer(), nullable=True),
        sa.Column('shelf_location', sa.String(length=128), nullable=True),
        sa.Column('total_copies', sa.Integer(), nullable=False),
        sa.Column('available_copies', sa.Integer(), nullable=False),
        *_base_cols(),
        sa.PrimaryKeyConstraint('id'),
        sa.UniqueConstraint('accession_no', name='uq_library_book_accession'),
    )
    op.create_index('ix_library_books_title', 'library_books', ['title'])
    op.create_index('ix_library_books_accession_no', 'library_books', ['accession_no'])
    op.create_index('ix_library_books_category', 'library_books', ['category'])

    op.create_table(
        'library_issues',
        sa.Column('book_id', sa.String(length=36), nullable=False),
        sa.Column('member_id', sa.String(length=36), nullable=False),
        sa.Column('issued_on', sa.Date(), nullable=False),
        sa.Column('due_on', sa.Date(), nullable=False),
        sa.Column('returned_on', sa.Date(), nullable=True),
        sa.Column('fine_amount', sa.Numeric(precision=12, scale=2), nullable=False),
        sa.Column('status', sa.String(length=16), nullable=False),
        sa.Column('remarks', sa.String(length=512), nullable=True),
        *_base_cols(),
        sa.ForeignKeyConstraint(['book_id'], ['library_books.id']),
        sa.ForeignKeyConstraint(['member_id'], ['users.id']),
        sa.PrimaryKeyConstraint('id'),
    )
    op.create_index('ix_library_issues_book_id', 'library_issues', ['book_id'])
    op.create_index('ix_library_issues_member_id', 'library_issues', ['member_id'])
    op.create_index('ix_library_issues_status', 'library_issues', ['status'])

    # ---------------- Transport ----------------
    op.create_table(
        'transport_routes',
        sa.Column('name', sa.String(length=255), nullable=False),
        sa.Column('code', sa.String(length=32), nullable=False),
        sa.Column('start_point', sa.String(length=255), nullable=False),
        sa.Column('end_point', sa.String(length=255), nullable=False),
        sa.Column('distance_km', sa.Numeric(precision=8, scale=2), nullable=True),
        sa.Column('fare', sa.Numeric(precision=12, scale=2), nullable=True),
        sa.Column('stops', sa.String(length=2000), nullable=True),
        *_base_cols(),
        sa.PrimaryKeyConstraint('id'),
        sa.UniqueConstraint('code', name='uq_transport_route_code'),
    )
    op.create_index('ix_transport_routes_code', 'transport_routes', ['code'])

    op.create_table(
        'transport_vehicles',
        sa.Column('registration_no', sa.String(length=32), nullable=False),
        sa.Column('vehicle_type', sa.String(length=64), nullable=True),
        sa.Column('capacity', sa.Integer(), nullable=False),
        sa.Column('driver_name', sa.String(length=128), nullable=True),
        sa.Column('driver_phone', sa.String(length=20), nullable=True),
        sa.Column('route_id', sa.String(length=36), nullable=True),
        sa.Column('status', sa.String(length=16), nullable=False),
        *_base_cols(),
        sa.ForeignKeyConstraint(['route_id'], ['transport_routes.id']),
        sa.PrimaryKeyConstraint('id'),
        sa.UniqueConstraint('registration_no', name='uq_transport_vehicle_reg'),
    )
    op.create_index('ix_transport_vehicles_registration_no', 'transport_vehicles', ['registration_no'])
    op.create_index('ix_transport_vehicles_route_id', 'transport_vehicles', ['route_id'])
    op.create_index('ix_transport_vehicles_status', 'transport_vehicles', ['status'])

    op.create_table(
        'transport_passes',
        sa.Column('route_id', sa.String(length=36), nullable=False),
        sa.Column('student_id', sa.String(length=36), nullable=False),
        sa.Column('pickup_point', sa.String(length=255), nullable=True),
        sa.Column('valid_from', sa.Date(), nullable=False),
        sa.Column('valid_to', sa.Date(), nullable=False),
        sa.Column('fare_paid', sa.Numeric(precision=12, scale=2), nullable=True),
        sa.Column('status', sa.String(length=16), nullable=False),
        *_base_cols(),
        sa.ForeignKeyConstraint(['route_id'], ['transport_routes.id']),
        sa.ForeignKeyConstraint(['student_id'], ['students.id']),
        sa.PrimaryKeyConstraint('id'),
    )
    op.create_index('ix_transport_passes_route_id', 'transport_passes', ['route_id'])
    op.create_index('ix_transport_passes_student_id', 'transport_passes', ['student_id'])
    op.create_index('ix_transport_passes_status', 'transport_passes', ['status'])


def downgrade() -> None:
    op.drop_table('transport_passes')
    op.drop_table('transport_vehicles')
    op.drop_table('transport_routes')
    op.drop_table('library_issues')
    op.drop_table('library_books')
    op.drop_table('inventory_transactions')
    op.drop_table('inventory_items')
    op.drop_table('hostel_allocations')
    op.drop_table('hostel_rooms')
    op.drop_table('hostel_blocks')
