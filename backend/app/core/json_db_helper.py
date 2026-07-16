import os
import json
import logging
from typing import Any, Callable
import psycopg2
from psycopg2 import pool
from app.core.config import settings
import sqlite3
import threading

logger = logging.getLogger("app.json_db_helper")

_pool = None
_sqlite_conn = None
_sqlite_lock = threading.Lock()

def get_db_pool():
    global _pool, _sqlite_conn
    db_url = settings.DATABASE_URL
    if db_url.startswith("sqlite"):
        if _sqlite_conn is None:
            # extract path
            path = db_url.split("sqlite+aiosqlite:///")[-1]
            if path == db_url: # fallback
                path = "cams.db"
            _sqlite_conn = sqlite3.connect(path, check_same_thread=False)
            cur = _sqlite_conn.cursor()
            cur.execute("""
                CREATE TABLE IF NOT EXISTS local_json_db (
                    key VARCHAR(255) PRIMARY KEY,
                    data TEXT,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """)
            _sqlite_conn.commit()
        return _sqlite_conn

    if _pool is None:
        sync_url = db_url.replace("postgresql+asyncpg://", "postgresql://")
        try:
            _pool = pool.ThreadedConnectionPool(1, 20, sync_url)
            conn = _pool.getconn()
            try:
                with conn.cursor() as cur:
                    cur.execute("""
                        CREATE TABLE IF NOT EXISTS local_json_db (
                            key VARCHAR(255) PRIMARY KEY,
                            data JSONB,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                        );
                    """)
                    conn.commit()
            finally:
                _pool.putconn(conn)
        except Exception as e:
            logger.error(f"Failed to initialize PostgreSQL connection pool: {e}")
            raise e
    return _pool

def load_db_from_postgres(filename: str, default_factory: Callable[[], Any]) -> Any:
    """
    Loads JSON data for the given filename from the database.
    If the key doesn't exist, inserts the default value produced by default_factory
    and returns it.
    """
    # Use only the basename of the file to be independent of path differences
    key = os.path.basename(filename)
    try:
        if settings.DATABASE_URL.startswith("sqlite"):
            with _sqlite_lock:
                conn = get_db_pool()
                cur = conn.cursor()
                cur.execute("SELECT data FROM local_json_db WHERE key = ?;", (key,))
                row = cur.fetchone()
                if row:
                    return json.loads(row[0])
                else:
                    default_data = default_factory()
                    cur.execute("""
                        INSERT INTO local_json_db (key, data, updated_at)
                        VALUES (?, ?, CURRENT_TIMESTAMP)
                        ON CONFLICT (key) DO NOTHING;
                    """, (key, json.dumps(default_data)))
                    conn.commit()
                    return default_data

        connection_pool = get_db_pool()
        conn = connection_pool.getconn()
        try:
            with conn.cursor() as cur:
                cur.execute("SELECT data FROM local_json_db WHERE key = %s;", (key,))
                row = cur.fetchone()
                if row:
                    return row[0] if isinstance(row[0], (dict, list)) else json.loads(row[0])
                else:
                    default_data = default_factory()
                    cur.execute("""
                        INSERT INTO local_json_db (key, data, updated_at)
                        VALUES (%s, %s, CURRENT_TIMESTAMP)
                        ON CONFLICT (key) DO NOTHING;
                    """, (key, json.dumps(default_data)))
                    conn.commit()
                    return default_data
        finally:
            connection_pool.putconn(conn)
    except Exception as e:
        logger.error(f"Error loading {key} from database: {e}")
        # Fallback to returning default_factory() to ensure application robustness
        return default_factory()

def save_db_to_postgres(filename: str, data: Any) -> None:
    """
    Saves JSON data for the given filename to the database (upsert).
    """
    key = os.path.basename(filename)
    try:
        if settings.DATABASE_URL.startswith("sqlite"):
            with _sqlite_lock:
                conn = get_db_pool()
                cur = conn.cursor()
                cur.execute("""
                    INSERT INTO local_json_db (key, data, updated_at)
                    VALUES (?, ?, CURRENT_TIMESTAMP)
                    ON CONFLICT (key) DO UPDATE
                    SET data = excluded.data, updated_at = CURRENT_TIMESTAMP;
                """, (key, json.dumps(data)))
                conn.commit()
                return

        connection_pool = get_db_pool()
        conn = connection_pool.getconn()
        try:
            with conn.cursor() as cur:
                cur.execute("""
                    INSERT INTO local_json_db (key, data, updated_at)
                    VALUES (%s, %s, CURRENT_TIMESTAMP)
                    ON CONFLICT (key) DO UPDATE
                    SET data = EXCLUDED.data, updated_at = CURRENT_TIMESTAMP;
                """, (key, json.dumps(data)))
                conn.commit()
        finally:
            connection_pool.putconn(conn)
    except Exception as e:
        logger.error(f"Error saving {key} to database: {e}")
