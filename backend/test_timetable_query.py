import sys
import asyncio
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
            # Get a list of faculty
            cur.execute("SELECT id, full_name FROM users WHERE role = 'FACULTY';")
            facs = cur.fetchall()
            print("=== Faculties ===")
            for f in facs:
                print(f)
                
            for fid, fname in facs:
                # Query timetable entries directly
                cur.execute("SELECT COUNT(*) FROM timetable WHERE faculty_id = %s AND is_deleted = False;", (fid,))
                raw_count = cur.fetchone()[0]
                
                # Query timetable entries joined with timetable_approvals
                cur.execute("""
                    SELECT COUNT(*)
                    FROM timetable t
                    JOIN timetable_approvals ta ON t.id = ta.timetable_id
                    WHERE t.faculty_id = %s AND t.is_deleted = False AND ta.status = 'APPROVED' AND ta.is_deleted = False;
                """, (fid,))
                approved_count = cur.fetchone()[0]
                
                print(f"Faculty {fname} (ID {fid}): raw slots={raw_count}, approved slots={approved_count}")
    except Exception as e:
        print("Error:", e)
    finally:
        conn.close()

if __name__ == "__main__":
    main()
