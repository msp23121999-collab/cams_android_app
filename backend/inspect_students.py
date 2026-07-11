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
                SELECT s.id, u.full_name, s.semester, s.section_id, s.degree_id, s.department_id, s.mentor_id
                FROM students s
                JOIN users u ON s.user_id = u.id
                LIMIT 10;
            """)
            print("=== Students Sample ===")
            cols = ["id", "name", "semester", "section_id", "degree_id", "department_id", "mentor_id"]
            for r in cur.fetchall():
                print(dict(zip(cols, r)))
    except Exception as e:
        print("Error:", e)
    finally:
        conn.close()

if __name__ == "__main__":
    main()
