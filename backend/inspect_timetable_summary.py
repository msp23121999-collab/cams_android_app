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
            # Get department names and IDs
            cur.execute("SELECT id, name FROM departments;")
            depts = {r[0]: r[1] for r in cur.fetchall()}
            print("=== DEPARTMENTS ===")
            for did, dname in depts.items():
                print(f"  {did}: {dname}")
            
            # Count faculty
            cur.execute("SELECT COUNT(*) FROM users WHERE role = 'FACULTY';")
            print(f"\nTotal Faculty in DB: {cur.fetchone()[0]}")
            
            # Faculty details
            cur.execute("SELECT id, full_name, email, department_id FROM users WHERE role IN ('FACULTY', 'HOD');")
            faculties = cur.fetchall()
            print("\n=== FACULTY LIST ===")
            fac_names = {}
            for fid, name, email, did in faculties:
                fac_names[fid] = name
                print(f"  {fid}: {name} ({email}) | Dept: {depts.get(did, 'None')}")
            
            # Timetable slots counts
            cur.execute("SELECT COUNT(*) FROM timetable WHERE is_deleted = False;")
            print(f"\nTotal Active Timetable Slots: {cur.fetchone()[0]}")
            
            # Timetable slots grouped by faculty and course
            cur.execute("""
                SELECT t.faculty_id, c.name, c.code, COUNT(*)
                FROM timetable t
                JOIN courses c ON t.subject_id = c.id
                WHERE t.is_deleted = False
                GROUP BY t.faculty_id, c.name, c.code;
            """)
            print("\n=== TIMETABLE SLOTS GROUPED BY FACULTY & COURSE ===")
            for fid, cname, ccode, count in cur.fetchall():
                fname = fac_names.get(fid, f"Unknown ({fid})")
                print(f"  Faculty: {fname} | Subject: {cname} ({ccode}) | Count: {count}")
                
            # Let's check if there is a subject_allocations table
            cur.execute("""
                SELECT EXISTS (
                    SELECT FROM information_schema.tables 
                    WHERE table_schema = 'public' 
                    AND table_name = 'subject_allocations'
                );
            """)
            has_alloc_table = cur.fetchone()[0]
            print(f"\nSubject Allocations table exists: {has_alloc_table}")
            if has_alloc_table:
                cur.execute("""
                    SELECT sa.faculty_id, c.name, c.code
                    FROM subject_allocations sa
                    JOIN courses c ON sa.course_id = c.id;
                """)
                print("\n=== SUBJECT ALLOCATIONS ===")
                for fid, cname, ccode in cur.fetchall():
                    fname = fac_names.get(fid, f"Unknown ({fid})")
                    print(f"  Faculty: {fname} | Allocated Subject: {cname} ({ccode})")

    except Exception as e:
        print("Error:", e)
    finally:
        conn.close()

if __name__ == "__main__":
    main()
