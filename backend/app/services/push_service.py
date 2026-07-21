"""Server-originated push notifications via Firebase Cloud Messaging.

Configuration is optional. With no credentials the app behaves exactly as before:
in-app notifications still work, pushes are skipped with a log line, and nothing
raises. This mirrors how SMTP and Razorpay are treated — a missing integration
disables its feature rather than breaking the request that triggered it.

Set FIREBASE_CREDENTIALS_JSON (the service-account JSON, as a string) or
FIREBASE_CREDENTIALS_FILE (a path to it). Never commit either.
"""
from __future__ import annotations

import json
import logging
from typing import Iterable, Sequence

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config import settings
from app.db.models.device_token import DeviceToken

logger = logging.getLogger(__name__)

_app = None
_init_attempted = False


def _get_app():
    """Initialise the Firebase app once. Returns None when unconfigured."""
    global _app, _init_attempted
    if _app is not None or _init_attempted:
        return _app
    _init_attempted = True

    raw = (settings.FIREBASE_CREDENTIALS_JSON or "").strip()
    path = (settings.FIREBASE_CREDENTIALS_FILE or "").strip()
    if not raw and not path:
        logger.info("Push disabled: no Firebase credentials configured.")
        return None

    try:
        import firebase_admin
        from firebase_admin import credentials
    except ImportError:
        logger.warning(
            "Push disabled: firebase-admin is not installed. "
            "Add it to requirements.txt to enable server-sent notifications."
        )
        return None

    try:
        cred = credentials.Certificate(json.loads(raw)) if raw else credentials.Certificate(path)
        _app = firebase_admin.initialize_app(cred, name="cams-push")
        logger.info("Push enabled: Firebase initialised.")
    except Exception:
        logger.exception("Push disabled: Firebase credentials present but invalid.")
        _app = None
    return _app


def is_enabled() -> bool:
    return _get_app() is not None


async def tokens_for_users(db: AsyncSession, user_ids: Sequence[str]) -> list[DeviceToken]:
    if not user_ids:
        return []
    res = await db.execute(
        select(DeviceToken).where(
            DeviceToken.user_id.in_(list(user_ids)),
            DeviceToken.is_deleted.is_(False),
        )
    )
    return list(res.scalars().all())


async def send_to_users(
    db: AsyncSession,
    user_ids: Sequence[str],
    title: str,
    body: str,
    data: dict[str, str] | None = None,
) -> int:
    """Push to every registered device of the given users.

    Returns the number of messages accepted by FCM. Never raises: a failed push must
    not fail the action that triggered it (marking attendance, approving leave, ...).
    Tokens FCM reports as dead are removed so the table does not accumulate them.
    """
    app = _get_app()
    if app is None:
        return 0

    rows = await tokens_for_users(db, user_ids)
    if not rows:
        return 0

    try:
        from firebase_admin import messaging
    except ImportError:
        return 0

    sent = 0
    dead: list[DeviceToken] = []
    for row in rows:
        try:
            messaging.send(
                messaging.Message(
                    token=row.token,
                    notification=messaging.Notification(title=title, body=body),
                    data={k: str(v) for k, v in (data or {}).items()},
                ),
                app=app,
            )
            sent += 1
        except Exception as exc:  # noqa: BLE001 - classified below
            name = type(exc).__name__
            if name in {"UnregisteredError", "SenderIdMismatchError", "InvalidArgumentError"}:
                dead.append(row)
            else:
                logger.warning("Push to token %s… failed: %s", row.token[:12], exc)

    if dead:
        for row in dead:
            row.is_deleted = True
        await db.commit()
        logger.info("Removed %d dead device token(s).", len(dead))

    return sent
