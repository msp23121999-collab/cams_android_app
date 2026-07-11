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
            # Let's find a BA LLB student in semester 1
            cur.execute("""
                SELECT s.id, u.full_name, s.semester, s.section_id, s.degree_id, s.department_id
                FROM students s
                JOIN users u ON s.user_id = u.id
                WHERE s.semester = 1 AND s.degree_id = 'ba26ba26-1111-2222-3333-444444444444'
                LIMIT 1;
            """)
            stud = cur.fetchone()
            if not stud:
                print("No BA LLB student found in semester 1.")
                return
            print("Student info:", stud)
            sid, name, sem, sec_id, deg_id, dept_id = stud
            
            # Resolve section name
            cur.execute("SELECT section_name FROM sections WHERE id = %s;", (sec_id,))
            sec_name = cur.fetchone()[0]
            print("Resolved section name:", sec_name)
            
            # Query timetable entries
            cur.execute("""
                SELECT t.id, t.subject_id, c.name, s.section_name, ta.status
                FROM timetable t
                JOIN sections s ON t.section_id = s.id
                JOIN courses c ON t.subject_id = c.id
                LEFT JOIN timetable_approvals ta ON t.id = ta.timetable_id
                WHERE c.semester = %s
                  AND s.section_name = %s
                  AND t.is_deleted = False
                  AND s.is_deleted = False
                  AND c.is_deleted = False
                  AND c.degree_id = %s;
            """, (sem, sec_name, deg_id))
            
            rows = cur.fetchall()
            print(f"Query returned {len(rows)} rows:")
            for r in rows:
                print(r)
                
    except Exception as e:
        print("Error:", e)
    finally:
        conn.close()

if __name__ == "__main__":
    main()
