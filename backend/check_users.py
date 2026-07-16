import sqlite3

conn = sqlite3.connect('D:/cams-app-upload/backend/cams.db')
cursor = conn.cursor()

# Get all users with their emails
print("=== ALL USERS ===")
cursor.execute("SELECT * FROM users")
cols = [d[0] for d in cursor.description]
print(f"  Columns: {cols}")
for row in cursor.fetchall():
    print(f"  {row}")

conn.close()
