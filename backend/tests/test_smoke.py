"""Sanity checks that the fixtures themselves work — if these fail, every other
failure in the suite is noise."""
from tests.conftest import auth


async def test_seed_student_can_read_own_fees(client, seed):
    r = await client.get("/api/v1/students/fees", headers=auth(seed.student_a_user))
    assert r.status_code == 200, r.text
    body = r.json()
    assert body["records"], "seeded student A should have at least one fee record"


async def test_seed_parent_can_read_child_fees(client, seed):
    r = await client.get("/api/v1/students/parent/child/fees", headers=auth(seed.parent_a))
    assert r.status_code == 200, r.text
