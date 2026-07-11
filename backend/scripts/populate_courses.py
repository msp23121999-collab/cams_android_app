import os
import re
import psycopg2
import uuid

# 1. Database Connection Setup
backend_dir = os.path.dirname(os.path.abspath(__file__))
env_path = os.path.join(backend_dir, "..", ".env")
if not os.path.exists(env_path):
    env_path = os.path.join(backend_dir, ".env")

with open(env_path) as f:
    content = f.read()

m = re.search(r"DATABASE_URL=(.+)", content)
if not m:
    print("Could not find DATABASE_URL in .env")
    exit(1)

raw_url = m.group(1).strip()
raw_url = raw_url.replace("postgresql+asyncpg://", "")
raw_url = raw_url.replace("%40", "@")

# Parse user:pass@host:port/db
at_idx = raw_url.rfind("@")
user_pass = raw_url[:at_idx]
host_rest = raw_url[at_idx+1:]
colon_pw = user_pass.index(":")
user = user_pass[:colon_pw]
password = user_pass[colon_pw+1:]
slash_idx = host_rest.index("/")
host_port = host_rest[:slash_idx]
dbname = host_rest[slash_idx+1:]

if ":" in host_port:
    host, port = host_port.split(":", 1)
else:
    host, port = host_port, "5432"

print(f"Connecting to database {dbname} at {host}:{port}...")
conn = psycopg2.connect(
    host=host, port=int(port), dbname=dbname,
    user=user, password=password
)
cur = conn.cursor()

# Get department ID for L1 (LLB) and L2 (BA LLB)
cur.execute("SELECT id FROM departments WHERE code = 'L1';")
row_l1 = cur.fetchone()
if not row_l1:
    print("Error: Department of LLB (L1) not found in DB!")
    cur.close()
    conn.close()
    exit(1)
llb_dept_id = row_l1[0]

cur.execute("SELECT id FROM departments WHERE code = 'L2';")
row_l2 = cur.fetchone()
if not row_l2:
    print("Error: Department of BA LLB (L2) not found in DB!")
    cur.close()
    conn.close()
    exit(1)
ba_llb_dept_id = row_l2[0]

print(f"Using LLB ID: {llb_dept_id}, BA LLB ID: {ba_llb_dept_id}")

# 2. De-duplicate existing courses in DB by (code, degree_id)
print("\nScanning for duplicate course (code, degree_id) in the database...")
cur.execute("""
    SELECT code, degree_id, COUNT(*) 
    FROM courses 
    GROUP BY code, degree_id 
    HAVING COUNT(*) > 1;
""")
duplicates = cur.fetchall()

if duplicates:
    print(f"Found {len(duplicates)} duplicate course mappings:")
    for code, degree_id, count in duplicates:
        print(f"  Code '{code}' with degree_id '{degree_id}' appears {count} times.")
        if degree_id is None:
            cur.execute("SELECT id FROM courses WHERE code = %s AND degree_id IS NULL ORDER BY created_at ASC;", (code,))
        else:
            cur.execute("SELECT id FROM courses WHERE code = %s AND degree_id = %s ORDER BY created_at ASC;", (code, degree_id))
        ids = [r[0] for r in cur.fetchall()]
        
        kept_id = ids[0]
        delete_ids = ids[1:]
        
        print(f"    Keeping ID: {kept_id}")
        print(f"    Re-mapping references for IDs: {delete_ids}")
        
        # Re-map referencing tables
        for table, col in [
            ('sections', 'course_id'),
            ('timetable', 'subject_id'),
            ('exams', 'course_id'),
            ('subject_allocations', 'course_id'),
            ('internal_marks', 'subject_id')
        ]:
            for old_id in delete_ids:
                cur.execute(f"UPDATE {table} SET {col} = %s WHERE {col} = %s;", (kept_id, old_id))
                
        # Now delete the duplicate course records
        for old_id in delete_ids:
            cur.execute("DELETE FROM courses WHERE id = %s;", (old_id,))
        print(f"    Successfully cleaned duplicate code '{code}'.")
else:
    print("No duplicate courses found.")

