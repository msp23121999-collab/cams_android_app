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
                SELECT t.id, t.weekday, t.start_time, t.end_time, t.room, t.faculty_id
                FROM timetable t
                JOIN timetable_approvals ta ON t.id = ta.timetable_id
                WHERE t.faculty_id = '355c5c1d-3567-4edf-8f08-29a4a03dee62' AND t.is_deleted = False AND ta.status = 'APPROVED' AND ta.is_deleted = False;
            """)
            print("=== Ramesh Kumar Approved Slots ===")
            for r in cur.fetchall():
                print(r)
    except Exception as e:
        print("Error:", e)
    finally:
        conn.close()

if __name__ == "__main__":
    main()
