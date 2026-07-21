"""Money-path regression tests.

Fee state and payment verification are the parts of this system where a bug
costs real money or lets someone mark an unpaid fee as paid.  Each test below
locks in a specific failure that was live in this repo.
"""
from __future__ import annotations

import hashlib
import hmac
import json

import pytest

from tests.conftest import auth


async def test_partial_payment_does_not_mark_record_paid(client, seed):
    """A partial payment must leave the remainder collectable.

    Regression: `POST /fees/{id}/pay` set `record.status = FeeStatus.PAID`
    unconditionally after ANY payment amount.  A 75,000 payment against an
    85,000 fee flipped the record to PAID, and the outstanding 10,000 then became
    permanently uncollectable -- every later attempt was rejected with
    "Fee already paid", for students and parents alike.
    """
    summary = await client.get("/api/v1/students/fees", headers=auth(seed.student_a_user))
    assert summary.status_code == 200, summary.text
    record = next(r for r in summary.json()["records"] if r["remaining_amount"] > 0)
    part = round(record["remaining_amount"] / 4, 2)
    assert part > 0

    paid = await client.post(
        f"/api/v1/fees/{record['record_id']}/pay",
        headers=auth(seed.student_a_user),
        json={"amount": part, "mode": "Cash"},
    )
    assert paid.status_code == 200, paid.text
    assert paid.json()["status"] != "paid", (
        "a partial payment reported the record as fully paid; the remaining "
        "balance is now uncollectable"
    )

    after = await client.get("/api/v1/students/fees", headers=auth(seed.student_a_user))
    updated = next(r for r in after.json()["records"] if r["record_id"] == record["record_id"])
    assert updated["remaining_amount"] > 0, "remaining balance vanished after a partial payment"
    assert updated["status"] != "paid"


async def test_stored_status_matches_computed_status_after_partial_payment(client, seed):
    """The stored column and the computed summary must agree.

    `FeeStatus` gained a PARTIALLY_PAID member so a part-settled record can be
    represented directly. Before that it had to sit in PENDING while the summary
    service reported "partially_paid", so screens reading the stored column could
    not tell "nothing paid" apart from "half paid".
    """
    from app.db.models.fee import FeeRecord, FeeStatus
    from app.db.session import AsyncSessionLocal

    summary = await client.get("/api/v1/students/fees", headers=auth(seed.student_a_user))
    record = next((r for r in summary.json()["records"] if r["remaining_amount"] > 0), None)
    if record is None:
        pytest.skip("student A has no outstanding balance in this run")

    paid = await client.post(
        f"/api/v1/fees/{record['record_id']}/pay",
        headers=auth(seed.student_a_user),
        json={"amount": round(record["remaining_amount"] / 3, 2), "mode": "Cash"},
    )
    assert paid.status_code == 200, paid.text

    async with AsyncSessionLocal() as db:
        stored = await db.get(FeeRecord, record["record_id"])
        assert stored is not None
        assert stored.status == FeeStatus.PARTIALLY_PAID, (
            f"stored status is {stored.status!r} after a partial payment; "
            "it should be PARTIALLY_PAID so it matches the computed summary"
        )


async def test_paying_the_full_balance_marks_record_paid(client, seed):
    """The other half of the contract: settling the balance must close the record.

    Without this, a fix for the partial-payment bug that simply never sets PAID
    would pass the test above while quietly breaking collection.
    """
    summary = await client.get("/api/v1/students/fees", headers=auth(seed.student_a_user))
    record = next((r for r in summary.json()["records"] if r["remaining_amount"] > 0), None)
    if record is None:
        pytest.skip("student A has no outstanding balance in this run")

    paid = await client.post(
        f"/api/v1/fees/{record['record_id']}/pay",
        headers=auth(seed.student_a_user),
        json={"amount": record["remaining_amount"], "mode": "Cash"},
    )
    assert paid.status_code == 200, paid.text

    after = await client.get("/api/v1/students/fees", headers=auth(seed.student_a_user))
    updated = next(r for r in after.json()["records"] if r["record_id"] == record["record_id"])
    assert updated["remaining_amount"] <= 0.01, updated
    assert updated["status"] == "paid", updated


async def test_fee_summary_exposes_due_date_for_a_fully_paid_student(client, seed):
    """`due_date` is null when nothing is outstanding.

    Regression on the client side: the Android DTO declared `dueDate` as a
    non-null String, so Moshi threw a JsonDataException for any fully-paid child
    and took out BOTH the parent fee screen and the parent dashboard (they share
    one try-block).  Student B is seeded fully paid to keep that shape covered.
    """
    response = await client.get(
        "/api/v1/students/parent/child/fees", headers=auth(seed.parent_b)
    )
    assert response.status_code == 200, response.text
    body = response.json()
    assert "due_date" in body, "due_date must be present in the payload even when null"
    assert body["pending_balance"] <= 0.01, "student B is seeded as fully paid"


async def test_webhook_rejects_a_forged_signature(client, seed, monkeypatch):
    """The Razorpay webhook is unauthenticated by design and must rely on HMAC.

    It is in PUBLIC_PATHS in test_access_control.py precisely because Razorpay
    cannot send one of our bearer tokens, so the signature check is the only
    thing standing between an attacker and a free "payment".
    """
    from app.core.config import settings

    monkeypatch.setattr(settings, "RAZORPAY_WEBHOOK_SECRET", "test-webhook-secret")

    payload = {
        "event": "payment.captured",
        "payload": {"payment": {"entity": {"id": "pay_forged", "order_id": "order_forged"}}},
    }
    response = await client.post(
        "/api/v1/fees/webhook/razorpay",
        content=json.dumps(payload),
        headers={"X-Razorpay-Signature": "definitely-not-a-valid-signature",
                 "Content-Type": "application/json"},
    )
    assert response.status_code >= 400, (
        "a forged webhook signature was accepted; an attacker could mark fees paid"
    )


async def test_webhook_accepts_a_correctly_signed_payload(client, seed, monkeypatch):
    """Proves the rejection above is signature-based, not blanket refusal.

    A test that only checks the forged case would still pass against an endpoint
    that rejects everything -- which would silently break real payments.
    """
    from app.core.config import settings

    secret = "test-webhook-secret"
    monkeypatch.setattr(settings, "RAZORPAY_WEBHOOK_SECRET", secret)

    body = json.dumps({
        "event": "payment.captured",
        "payload": {"payment": {"entity": {"id": "pay_ok", "order_id": "order_ok"}}},
    })
    signature = hmac.new(secret.encode(), body.encode(), hashlib.sha256).hexdigest()

    response = await client.post(
        "/api/v1/fees/webhook/razorpay",
        content=body,
        headers={"X-Razorpay-Signature": signature, "Content-Type": "application/json"},
    )
    assert response.status_code < 400, (
        f"a correctly signed webhook was rejected ({response.status_code}); "
        f"real Razorpay callbacks would fail: {response.text[:200]}"
    )


async def test_parent_cannot_pay_against_an_unlinked_childs_record(client, seed):
    """Ownership is checked on the fee record itself, not just the child_id."""
    response = await client.post(
        f"/api/v1/students/parent/child/fees/{seed.fee_record_b}/create-order",
        headers=auth(seed.parent_a),
        json={"amount": 100},
    )
    assert response.status_code in (403, 404), response.text
