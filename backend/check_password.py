import sqlite3
import bcrypt

conn = sqlite3.connect('D:/cams-app-upload/backend/cams.db')
cursor = conn.cursor()

# Get the hash from the DB
cursor.execute("SELECT hashed_password FROM users WHERE email = 'faculty@cams.local'")
row = cursor.fetchone()
stored_hash = row[0]
print(f"Stored hash: {stored_hash}")

# Try common passwords
passwords = ["password", "password123", "admin", "admin123", "cams@123", "faculty123", "Pass@123", "123456", "Faculty@1"]
for p in passwords:
    match = bcrypt.checkpw(p.encode(), stored_hash.encode())
    print(f"  '{p}' -> {match}")

conn.close()
