"""Minimal SMTP email-sending helper.

Follows the same "graceful degradation" pattern already established for
Razorpay in app/core/config.py: SMTP_* settings default to empty strings, so
this module never raises — callers should treat a `False` return as a soft
failure (log it, don't 500 the request) rather than a crash.
"""
import logging
import smtplib
from email.mime.text import MIMEText

from app.core.config import settings

logger = logging.getLogger(__name__)


def send_email(to_email: str, subject: str, body: str) -> bool:
    """Send a plain-text email via SMTP.

    Returns True if the message was handed off to the SMTP server
    successfully, False otherwise (e.g. SMTP not configured, connection
    failure, auth failure). Never raises.
    """
    if not settings.SMTP_HOST:
        logger.warning(
            "send_email: SMTP_HOST is not configured; skipping send to %s (subject=%r)",
            to_email,
            subject,
        )
        return False

    message = MIMEText(body, "plain", "utf-8")
    message["Subject"] = subject
    message["From"] = settings.SMTP_FROM_EMAIL or settings.SMTP_USER or "no-reply@cams.local"
    message["To"] = to_email

    try:
        with smtplib.SMTP(settings.SMTP_HOST, settings.SMTP_PORT, timeout=10) as server:
            if settings.SMTP_USE_TLS:
                server.starttls()
            if settings.SMTP_USER and settings.SMTP_PASSWORD:
                server.login(settings.SMTP_USER, settings.SMTP_PASSWORD)
            server.sendmail(message["From"], [to_email], message.as_string())
        return True
    except Exception:
        logger.exception("send_email: failed to send email to %s", to_email)
        return False
