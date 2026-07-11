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
                SELECT id, name, code, degree_id, semester
                FROM courses
                WHERE degree_id = 'llb26llb-1111-2222-3333-444444444444' AND semester = 1;
            """)
            print("=== Semester 1 Courses for active LLB degree ===")
            for r in cur.fetchall():
                print(r)
                
    except Exception as e:
        print("Error:", e)
    finally:
        conn.close()

if __name__ == "__main__":
    main()
