import sys
sys.path.insert(0, 'D:/cams-app-upload/backend')
from app.core.security import verify_password, hash_password
import sqlite3

conn = sqlite3.connect('D:/cams-app-upload/backend/cams.db')
cursor = conn.cursor()
cursor.execute("SELECT email, hashed_password FROM users WHERE role IN ('FACULTY') LIMIT 1")
row = cursor.fetchone()
print(f"Email: {row[0]}, hash: {row[1][:30]}...")

passwords = ["password", "password123", "admin@123", "faculty@1", "cams@123", "Password@1", "Faculty1@"]
for p in passwords:
    result = verify_password(p, row[1])
    print(f"  '{p}' -> {result}")

# Generate a fresh hash for Password@1
new_hash = hash_password("Password@1")
print(f"\nNew hash for 'Password@1': {new_hash}")
conn.close()
