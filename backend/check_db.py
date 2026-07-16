import sqlite3

conn = sqlite3.connect('D:/cams-app-upload/backend/cams.db')
cursor = conn.cursor()

# Users table schema
print("=== USERS TABLE SCHEMA ===")
cursor.execute("PRAGMA table_info(users)")
for row in cursor.fetchall():
    print(f"  {row}")

# All faculty users
print("\n=== ALL FACULTY USERS ===")
cursor.execute("SELECT * FROM users WHERE role='faculty'")
cols = [d[0] for d in cursor.description]
print(f"  Columns: {cols}")
for row in cursor.fetchall():
    print(f"  {row}")

# Faculty profiles
print("\n=== FACULTY PROFILES ===")
cursor.execute("SELECT * FROM faculty_profiles")
cols = [d[0] for d in cursor.description]
print(f"  Columns: {cols}")
for row in cursor.fetchall():
    print(f"  {row}")

# Sections with faculty
print("\n=== SECTIONS WITH FACULTY ===")
cursor.execute("SELECT * FROM sections")
cols = [d[0] for d in cursor.description]
print(f"  Columns: {cols}")
for row in cursor.fetchall():
    print(f"  {row}")

# Courses
print("\n=== COURSES ===")
cursor.execute("SELECT * FROM courses")
cols = [d[0] for d in cursor.description]
print(f"  Columns: {cols}")
for row in cursor.fetchall():
    print(f"  {row}")

# Timetable
print("\n=== TIMETABLE ===")
cursor.execute("SELECT * FROM timetable LIMIT 10")
cols = [d[0] for d in cursor.description]
print(f"  Columns: {cols}")
for row in cursor.fetchall():
    print(f"  {row}")

# Study materials
print("\n=== STUDY MATERIALS ===")
cursor.execute("SELECT * FROM study_materials")
cols = [d[0] for d in cursor.description]
print(f"  Columns: {cols}")
for row in cursor.fetchall():
    print(f"  {row}")

# Assignments
print("\n=== ASSIGNMENTS ===")
cursor.execute("SELECT * FROM assignments")
cols = [d[0] for d in cursor.description]
print(f"  Columns: {cols}")
for row in cursor.fetchall():
    print(f"  {row}")

# Faculty research
print("\n=== FACULTY RESEARCH ===")
cursor.execute("SELECT * FROM faculty_research")
cols = [d[0] for d in cursor.description]
print(f"  Columns: {cols}")
for row in cursor.fetchall():
    print(f"  {row}")

# Faculty workload
print("\n=== FACULTY WORKLOAD ===")
cursor.execute("SELECT * FROM faculty_workload")
cols = [d[0] for d in cursor.description]
print(f"  Columns: {cols}")
for row in cursor.fetchall():
    print(f"  {row}")

# Salary
print("\n=== SALARY ===")
cursor.execute("SELECT * FROM salary")
cols = [d[0] for d in cursor.description]
print(f"  Columns: {cols}")
for row in cursor.fetchall():
    print(f"  {row}")

conn.close()
