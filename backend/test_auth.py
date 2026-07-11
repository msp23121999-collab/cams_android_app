import requests

base_url = "http://localhost:8000/api/v1"

# Test Login
res = requests.post(f"{base_url}/auth/login", json={"email": "teststudent@campus.local", "password": "password123"})
print(f"Login Status: {res.status_code}")
data = res.json()
token = data.get("access_token")
refresh_token = data.get("refresh_token")
print(f"Role: {data.get('role')}")

# Test Me
res_me = requests.get(f"{base_url}/auth/me", headers={"Authorization": f"Bearer {token}"})
print(f"Me Status: {res_me.status_code}")
print(f"Me Data: {res_me.json()}")

# Test Refresh
res_ref = requests.post(f"{base_url}/auth/refresh", json={"refresh_token": refresh_token})
print(f"Refresh Status: {res_ref.status_code}")
ref_data = res_ref.json()
print(f"New Access Token exists: {bool(ref_data.get('access_token'))}")
