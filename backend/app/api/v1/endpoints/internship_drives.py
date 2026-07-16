"""
Internship Drives — simple JSON-based file store.
"""
from typing import Any, List
from fastapi import APIRouter

import os
import json

router = APIRouter()

DRIVES_FILE = os.path.join(
    os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(__file__)))),
    "internship_drives_db.json"
)

DEFAULT_DRIVES = [
    { "name": "Shardul Amarchand Mangaldas", "role": "Associate - Corporate", "package": "₹18-20 LPA", "date": "Oct 30, 2026", "status": "Hiring" },
    { "name": "Trilegal", "role": "Disputes Intern", "package": "₹25k / Month", "date": "Nov 05, 2026", "status": "Closing Soon" },
    { "name": "L&T Infrastructure", "role": "In-house Legal Counsel", "package": "₹12-15 LPA", "date": "Nov 12, 2026", "status": "Hiring" },
    { "name": "High Court of Delhi", "role": "Judicial Clerkship", "package": "₹40k / Month", "date": "Nov 15, 2026", "status": "Open" }
]

def load_drives() -> list:
    if not os.path.exists(DRIVES_FILE):
        # Initialize default drives file
        save_drives_to_file(DEFAULT_DRIVES)
        return DEFAULT_DRIVES
    with open(DRIVES_FILE, "r", encoding="utf-8") as f:
        try:
            return json.load(f)
        except Exception:
            return DEFAULT_DRIVES

def save_drives_to_file(data: list):
    with open(DRIVES_FILE, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=4, ensure_ascii=False)

@router.get("", summary="Get all live internship drives")
def get_drives() -> List[Any]:
    return load_drives()

@router.post("", summary="Replace the full internship drives list")
def save_drives(drives: List[Any]) -> dict:
    save_drives_to_file(drives)
    return {"ok": True, "count": len(drives)}

APPLICATIONS_FILE = os.path.join(
    os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(__file__)))),
    "internship_applications_db.json"
)

def load_applications() -> list:
    if not os.path.exists(APPLICATIONS_FILE):
        return []
    with open(APPLICATIONS_FILE, "r", encoding="utf-8") as f:
        try:
            return json.load(f)
        except Exception:
            return []

def save_applications_to_file(data: list):
    with open(APPLICATIONS_FILE, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=4, ensure_ascii=False)

@router.post("/apply", summary="Apply for an internship drive")
def apply_drive(application: dict) -> dict:
    apps = load_applications()
    apps.append(application)
    save_applications_to_file(apps)
    return {"ok": True}

@router.get("/applications", summary="Get all internship drive applications")
def get_applications() -> list:
    return load_applications()
