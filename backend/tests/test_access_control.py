"""Access-control regression tests.

Every access-control hole found in this codebase was found by hand, one at a
time.  This suite turns that into a standing check.

Real bugs that motivated each test are named in the docstrings so nobody
"simplifies" a case away without understanding what it was protecting against.
"""
from __future__ import annotations

import pytest

from tests.conftest import auth

# Routes that are legitimately reachable without a token.  Keep this list SHORT
# and justified — it is the thing standing between a new endpoint and an
# accidental public data leak.  Adding an entry should require a real reason.
PUBLIC_PATHS: set[str] = {
    "/api/v1/auth/login",
    "/api/v1/auth/refresh",
    "/api/v1/auth/logout",
    "/api/v1/auth/forgot-password",
    "/api/v1/auth/reset-password",
    "/api/v1/auth/confirm-email-change",
    # Unauthenticated on purpose so load balancers / uptime probes can call it.
    # Returns only {"status": "ok"}; the driver error is logged, never returned.
    "/api/v1/test_connection",
    # Razorpay calls this server-to-server, so it cannot carry one of our tokens.
    # It is authenticated by HMAC signature against RAZORPAY_WEBHOOK_SECRET rather
    # than by a bearer token -- see test_money.py, which asserts a forged payload
    # cannot mark a fee paid.
    "/api/v1/fees/webhook/razorpay",
}

# Path prefixes that are documentation/asset surfaces rather than API data.
PUBLIC_PREFIXES = ("/static", "/uploads", "/docs", "/redoc", "/openapi")


def _api_routes(app):
    """Every /api/v1 route with a concrete (non-parameterised) path."""
    out = []
    for route in app.routes:
        path = getattr(route, "path", "")
        methods = getattr(route, "methods", None) or set()
        if not path.startswith("/api/v1"):
            continue
        if path in PUBLIC_PATHS or path.startswith(PUBLIC_PREFIXES):
            continue
        # Parameterised paths need real ids to be meaningful; the unauth check
        # below still works because auth runs before path params are resolved.
        for method in sorted(methods & {"GET", "POST", "PATCH", "PUT", "DELETE"}):
            out.append((method, path))
    return sorted(set(out))


async def test_no_api_route_is_reachable_without_a_token(app, client):
    """No /api/v1 route may return 2xx to an anonymous caller.

    This is the test that would have caught, on the day it was written:
      * GET /students/temp-debug-users  -- returned every user's email, role and
        id to anyone at all, with no dependency of any kind.
      * POST /teaching-logs/notifications/read/{id} -- no current_user, so any
        caller could mutate another user's notification state.
    """
    offenders = []
    for method, path in _api_routes(app):
        response = await client.request(method, path)
        if response.status_code < 300:
            offenders.append(f"{method} {path} -> {response.status_code}")
    assert not offenders, (
        "These routes answered an UNAUTHENTICATED request with a success status.\n"
        "Either add an auth dependency, or if the route is genuinely public add it\n"
        "to PUBLIC_PATHS in this file with a comment saying why:\n  "
        + "\n  ".join(offenders)
    )


# ── wrong-role checks ────────────────────────────────────────────────────────

@pytest.mark.parametrize(
    "method,path",
    [
        ("GET", "/api/v1/faculty/hod/attendance/monitoring"),
        ("GET", "/api/v1/faculty/hod/leaves"),
        ("GET", "/api/v1/teaching-logs/hod/dashboard"),
    ],
)
async def test_student_cannot_reach_hod_endpoints(client, seed, method, path):
    response = await client.request(method, path, headers=auth(seed.student_a_user))
    assert response.status_code in (401, 403), (
        f"{method} {path} let a STUDENT token through with "
        f"{response.status_code}: {response.text[:200]}"
    )


async def test_student_cannot_reach_parent_payment_endpoints(client, seed):
    """The parent fee-payment endpoints are money paths scoped to PARENT.

    They resolve the student from an explicit child_id rather than from
    current_user, so letting another role in would bypass the linkage check.
    """
    response = await client.post(
        f"/api/v1/students/parent/child/fees/{seed.fee_record_a}/create-order",
        headers=auth(seed.student_a_user),
        json={"amount": 100},
    )
    assert response.status_code in (401, 403), response.text


async def test_temp_debug_users_is_not_open(client, seed):
    """Regression: this endpoint had no auth dependency at all."""
    anonymous = await client.get("/api/v1/students/temp-debug-users")
    assert anonymous.status_code in (401, 403, 404)

    as_student = await client.get(
        "/api/v1/students/temp-debug-users", headers=auth(seed.student_a_user)
    )
    assert as_student.status_code in (401, 403, 404), as_student.text


# ── IDOR / cross-tenant checks ───────────────────────────────────────────────

async def test_parent_cannot_read_another_familys_child(client, seed):
    """Parent A must not reach student B by passing an explicit child_id."""
    response = await client.get(
        "/api/v1/students/parent/child/fees",
        headers=auth(seed.parent_a),
        params={"child_id": seed.student_b_id},
    )
    assert response.status_code in (403, 404), (
        "parent A read a child they are not linked to: "
        f"{response.status_code} {response.text[:200]}"
    )


async def test_parent_cannot_create_order_for_unlinked_child(client, seed):
    """Money path: ownership is re-checked on the fee record, not just the child."""
    response = await client.post(
        f"/api/v1/students/parent/child/fees/{seed.fee_record_b}/create-order",
        headers=auth(seed.parent_a),
        params={"child_id": seed.student_b_id},
        json={"amount": 100},
    )
    assert response.status_code in (403, 404), response.text


async def test_hod_cannot_read_another_departments_students(client, seed):
    """An HOD's scope stops at their own department.

    `hod_other` owns the Commerce department and must not see Law students.
    """
    response = await client.get(
        "/api/v1/faculty/students/list", headers=auth(seed.hod_other)
    )
    if response.status_code != 200:
        pytest.skip(f"roster endpoint unavailable for this role: {response.status_code}")
    rows = response.json()
    rows = rows if isinstance(rows, list) else rows.get("students", [])
    leaked = [r for r in rows if r.get("student_id") in {seed.student_a_id, seed.student_b_id}]
    assert not leaked, "HOD of another department received this department's students"


async def test_faculty_roster_excludes_sensitive_pii(client, seed):
    """Faculty get a roster, not a data dump.

    Aadhaar number, parent income and medical info are for staff-level screens;
    a faculty member listing their class has no need for them.
    """
    response = await client.get(
        "/api/v1/faculty/students/list", headers=auth(seed.faculty)
    )
    assert response.status_code == 200, response.text
    rows = response.json()
    rows = rows if isinstance(rows, list) else rows.get("students", [])
    forbidden = {"aadhaar_number", "parent_annual_income", "medical_info",
                 "current_address", "permanent_address"}
    for row in rows:
        present = forbidden & set(row)
        assert not present, f"faculty roster exposed {sorted(present)}"
