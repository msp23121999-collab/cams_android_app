from fastapi import APIRouter, Depends, HTTPException
from datetime import datetime, timezone
from pydantic import BaseModel
from app.db.models.device_token import DeviceToken
from app.services import push_service
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, update

from app.db.models.communication import Notification
from app.db.models.user import User
from app.core.dependencies import get_current_user, get_db_session

router = APIRouter()


@router.get("/")
async def get_notifications(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session)
):
    """Get all notifications for the current user, sorted by date (newest first)."""
    result = await db.execute(
        select(Notification)
        .where(
            Notification.user_id == current_user.id,
            Notification.is_deleted.is_(False)
        )
        .order_by(Notification.created_at.desc())
    )
    notifications = result.scalars().all()
    return [
        {
            "id": n.id,
            "type": n.type,
            "message": n.message,
            "is_read": n.is_read,
            "sent_via": n.sent_via,
            "created_at": n.created_at.isoformat() if n.created_at else None,
        }
        for n in notifications
    ]


@router.get("/unread-count")
async def get_unread_count(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session)
):
    """Get unread notification count for the current user."""
    result = await db.execute(
        select(Notification)
        .where(
            Notification.user_id == current_user.id,
            Notification.is_read.is_(False),
            Notification.is_deleted.is_(False)
        )
    )
    count = len(result.scalars().all())
    return {"unread_count": count}


# NOTE: these must stay ABOVE the parameterised routes below. FastAPI matches in
# registration order, so a single-segment "/{notification_id}" route registered
# first will swallow "/device-token" and treat it as an id — which is exactly what
# happened: DELETE /device-token returned "Notification not found".
# ── Device tokens (push notifications) ───────────────────────────────────────

class DeviceTokenRequest(BaseModel):
    token: str
    platform: str = "android"


@router.post("/device-token")
async def register_device_token(
    payload: DeviceTokenRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session),
):
    """Register this installation's FCM token against the signed-in user.

    Safe to call repeatedly — the app registers on every launch and whenever FCM
    rotates the token. The token is unique, not the user: a device that changes
    hands is re-pointed at whoever is signed in now, so the previous user stops
    receiving that device's notifications.
    """
    token = (payload.token or "").strip()
    if not token:
        raise HTTPException(status_code=400, detail="Token is required")

    res = await db.execute(select(DeviceToken).where(DeviceToken.token == token))
    existing = res.scalar_one_or_none()

    if existing:
        existing.user_id = current_user.id
        existing.platform = payload.platform or "android"
        existing.is_deleted = False
        existing.last_seen_at = datetime.now(timezone.utc)
    else:
        db.add(DeviceToken(
            user_id=current_user.id,
            token=token,
            platform=payload.platform or "android",
            last_seen_at=datetime.now(timezone.utc),
        ))

    await db.commit()
    return {"status": "registered", "push_enabled": push_service.is_enabled()}


@router.delete("/device-token")
async def unregister_device_token(
    payload: DeviceTokenRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session),
):
    """Detach this device's token. Called on sign-out.

    Without this, the next person to sign in on a shared device would keep receiving
    the previous user's notifications until the token happened to rotate.
    Scoped to the caller so one user cannot unregister another's device.
    """
    token = (payload.token or "").strip()
    res = await db.execute(
        select(DeviceToken).where(
            DeviceToken.token == token,
            DeviceToken.user_id == current_user.id,
        )
    )
    row = res.scalar_one_or_none()
    if row:
        row.is_deleted = True
        await db.commit()
    # Idempotent: an already-removed or unknown token is not an error.
    return {"status": "unregistered"}


@router.post("/{notification_id}/read")
async def mark_notification_read(
    notification_id: str,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session)
):
    """Mark a single notification as read."""
    result = await db.execute(
        select(Notification).where(
            Notification.id == notification_id,
            Notification.user_id == current_user.id
        )
    )
    notif = result.scalars().first()
    if not notif:
        raise HTTPException(status_code=404, detail="Notification not found")
    notif.is_read = True
    await db.commit()
    return {"message": "Notification marked as read"}


@router.post("/read-all")
async def mark_all_notifications_read(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session)
):
    """Mark all notifications for the current user as read."""
    await db.execute(
        update(Notification)
        .where(
            Notification.user_id == current_user.id,
            Notification.is_read.is_(False),
            Notification.is_deleted.is_(False)
        )
        .values(is_read=True)
    )
    await db.commit()
    return {"message": "All notifications marked as read"}


@router.delete("/{notification_id}")
async def delete_notification(
    notification_id: str,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session)
):
    """Soft-delete a notification."""
    result = await db.execute(
        select(Notification).where(
            Notification.id == notification_id,
            Notification.user_id == current_user.id
        )
    )
    notif = result.scalars().first()
    if not notif:
        raise HTTPException(status_code=404, detail="Notification not found")
    notif.is_deleted = True
    await db.commit()
    return {"message": "Notification deleted"}


@router.delete("/delete-all/read")
async def delete_all_read_notifications(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session)
):
    """Delete all read notifications for the current user."""
    await db.execute(
        update(Notification)
        .where(
            Notification.user_id == current_user.id,
            Notification.is_read.is_(True),
            Notification.is_deleted.is_(False)
        )
        .values(is_deleted=True)
    )
    await db.commit()
    return {"message": "All read notifications deleted"}
