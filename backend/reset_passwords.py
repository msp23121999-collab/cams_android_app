import sys
sys.path.insert(0, 'D:/cams-app-upload/backend')
from app.core.security import hash_password
import sqlite3

conn = sqlite3.connect('D:/cams-app-upload/backend/cams.db')
cursor = conn.cursor()

# Reset ALL user passwords to "Password@1"
new_hash = hash_password("Password@1")
cursor.execute("UPDATE users SET hashed_password = ?", (new_hash,))
conn.commit()

# Verify the update
cursor.execute("SELECT email, role FROM users")
for row in cursor.fetchall():
    print(f"  Updated: {row[0]} ({row[1]})")

print(f"\nAll passwords reset to: Password@1")
conn.close()
