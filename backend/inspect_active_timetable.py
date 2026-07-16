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
            # Active cohort LLB (2026-2029)
            # Degree ID: llb26llb-1111-2222-3333-444444444444
            cur.execute("""
                SELECT t.id, c.name, c.code, s.section_name, u.full_name
                FROM timetable t
                JOIN sections s ON t.section_id = s.id
                JOIN courses c ON t.subject_id = c.id
                JOIN users u ON t.faculty_id = u.id
                WHERE c.degree_id = 'llb26llb-1111-2222-3333-444444444444' AND t.is_deleted = False;
            """)
            print("=== Timetable slots for active cohort LLB (2026-2029) ===")
            for r in cur.fetchall():
                print(r)
                
            # Let's count timetable slots for degree 359e8b9a-2e39-4679-bc51-641f8da31a97
            cur.execute("""
                SELECT COUNT(*)
                FROM timetable t
                JOIN courses c ON t.subject_id = c.id
                WHERE c.degree_id = '359e8b9a-2e39-4679-bc51-641f8da31a97' AND t.is_deleted = False;
            """)
            print("\nTimetable slots count for degree 359e8b9a-2e39-4679-bc51-641f8da31a97 (LLB legacy):", cur.fetchone()[0])
            
    except Exception as e:
        print("Error:", e)
    finally:
        conn.close()

if __name__ == "__main__":
    main()
