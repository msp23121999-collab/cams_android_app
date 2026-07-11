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
                SELECT c.semester, ta.status, COUNT(*)
                FROM timetable t
                JOIN courses c ON t.subject_id = c.id
                LEFT JOIN timetable_approvals ta ON t.id = ta.timetable_id
                WHERE t.is_deleted = False
                GROUP BY c.semester, ta.status
                ORDER BY c.semester, ta.status;
            """)
            print("=== Timetable Slots Count by Semester & Status ===")
            for r in cur.fetchall():
                print(r)
    except Exception as e:
        print("Error:", e)
    finally:
        conn.close()

if __name__ == "__main__":
    main()
