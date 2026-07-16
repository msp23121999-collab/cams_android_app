import requests
import json
import sys
sys.stdout.reconfigure(encoding='utf-8')

BASE = "http://localhost:8000/api/v1"

r = requests.post(f"{BASE}/auth/login", json={"email": "faculty@cams.local", "password": "Password@1"})
token = r.json()["access_token"]
headers = {"Authorization": f"Bearer {token}"}
print(f"Login: {r.status_code}\n")

endpoints = [
    ("GET", "/faculty/dashboard/metrics", "Dashboard Metrics"),
    ("GET", "/faculty/profile", "Profile"),
    ("GET", "/faculty/timetable", "Timetable"),
    ("GET", "/faculty/assignments", "Assignments"),
    ("GET", "/faculty/materials", "Study Materials"),
    ("GET", "/faculty/students", "Students"),
    ("GET", "/faculty/research/list", "Research"),
    ("GET", "/faculty/salary-slips", "Salary Slips"),
    ("GET", "/faculty/leaves", "Leaves"),
    ("GET", "/faculty/payroll", "Payroll"),
    ("GET", "/faculty/notices", "Notices"),
    ("GET", "/faculty/notifications", "Notifications"),
]

for method, path, label in endpoints:
    try:
        resp = requests.get(f"{BASE}{path}", headers=headers)
        body = resp.json()
        if isinstance(body, list):
            print(f"  OK {label} ({path}): HTTP {resp.status_code} -> {len(body)} items")
        elif isinstance(body, dict):
            if "detail" in body:
                print(f"  FAIL {label} ({path}): HTTP {resp.status_code} -> {body['detail']}")
            else:
                print(f"  OK {label} ({path}): HTTP {resp.status_code} -> keys: {list(body.keys())}")
        else:
            print(f"  ?? {label} ({path}): HTTP {resp.status_code} -> {body}")
    except Exception as e:
        print(f"  FAIL {label} ({path}): ERROR {e}")