# 3. Define Relatable Course List
# BA LLB (5 Years, 10 Semesters)
ba_llb_courses = [
    # Sem 1
    ("BALLB101", "Legal Methods & Legal System", 4, 1),
    ("BALLB102", "English & Legal Writing", 4, 1),
    ("BALLB103", "Political Science I (Political Theory)", 4, 1),
    ("BALLB104", "Law of Torts & Consumer Protection", 4, 1),
    ("BALLB105", "General Principles of Sociology", 4, 1),
    # Sem 2
    ("BALLB201", "Law of Contracts I (General Principles)", 4, 2),
    ("BALLB202", "English II (Literature & Communication)", 4, 2),
    ("BALLB203", "Political Science II (Indian Government)", 4, 2),
    ("BALLB204", "General Principles of Economics", 4, 2),
    ("BALLB205", "Sociology of India", 4, 2),
    # Sem 3
    ("BALLB301", "Law of Contracts II (Special Contracts)", 4, 3),
    ("BALLB302", "Constitutional Law I", 4, 3),
    ("BALLB303", "Law of Crimes I (Indian Penal Code)", 4, 3),
    ("BALLB304", "Economics II (Indian Economy)", 4, 3),
    ("BALLB305", "History of Courts & Legislature in India", 4, 3),
    # Sem 4
    ("BALLB401", "Constitutional Law II", 4, 4),
    ("BALLB402", "Law of Crimes II (Criminal Procedure)", 4, 4),
    ("BALLB403", "Family Law I (Hindu Law)", 4, 4),
    ("BALLB404", "Administrative Law", 4, 4),
    ("BALLB405", "Political Science III (International Relations)", 4, 4),
    # Sem 5
    ("BALLB501", "Family Law II (Muslim Law & Succession)", 4, 5),
    ("BALLB502", "Property Law & Easements", 4, 5),
    ("BALLB503", "Environmental Law", 4, 5),
    ("BALLB504", "Labour & Industrial Law I", 4, 5),
    ("BALLB505", "Jurisprudence (Legal Theory)", 4, 5),
    # Sem 6
    ("BALLB601", "Company Law", 4, 6),
    ("BALLB602", "Public International Law", 4, 6),
    ("BALLB603", "Law of Evidence", 4, 6),
    ("BALLB604", "Labour & Industrial Law II", 4, 6),
    ("BALLB605", "Human Rights Law", 4, 6),
    # Sem 7
    ("BALLB701", "Code of Civil Procedure & Limitation Act", 4, 7),
    ("BALLB702", "Principles of Taxation Law", 4, 7),
    ("BALLB703", "Intellectual Property Rights", 4, 7),
    ("BALLB704", "Land Laws & Local Laws", 4, 7),
    ("BALLB705", "Gender Justice & Feminist Jurisprudence", 4, 7),
    # Sem 8
    ("BALLB801", "Alternative Dispute Resolution (ADR)", 4, 8),
    ("BALLB802", "Cyber Law & Information Technology", 4, 8),
    ("BALLB803", "Banking & Insurance Law", 4, 8),
    ("BALLB804", "Interpretation of Statutes", 4, 8),
    ("BALLB805", "International Trade Law", 4, 8),
    # Sem 9
    ("BALLB901", "Professional Ethics & Bar-Bench Relations", 4, 9),
    ("BALLB902", "Drafting, Pleading & Conveyance (Clinical)", 4, 9),
    ("BALLB903", "Competition Law", 4, 9),
    ("BALLB904", "Forensic Science & Law", 4, 9),
    # Sem 10
    ("BALLB1001", "Moot Court Exercise & Internship", 4, 10),
    ("BALLB1002", "Public Interest Litigations & Legal Aid", 4, 10),
    ("BALLB1003", "Media & Law", 4, 10),
    ("BALLB1004", "Seminar Course: White Collar Crimes", 4, 10),
]

# LLB (3 Years, 6 Semesters)
llb_courses = [
    # Sem 1
    ("LLB101", "Jurisprudence (Legal Theory)", 4, 1),
    ("LLB102", "Constitutional Law I", 4, 1),
    ("LLB103", "Law of Contracts I", 4, 1),
    ("LLB104", "Law of Torts & Consumer Protection", 4, 1),
    ("LLB105", "Law of Crimes I (IPC / BNS)", 4, 1),
    # Sem 2
    ("LLB201", "Constitutional Law II", 4, 2),
    ("LLB202", "Law of Contracts II (Special Contracts)", 4, 2),
    ("LLB203", "Law of Crimes II (Criminal Procedure)", 4, 2),
    ("LLB204", "Family Law I (Hindu Law)", 4, 2),
    ("LLB205", "Administrative Law", 4, 2),
    # Sem 3
    ("LLB301", "Family Law II (Muslim Law & Succession)", 4, 3),
    ("LLB302", "Property Law & Easements", 4, 3),
    ("LLB303", "Environmental Law", 4, 3),
    ("LLB304", "Labour & Industrial Law I", 4, 3),
    ("LLB305", "Public International Law", 4, 3),
    # Sem 4
    ("LLB401", "Company Law", 4, 4),
    ("LLB402", "Law of Evidence", 4, 4),
    ("LLB403", "Labour & Industrial Law II", 4, 4),
    ("LLB404", "Alternative Dispute Resolution (ADR)", 4, 4),
    ("LLB405", "Human Rights Law", 4, 4),
    # Sem 5
    ("LLB501", "Code of Civil Procedure & Limitation Act", 4, 5),
    ("LLB502", "Principles of Taxation Law", 4, 5),
    ("LLB503", "Intellectual Property Rights", 4, 5),
    ("LLB504", "Cyber Law & Information Technology", 4, 5),
    ("LLB505", "Drafting, Pleading & Conveyance (Clinical)", 4, 5),
    # Sem 6
    ("LLB601", "Professional Ethics & Bar-Bench Relations", 4, 6),
    ("LLB602", "Moot Court Exercise & Internship", 4, 6),
    ("LLB603", "Public Interest Litigations & Legal Aid", 4, 6),
    ("LLB604", "Land Laws & Local Laws", 4, 6),
    ("LLB605", "Interpretation of Statutes", 4, 6),
]

