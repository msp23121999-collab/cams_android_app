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

# 2. Ensure 'L1' and 'L2' departments exist
cur.execute("SELECT id FROM departments WHERE code = 'L1';")
row_l1 = cur.fetchone()
if row_l1:
    l1_dept_id = row_l1[0]
    print(f"Found existing LLB (L1) department ID: {l1_dept_id}")
else:
    l1_dept_id = str(uuid.uuid4())
    cur.execute("""
        INSERT INTO departments (id, name, code, establish_year, created_at, updated_at, is_deleted)
        VALUES (%s, 'LLB', 'L1', 2026, NOW(), NOW(), false);
    """, (l1_dept_id,))
    print(f"Created new LLB (L1) department with ID: {l1_dept_id}")

cur.execute("SELECT id FROM departments WHERE code = 'L2';")
row_l2 = cur.fetchone()
if row_l2:
    l2_dept_id = row_l2[0]
    print(f"Found existing BA LLB (L2) department ID: {l2_dept_id}")
else:
    l2_dept_id = str(uuid.uuid4())
    cur.execute("""
        INSERT INTO departments (id, name, code, establish_year, created_at, updated_at, is_deleted)
        VALUES (%s, 'BA LLB', 'L2', 2026, NOW(), NOW(), false);
    """, (l2_dept_id,))
    print(f"Created new BA LLB (L2) department with ID: {l2_dept_id}")

# 3. Drop existing foreign keys referencing regulations
# We query them from the database to drop them dynamically
cur.execute("""
    SELECT tc.table_name, kcu.column_name, tc.constraint_name
    FROM information_schema.table_constraints AS tc 
    JOIN information_schema.key_column_usage AS kcu
      ON tc.constraint_name = kcu.constraint_name
      AND tc.table_schema = kcu.table_schema
    JOIN information_schema.constraint_column_usage AS ccu
      ON ccu.constraint_name = tc.constraint_name
      AND ccu.table_schema = tc.table_schema
    WHERE tc.constraint_type = 'FOREIGN KEY' AND ccu.table_name = 'regulations';
""")
fkeys = cur.fetchall()
print(f"Found foreign keys referencing regulations: {fkeys}")

for table, col, conname in fkeys:
    try:
        cur.execute(f"ALTER TABLE {table} DROP CONSTRAINT {conname};")
        print(f"Dropped foreign key constraint {conname} on {table}")
    except Exception as e:
        print(f"Error dropping constraint {conname}: {e}")
        conn.rollback()

# 4. Rename unique constraint on regulations
try:
    cur.execute("ALTER TABLE regulations RENAME CONSTRAINT uq_regulation_code_program_batch TO uq_degree_code_program_batch;")
    print("Renamed regulations unique constraint.")
except Exception as e:
    print(f"Info: unique constraint rename skipped (might already be renamed or table doesn't exist): {e}")
    conn.rollback()

# 5. Rename regulations table to degrees
cur.execute("SELECT EXISTS (SELECT FROM pg_tables WHERE tablename = 'degrees');")
if not cur.fetchone()[0]:
    try:
        cur.execute("ALTER TABLE regulations RENAME TO degrees;")
        print("Renamed table regulations to degrees.")
    except Exception as e:
        print(f"Error renaming table regulations: {e}")
        conn.rollback()
else:
    print("Table degrees already exists.")

# 6. Rename columns in referencing tables from regulation_id to degree_id
for table in ['academic_years', 'courses', 'students']:
    # Check if column regulation_id exists
    cur.execute(f"""
        SELECT EXISTS (
            SELECT 1 
            FROM information_schema.columns 
            WHERE table_name='{table}' AND column_name='regulation_id'
        );
    """)
    if cur.fetchone()[0]:
        try:
            cur.execute(f"ALTER TABLE {table} RENAME COLUMN regulation_id TO degree_id;")
            print(f"Renamed column regulation_id to degree_id in table {table}")
        except Exception as e:
            print(f"Error renaming column in {table}: {e}")
            conn.rollback()
    else:
        print(f"Column regulation_id does not exist in table {table}")

