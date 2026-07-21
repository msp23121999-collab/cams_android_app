"""Application-level rate limiting for authentication endpoints.

Why this exists
---------------
Credential-stuffing protection. Only *authentication* routes are limited
(login, password reset request/confirm, password change) — dashboards and
list endpoints are deliberately untouched so legitimate authenticated
traffic is never throttled.

Proxy awareness
---------------
In production the app sits behind nginx/Caddy/ALB (see DEPLOYMENT.md 7.1).
If we keyed the limiter on ``request.client.host`` every request would look
like it came from the proxy and one attacker would lock out every user.
``client_ip_key`` therefore prefers the forwarded client IP.

Run uvicorn with ``--proxy-headers --forwarded-allow-ips=<proxy ip>`` in
production so Starlette itself also resolves ``request.client`` correctly.
Only set ``RATE_LIMIT_TRUST_FORWARDED_FOR=true`` when a trusted proxy is in
front of the app: without one, clients can spoof ``X-Forwarded-For`` and
trivially bypass the limit.

Storage
-------
Redis-backed when ``RATE_LIMIT_STORAGE_URI``/``REDIS_URL`` is reachable
(correct across multiple uvicorn workers); otherwise it degrades silently to
in-memory storage, which is per-worker and therefore allows up to
``workers x limit``. The app must keep running without Redis, so the
degradation is by design and is logged as a warning at startup.
"""
from __future__ import annotations

import logging

from fastapi import Request
from fastapi.responses import JSONResponse
from slowapi import Limiter
from slowapi.errors import RateLimitExceeded
from slowapi.util import get_remote_address

from app.core.config import settings

logger = logging.getLogger(__name__)


def client_ip_key(request: Request) -> str:
    """Best-effort real client IP, proxy-aware."""
    if settings.RATE_LIMIT_TRUST_FORWARDED_FOR:
        forwarded = request.headers.get("x-forwarded-for")
        if forwarded:
            # Left-most entry is the original client.
            first = forwarded.split(",")[0].strip()
            if first:
                return first
        real_ip = request.headers.get("x-real-ip")
        if real_ip and real_ip.strip():
            return real_ip.strip()
    return get_remote_address(request) or "unknown"


def _resolve_storage_uri() -> str:
    """Return a usable storage URI, falling back to in-memory if Redis is down."""
    uri = (settings.RATE_LIMIT_STORAGE_URI or "").strip() or (settings.REDIS_URL or "").strip()
    if not uri or not uri.startswith(("redis://", "rediss://", "unix://")):
        return "memory://"
    try:
        import redis  # type: ignore

        client = redis.Redis.from_url(uri, socket_connect_timeout=1, socket_timeout=1)
        client.ping()
        client.close()
        return uri
    except Exception as exc:  # pragma: no cover - depends on deployment
        logger.warning(
            "Rate limiter: Redis at %s unavailable (%s); falling back to in-memory "
            "storage. Limits are per-worker until Redis is reachable.",
            uri,
            exc,
        )
        return "memory://"


limiter = Limiter(
    key_func=client_ip_key,
    storage_uri=_resolve_storage_uri(),
    enabled=settings.RATE_LIMIT_ENABLED,
    # Must stay False. slowapi's header injection requires every decorated endpoint to
    # return a starlette Response; ours return Pydantic models, so with this enabled
    # slowapi raises and the endpoint 500s. That silently broke forgot-password,
    # reset-password, change-password and request-email-change — i.e. the entire
    # account-recovery path — while login survived only because it happens to take a
    # `response: Response` parameter. Rate limiting itself is unaffected: 429s are
    # still returned, we simply don't advertise the limits in response headers.
    headers_enabled=False,
    # No default limit: rate limiting is opt-in per auth route so that
    # high-volume authenticated endpoints are never throttled.
    default_limits=[],
)


async def rate_limit_exceeded_handler(request: Request, exc: RateLimitExceeded) -> JSONResponse:
    retry_after = getattr(exc, "retry_after", None)
    headers = {"Retry-After": str(retry_after)} if retry_after else {}
    return JSONResponse(
        status_code=429,
        content={
            "detail": (
                "Too many attempts. Please wait a moment and try again. "
                f"(limit: {exc.detail})"
            )
        },
        headers=headers,
    )


# --- Per-account login throttling -------------------------------------------
#
# Keying login attempts on client IP alone is wrong for this deployment: an
# institution typically NATs its entire campus behind one public IP, so an
# IP-keyed limit low enough to stop brute force would lock out every legitimate
# student after the first few logins. The IP limit is therefore set generously
# (it exists to stop a single host spraying many accounts), and the real
# brute-force defence is this per-account limiter.
#
# Only FAILED attempts consume budget, so a user logging in normally is never
# penalised, and a successful login clears the account's counter.

from limits import parse as _parse_limit
from limits.storage import storage_from_string as _storage_from_string
from limits.strategies import MovingWindowRateLimiter as _MovingWindowRateLimiter

_account_storage = _storage_from_string(_resolve_storage_uri())
_account_limiter = _MovingWindowRateLimiter(_account_storage)


def _account_limit():
    return _parse_limit(settings.RATE_LIMIT_LOGIN_PER_ACCOUNT)


def _account_key(email: str) -> str:
    return f"login-account:{(email or '').strip().lower()}"


def account_login_allowed(email: str) -> bool:
    """True if this account still has failed-login budget remaining."""
    if not settings.RATE_LIMIT_ENABLED:
        return True
    try:
        return _account_limiter.test(_account_limit(), _account_key(email))
    except Exception:
        logger.exception("Per-account login limiter unavailable; allowing attempt")
        return True


def record_failed_login(email: str) -> None:
    """Consume one unit of the account's failed-login budget."""
    if not settings.RATE_LIMIT_ENABLED:
        return
    try:
        _account_limiter.hit(_account_limit(), _account_key(email))
    except Exception:
        logger.exception("Per-account login limiter unavailable; failure not recorded")


def clear_failed_logins(email: str) -> None:
    """Reset the account's counter after a successful authentication."""
    if not settings.RATE_LIMIT_ENABLED:
        return
    try:
        _account_storage.clear(_account_key(email))
    except Exception:
        logger.debug("Could not clear login counter for account", exc_info=True)