# Fetch degrees from DB
cur.execute("SELECT id, name, code, program_level FROM degrees;")
degrees = cur.fetchall()

# Separate integrated and UG degrees
integrated_degree_ids = [d[0] for d in degrees if d[3] == 'INTEGRATED']
ug_degree_ids = [d[0] for d in degrees if d[3] == 'UG']

print(f"\nFound Integrated degrees: {integrated_degree_ids}")
print(f"Found UG degrees: {ug_degree_ids}")

# Insert or update courses in the database
print("\nInserting relatable courses for all active degree programs...")
added_count = 0
updated_count = 0

# BA LLB (Integrated)
for code, name, credits, semester in ba_llb_courses:
    for deg_id in integrated_degree_ids:
        cur.execute("SELECT id FROM courses WHERE code = %s AND degree_id = %s;", (code, deg_id))
        row = cur.fetchone()
        if row:
            cur.execute("""
                UPDATE courses 
                SET dept_id = %s, name = %s, credits = %s, semester = %s, updated_at = NOW(), is_deleted = false
                WHERE id = %s;
            """, (ba_llb_dept_id, name, credits, semester, row[0]))
            updated_count += 1
        else:
            # Check if there is a row with this code and degree_id IS NULL to reuse
            cur.execute("SELECT id FROM courses WHERE code = %s AND degree_id IS NULL;", (code,))
            null_row = cur.fetchone()
            if null_row:
                cur.execute("""
                    UPDATE courses 
                    SET dept_id = %s, degree_id = %s, name = %s, credits = %s, semester = %s, updated_at = NOW(), is_deleted = false
                    WHERE id = %s;
                """, (ba_llb_dept_id, deg_id, name, credits, semester, null_row[0]))
                updated_count += 1
            else:
                new_id = str(uuid.uuid4())
                cur.execute("""
                    INSERT INTO courses (id, dept_id, degree_id, code, name, credits, semester, created_at, updated_at, is_deleted)
                    VALUES (%s, %s, %s, %s, %s, %s, %s, NOW(), NOW(), false);
                """, (new_id, ba_llb_dept_id, deg_id, code, name, credits, semester))
                added_count += 1

# LLB (UG)
for code, name, credits, semester in llb_courses:
    for deg_id in ug_degree_ids:
        cur.execute("SELECT id FROM courses WHERE code = %s AND degree_id = %s;", (code, deg_id))
        row = cur.fetchone()
        if row:
            cur.execute("""
                UPDATE courses 
                SET dept_id = %s, name = %s, credits = %s, semester = %s, updated_at = NOW(), is_deleted = false
                WHERE id = %s;
            """, (llb_dept_id, name, credits, semester, row[0]))
            updated_count += 1
        else:
            cur.execute("SELECT id FROM courses WHERE code = %s AND degree_id IS NULL;", (code,))
            null_row = cur.fetchone()
            if null_row:
                cur.execute("""
                    UPDATE courses 
                    SET dept_id = %s, degree_id = %s, name = %s, credits = %s, semester = %s, updated_at = NOW(), is_deleted = false
                    WHERE id = %s;
                """, (llb_dept_id, deg_id, name, credits, semester, null_row[0]))
                updated_count += 1
            else:
                new_id = str(uuid.uuid4())
                cur.execute("""
                    INSERT INTO courses (id, dept_id, degree_id, code, name, credits, semester, created_at, updated_at, is_deleted)
                    VALUES (%s, %s, %s, %s, %s, %s, %s, NOW(), NOW(), false);
                """, (new_id, llb_dept_id, deg_id, code, name, credits, semester))
                added_count += 1

# Commit changes and close
conn.commit()
cur.close()
conn.close()

print(f"\nDone! Added {added_count} new course instances, updated {updated_count} existing courses.")
