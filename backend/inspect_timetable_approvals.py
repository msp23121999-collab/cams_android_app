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
            cur.execute("SELECT COUNT(*) FROM timetable_approvals;")
            print("Total timetable approvals:", cur.fetchone()[0])
            
            cur.execute("SELECT * FROM timetable_approvals LIMIT 10;")
            print("Timetable approvals sample:")
            for r in cur.fetchall():
                print(r)
    except Exception as e:
        print("Error:", e)
    finally:
        conn.close()

if __name__ == "__main__":
    main()
