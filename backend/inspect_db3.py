import sys
import psycopg2

backend_dir = r"c:\Users\ADMIN\.gemini\antigravity-ide\scratch\cams-law\backend"
sys.path.append(backend_dir)

from app.core.config import settings

def main():
    db_url = settings.DATABASE_URL
    sync_url = db_url.replace("postgresql+asyncpg://", "postgresql://")
    conn = psycopg2.connect(sync_url)
    try:
        with conn.cursor() as cur:
            cur.execute("""
                SELECT id, name, start_date, end_date, is_active, is_deleted, current_semester, is_semester_open, degree_id, batch
                FROM academic_years
                WHERE is_deleted = False;
            """)
            print("=== Academic Years ===")
            cols = ["id", "name", "start_date", "end_date", "is_active", "is_deleted", "current_semester", "is_semester_open", "degree_id", "batch"]
            for r in cur.fetchall():
                print(dict(zip(cols, r)))
    except Exception as e:
        print("Error:", e)
    finally:
        conn.close()

if __name__ == "__main__":
    main()
