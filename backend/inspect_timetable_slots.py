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
            # Let's inspect faculty first
            cur.execute("""
                SELECT id, full_name, email, role, department_id FROM users WHERE role IN ('FACULTY', 'HOD');
            """)
            print("=== Faculty/HODs ===")
            cols = ["id", "full_name", "email", "role", "department_id"]
            faculty_list = []
            for r in cur.fetchall():
                row = dict(zip(cols, r))
                print(row)
                faculty_list.append(row)
            
            # Let's inspect timetable slots
            cur.execute("""
                SELECT id, section_id, subject_id, faculty_id, weekday, start_time, end_time, room, is_deleted
                FROM timetable
                WHERE is_deleted = False;
            """)
            print("\n=== Timetable Slots ===")
            t_cols = ["id", "section_id", "subject_id", "faculty_id", "weekday", "start_time", "end_time", "room", "is_deleted"]
            slots = []
            for r in cur.fetchall():
                row = dict(zip(t_cols, r))
                print(row)
                slots.append(row)
                
            # Let's inspect course / subject allocation or subjects table
            cur.execute("""
                SELECT id, name, code, dept_id, semester FROM courses;
            """)
            print("\n=== Courses ===")
            c_cols = ["id", "name", "code", "dept_id", "semester"]
            for r in cur.fetchall():
                print(dict(zip(c_cols, r)))
                
    except Exception as e:
        print("Error:", e)
    finally:
        conn.close()

if __name__ == "__main__":
    main()
