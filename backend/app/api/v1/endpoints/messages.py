"""1:1 direct messaging between staff (faculty/HOD/principal/admin) users."""
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy import select, update, or_, and_, func
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.dependencies import get_current_user, get_db_session, role_required
from app.db.models.communication import Message
from app.db.models.user import User, UserRole
from app.schemas.message import (
    MessageResponse,
    SendMessageRequest,
    ConversationResponse,
    ContactResponse,
)

router = APIRouter()

STAFF_ROLES = [UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.SUPER_ADMIN]


@router.get("/contacts", response_model=list[ContactResponse])
async def list_contacts(
    current_user: User = Depends(role_required(STAFF_ROLES)),
    db: AsyncSession = Depends(get_db_session),
):
    """Other staff members available to start a conversation with."""
    rows = await db.execute(
        select(User).where(
            User.role.in_(STAFF_ROLES), User.id != current_user.id, User.is_deleted.is_(False)
        ).order_by(User.full_name)
    )
    return [ContactResponse(id=u.id, full_name=u.full_name, role=u.role.value) for u in rows.scalars().all()]


@router.get("/conversations", response_model=list[ConversationResponse])
async def list_conversations(
    current_user: User = Depends(role_required(STAFF_ROLES)),
    db: AsyncSession = Depends(get_db_session),
):
    """One row per counterpart, with the last message and unread count."""
    rows = await db.execute(
        select(Message).where(
            or_(Message.sender_id == current_user.id, Message.receiver_id == current_user.id),
            Message.is_deleted.is_(False),
        ).order_by(Message.created_at.desc())
    )
    messages = rows.scalars().all()

    by_counterpart: dict[str, Message] = {}
    unread_counts: dict[str, int] = {}
    for m in messages:
        counterpart_id = m.receiver_id if m.sender_id == current_user.id else m.sender_id
        if counterpart_id not in by_counterpart:
            by_counterpart[counterpart_id] = m
        if m.receiver_id == current_user.id and not m.is_read:
            unread_counts[counterpart_id] = unread_counts.get(counterpart_id, 0) + 1

    if not by_counterpart:
        return []

    users_q = await db.execute(select(User).where(User.id.in_(list(by_counterpart.keys()))))
    users_by_id = {u.id: u for u in users_q.scalars().all()}

    result = []
    for counterpart_id, last_msg in by_counterpart.items():
        user = users_by_id.get(counterpart_id)
        if not user:
            continue
        result.append(
            ConversationResponse(
                user_id=counterpart_id,
                user_name=user.full_name,
                user_role=user.role.value,
                last_message=last_msg.body,
                last_message_at=last_msg.created_at,
                unread_count=unread_counts.get(counterpart_id, 0),
            )
        )
    result.sort(key=lambda c: c.last_message_at, reverse=True)
    return result


@router.get("/thread/{user_id}", response_model=list[MessageResponse])
async def get_thread(
    user_id: str,
    current_user: User = Depends(role_required(STAFF_ROLES)),
    db: AsyncSession = Depends(get_db_session),
):
    rows = await db.execute(
        select(Message).where(
            or_(
                and_(Message.sender_id == current_user.id, Message.receiver_id == user_id),
                and_(Message.sender_id == user_id, Message.receiver_id == current_user.id),
            ),
            Message.is_deleted.is_(False),
        ).order_by(Message.created_at.asc())
    )
    return list(rows.scalars().all())


@router.post("/send", response_model=MessageResponse)
async def send_message(
    payload: SendMessageRequest,
    current_user: User = Depends(role_required(STAFF_ROLES)),
    db: AsyncSession = Depends(get_db_session),
):
    if not payload.body.strip():
        raise HTTPException(status_code=400, detail="Message body cannot be empty")

    receiver = await db.get(User, payload.receiver_id)
    if not receiver or receiver.is_deleted:
        raise HTTPException(status_code=404, detail="Recipient not found")

    message = Message(sender_id=current_user.id, receiver_id=payload.receiver_id, body=payload.body.strip())
    db.add(message)
    await db.commit()
    await db.refresh(message)
    return message


@router.post("/read/{user_id}")
async def mark_thread_read(
    user_id: str,
    current_user: User = Depends(role_required(STAFF_ROLES)),
    db: AsyncSession = Depends(get_db_session),
):
    await db.execute(
        update(Message)
        .where(Message.sender_id == user_id, Message.receiver_id == current_user.id, Message.is_read.is_(False))
        .values(is_read=True)
    )
    await db.commit()
    return {"detail": "Thread marked as read"}