# 7. Add dept_id column to degrees table
cur.execute("""
    SELECT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name='degrees' AND column_name='dept_id'
    );
""")
if not cur.fetchone()[0]:
    try:
        cur.execute("ALTER TABLE degrees ADD COLUMN dept_id VARCHAR(36) REFERENCES departments(id);")
        print("Added dept_id column to degrees table.")
    except Exception as e:
        print(f"Error adding dept_id column: {e}")
        conn.rollback()
else:
    print("dept_id column already exists in degrees table.")

# 8. Set degrees dept_id to correct department
cur.execute("UPDATE degrees SET dept_id = %s WHERE program_level = 'UG' AND (dept_id IS NULL OR dept_id = '3b3cb02a-6b5b-4979-bdd5-3eab0a52ed42');", (l1_dept_id,))
cur.execute("UPDATE degrees SET dept_id = %s WHERE program_level = 'INTEGRATED' AND (dept_id IS NULL OR dept_id = '3b3cb02a-6b5b-4979-bdd5-3eab0a52ed42');", (l2_dept_id,))
print("Associated all degrees with correct LLB/BA LLB departments.")

# 9. Add foreign key constraints back pointing to degrees(id)
for table in ['academic_years', 'courses', 'students']:
    conname = f"fk_{table}_degree_id_degrees"
    try:
        cur.execute(f"ALTER TABLE {table} ADD CONSTRAINT {conname} FOREIGN KEY (degree_id) REFERENCES degrees(id);")
        print(f"Re-added foreign key constraint {conname} on {table}(degree_id)")
    except Exception as e:
        print(f"Info: Constraint {conname} might already exist: {e}")
        conn.rollback()

# 10. Update unique constraint on courses
try:
    cur.execute("ALTER TABLE courses DROP CONSTRAINT IF EXISTS uq_courses_code_regulation;")
    cur.execute("ALTER TABLE courses DROP CONSTRAINT IF EXISTS uq_courses_code_degree;")
    cur.execute("ALTER TABLE courses ADD CONSTRAINT uq_courses_code_degree UNIQUE (code, degree_id);")
    print("Updated courses unique constraint (code, degree_id)")
except Exception as e:
    print(f"Error updating courses unique constraint: {e}")
    conn.rollback()

# 11. Re-map referencing tables (skipping bulk re-mapping to single LAW department)
print("Skipping bulk department re-mapping to preserve separate LLB and BA LLB departments.")

# 12. Update degree names for readability (to B.A. LL.B. and LL.B.)
# integrated R2025 -> B.A. LL.B. (R2025)
cur.execute("UPDATE degrees SET name = 'B.A. LL.B. (R2025)' WHERE program_level = 'INTEGRATED' AND code = 'R2025';")
# integrated R2023 -> B.A. LL.B. (R2023)
cur.execute("UPDATE degrees SET name = 'B.A. LL.B. (R2023)' WHERE program_level = 'INTEGRATED' AND code = 'R2023';")
# UG R2023 -> LL.B. (R2023 - UG)
cur.execute("UPDATE degrees SET name = 'LL.B. (R2023 - UG)' WHERE program_level = 'UG' AND name = 'R2023 - UG';")
# UG R2023 batch -> LL.B. (R2023 - 2025-2028)
cur.execute("UPDATE degrees SET name = 'LL.B. (R2023 - 2025-2028)' WHERE program_level = 'UG' AND name = 'R2023 - 2025-2028';")
print("Updated degree names in degrees table.")

# 13. Delete old LAW department
try:
    cur.execute("DELETE FROM departments WHERE code = 'LAW';")
    print("Deleted old LAW department record.")
except Exception as e:
    print(f"Info: Could not delete LAW department record: {e}")
    conn.rollback()

conn.commit()
cur.close()
conn.close()
print("\nDatabase migration completed successfully!")
