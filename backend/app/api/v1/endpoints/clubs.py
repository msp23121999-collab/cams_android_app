import os
import json
from typing import Any, List
from fastapi import APIRouter, Depends, HTTPException, status
from app.core.dependencies import get_current_user
from app.db.models.user import User

router = APIRouter()

# ─── File Paths ──────────────────────────────────────────────────────────────
DB_DIR = os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(__file__))))
CLUBS_FILE = os.path.join(DB_DIR, "clubs_db.json")
MEMBERSHIPS_FILE = os.path.join(DB_DIR, "club_memberships_db.json")

# ─── Load & Save Helpers ──────────────────────────────────────────────────────
def load_data(filepath: str, default: Any) -> Any:
    if not os.path.exists(filepath):
        save_data(filepath, default)
        return default
    with open(filepath, "r", encoding="utf-8") as f:
        try:
            return json.load(f)
        except Exception:
            return default

def save_data(filepath: str, data: Any):
    with open(filepath, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=4, ensure_ascii=False)

# ─── Endpoints ────────────────────────────────────────────────────────────────
@router.get("", summary="Get all clubs and current student's roles")
def get_clubs(current_user: User = Depends(get_current_user)) -> List[Any]:
    clubs = load_data(CLUBS_FILE, [])
    memberships = load_data(MEMBERSHIPS_FILE, [])

    # Map club_id -> role for the current user
    user_roles = {}
    for m in memberships:
        if m.get("user_id") == str(current_user.id):
            user_roles[int(m.get("club_id"))] = m.get("role")

    # Add role to each club object
    for club in clubs:
        club_id = int(club.get("id"))
        club["role"] = user_roles.get(club_id, "None")

    return clubs

@router.post("/{club_id}/join", summary="Join a club")
def join_club(club_id: int, current_user: User = Depends(get_current_user)) -> Any:
    clubs = load_data(CLUBS_FILE, [])
    memberships = load_data(MEMBERSHIPS_FILE, [])

    # Find the club
    target_club = None
    for club in clubs:
        if int(club.get("id")) == club_id:
            target_club = club
            break

    if not target_club:
        raise HTTPException(status_code=404, detail="Club not found")

    # Find or create membership
    membership_found = False
    role_changed = False
    for m in memberships:
        if m.get("user_id") == str(current_user.id) and int(m.get("club_id")) == club_id:
            membership_found = True
            if m.get("role") == "None":
                m["role"] = "Member"
                role_changed = True
            break

    if not membership_found:
        memberships.append({
            "user_id": str(current_user.id),
            "club_id": club_id,
            "role": "Member"
        })
        role_changed = True

    # If the user wasn't a member before, increment the club members count
    if role_changed or not membership_found:
        target_club["members"] = target_club.get("members", 0) + 1
        save_data(CLUBS_FILE, clubs)
        save_data(MEMBERSHIPS_FILE, memberships)

    target_club["role"] = "Member"
    return target_club

@router.post("/{club_id}/leave", summary="Leave a club")
def leave_club(club_id: int, current_user: User = Depends(get_current_user)) -> Any:
    clubs = load_data(CLUBS_FILE, [])
    memberships = load_data(MEMBERSHIPS_FILE, [])

    # Find the club
    target_club = None
    for club in clubs:
        if int(club.get("id")) == club_id:
            target_club = club
            break

    if not target_club:
        raise HTTPException(status_code=404, detail="Club not found")

    # Find membership
    role_changed = False
    for m in memberships:
        if m.get("user_id") == str(current_user.id) and int(m.get("club_id")) == club_id:
            if m.get("role") != "None":
                m["role"] = "None"
                role_changed = True
            break

    if role_changed:
        target_club["members"] = max(0, target_club.get("members", 0) - 1)
        save_data(CLUBS_FILE, clubs)
        save_data(MEMBERSHIPS_FILE, memberships)

    target_club["role"] = "None"
    return target_club
