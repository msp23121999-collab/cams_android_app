"""Schema-integrity tests: the database Alembic builds must match the models.

Every check here corresponds to a bug that was live in this repo and invisible
in development, because app startup used to run
``Base.metadata.create_all()`` plus a block of hand-written ``ALTER TABLE``
statements.  That rebuilt whatever the migrations had failed to produce, so the
migration chain was never actually exercised.  What it was hiding:

  * seven PF/payroll tables created on one branch and dropped on a sibling
    branch, so which ones survived depended on the order Alembic walked the graph
  * six tables no migration created at all (``internal_marks``,
    ``subject_allocations``, ``mentorship_records``, ...)
  * fifty columns no migration added, including ``students.section_id`` (faculty
    roster scoping) and ``leaves.hod_status`` (HOD -> Principal escalation)
  * three legacy NOT NULL columns that blocked every insert in the current format

These tests build a database from migrations ONLY and compare it against the
model metadata, so the next such gap fails here instead of in production.
"""
from __future__ import annotations

import os
import subprocess
import sys
import tempfile
from pathlib import Path

import pytest
import sqlalchemy as sa

BACKEND_DIR = Path(__file__).resolve().parent.parent


@pytest.fixture(scope="module")
def migrated_db_url() -> str:
    """A fresh database built by `alembic upgrade head` and nothing else.

    Deliberately does NOT use the shared conftest schema fixture, which creates
    tables from model metadata — that would defeat the entire point.
    """
    tmp_dir = Path(tempfile.mkdtemp(prefix="cams-migrated-"))
    db_path = (tmp_dir / "migrated.db").as_posix()
    url = f"sqlite+aiosqlite:///{db_path}"

    env = dict(os.environ, DATABASE_URL=url)
    result = subprocess.run(
        [sys.executable, "-m", "alembic", "upgrade", "head"],
        cwd=str(BACKEND_DIR), env=env, capture_output=True, text=True, timeout=900,
    )
    assert result.returncode == 0, (
        "`alembic upgrade head` failed on an EMPTY database.\n"
        "A fresh production deploy would not come up.\n\n"
        + result.stdout[-3000:] + "\n" + result.stderr[-3000:]
    )
    return f"sqlite:///{db_path}"


def _model_metadata():
    from app.db.base import Base
    import app.db.models  # noqa: F401  -- registers every model on the metadata
    return Base.metadata


def test_migrations_create_every_model_table(migrated_db_url):
    """No table may exist only because create_all used to build it."""
    metadata = _model_metadata()
    engine = sa.create_engine(migrated_db_url)
    try:
        present = set(sa.inspect(engine).get_table_names())
    finally:
        engine.dispose()

    missing = sorted(set(metadata.tables) - present)
    assert not missing, (
        "These tables are defined on the models but no migration creates them, so a\n"
        "database built purely from migrations is missing them entirely:\n  "
        + "\n  ".join(missing)
        + "\n\nAdd a migration that creates them."
    )


def test_migrations_create_every_model_column(migrated_db_url):
    """Table-level parity is not enough — columns drifted too (50 of them)."""
    metadata = _model_metadata()
    engine = sa.create_engine(migrated_db_url)
    try:
        inspector = sa.inspect(engine)
        present_tables = set(inspector.get_table_names())
        drift: dict[str, list[str]] = {}
        for name, table in metadata.tables.items():
            if name not in present_tables:
                continue  # already reported by the table test
            cols = {c["name"] for c in inspector.get_columns(name)}
            missing = [c.name for c in table.columns if c.name not in cols]
            if missing:
                drift[name] = sorted(missing)
    finally:
        engine.dispose()

    assert not drift, (
        "These model columns are missing from a migration-built database:\n  "
        + "\n  ".join(f"{t}: {c}" for t, c in sorted(drift.items()))
    )


def test_no_legacy_not_null_columns_block_inserts(migrated_db_url):
    """Columns the models dropped must not remain NOT NULL without a default.

    Regression: ``attendance`` moved to a bulk representation, but
    ``attendance.student_id`` and ``attendance.status`` stayed NOT NULL, so every
    insert in the current format failed with an IntegrityError.
    """
    metadata = _model_metadata()
    engine = sa.create_engine(migrated_db_url)
    try:
        inspector = sa.inspect(engine)
        present_tables = set(inspector.get_table_names())
        blockers = []
        for name, table in metadata.tables.items():
            if name not in present_tables:
                continue
            model_cols = {c.name for c in table.columns}
            for col in inspector.get_columns(name):
                if (
                    col["name"] not in model_cols
                    and not col.get("nullable", True)
                    and col.get("default") is None
                ):
                    blockers.append(f"{name}.{col['name']}")
    finally:
        engine.dispose()

    assert not blockers, (
        "These columns are NOT NULL with no default but are no longer on the models,\n"
        "so nothing populates them and every INSERT fails:\n  "
        + "\n  ".join(sorted(blockers))
    )


@pytest.mark.skipif(
    not os.environ.get("TEST_POSTGRES_URL"),
    reason="set TEST_POSTGRES_URL to run the PostgreSQL-only enum drift check",
)
def test_postgres_enum_values_match_models():
    """PostgreSQL enforces enum membership; SQLite treats enums as free text.

    That difference hid a serious bug: ``leave_status`` and
    ``leave_approval_status`` were missing every workflow value the code writes
    (``APPROVED_BY_HOD``, ``PENDING_PRINCIPAL``, ``FINAL_APPROVED``, ...), so the
    HOD -> Principal escalation could not work at all on PostgreSQL while passing
    happily on the SQLite dev database.

    Run with:  TEST_POSTGRES_URL=postgresql://user:pw@host:port/db pytest
    """
    metadata = _model_metadata()
    model_enums: dict[str, set[str]] = {}
    for table in metadata.tables.values():
        for col in table.columns:
            if isinstance(col.type, sa.Enum) and col.type.name:
                model_enums.setdefault(col.type.name, set()).update(col.type.enums)

    engine = sa.create_engine(os.environ["TEST_POSTGRES_URL"])
    try:
        with engine.connect() as conn:
            rows = conn.execute(sa.text(
                "SELECT t.typname, e.enumlabel FROM pg_enum e "
                "JOIN pg_type t ON t.oid = e.enumtypid"
            )).fetchall()
    finally:
        engine.dispose()

    db_enums: dict[str, set[str]] = {}
    for type_name, label in rows:
        db_enums.setdefault(type_name, set()).add(label)

    drift = {}
    for name, values in model_enums.items():
        if name not in db_enums:
            continue  # type not created on this database; table tests cover that
        missing = sorted(values - db_enums[name])
        if missing:
            drift[name] = missing

    assert not drift, (
        "The models write enum values the database will reject:\n  "
        + "\n  ".join(f"{t}: {v}" for t, v in sorted(drift.items()))
        + "\n\nAdd an `ALTER TYPE ... ADD VALUE` migration."
    )
