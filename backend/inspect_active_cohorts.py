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
            # Active academic years
            cur.execute("""
                SELECT ay.id, ay.name, ay.current_semester, ay.degree_id, ay.batch, d.name
                FROM academic_years ay
                JOIN degrees d ON ay.degree_id = d.id
                WHERE ay.is_active = True AND ay.is_deleted = False;
            """)
            print("=== Active Academic Years ===")
            cols = ["id", "name", "current_semester", "degree_id", "batch", "degree_name"]
            for r in cur.fetchall():
                print(dict(zip(cols, r)))
                
    except Exception as e:
        print("Error:", e)
    finally:
        conn.close()

if __name__ == "__main__":
    main()
