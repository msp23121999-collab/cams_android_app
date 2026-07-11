import sqlite3
conn = sqlite3.connect('cams.db')
cur = conn.cursor()
cur.execute("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name")
tables = [r[0] for r in cur.fetchall()]
print('Tables:', tables)

# Check attendance count
if 'attendances' in tables:
    cur.execute('SELECT COUNT(*) FROM attendances')
    print('Attendance records:', cur.fetchone()[0])
    cur.execute('SELECT * FROM attendances LIMIT 3')
    if cur.description:
        cols = [d[0] for d in cur.description]
        for r in cur.fetchall():
            print(dict(zip(cols,r)))

conn.close()
