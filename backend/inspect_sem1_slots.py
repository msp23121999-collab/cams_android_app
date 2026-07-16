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
                SELECT t.id, c.name, c.degree_id, s.section_name, s.id, ta.status
                FROM timetable t
                JOIN sections s ON t.section_id = s.id
                JOIN courses c ON t.subject_id = c.id
                LEFT JOIN timetable_approvals ta ON t.id = ta.timetable_id
                WHERE c.semester = 1;
            """)
            print("=== Semester 1 Timetable Slots ===")
            for r in cur.fetchall():
                print(r)
    except Exception as e:
        print("Error:", e)
    finally:
        conn.close()

if __name__ == "__main__":
    main()
