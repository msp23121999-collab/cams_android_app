import sqlite3
conn = sqlite3.connect('cams.db')
tables = conn.execute("SELECT name FROM sqlite_master WHERE type='table'").fetchall()
for t in tables:
    count = conn.execute("SELECT COUNT(*) FROM " + t[0]).fetchone()[0]
    print(t[0], count)
