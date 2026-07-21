"""Clubs endpoints — real SQLAlchemy-backed storage (previously flat JSON files)."""
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select

from app.core.dependencies import get_db_session, get_current_user
from app.db.models.user import User
from app.db.models.club import Club, ClubMembership, ClubAnnouncement
from app.schemas.club import ClubResponse, ClubAnnouncementCreate, ClubAnnouncementResponse

router = APIRouter()


async def _resolve_president_name(db: AsyncSession, club_id: str) -> str | None:
    res = await db.execute(
        select(User.full_name)
        .join(ClubMembership, ClubMembership.user_id == User.id)
        .where(ClubMembership.club_id == club_id, ClubMembership.role == "President", ClubMembership.is_deleted.is_(False))
    )
    return res.scalar_one_or_none()


@router.get("", response_model=list[ClubResponse])
async def get_clubs(
    skip: int = 0,
    limit: int = 20,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session),
):
    """List all clubs, annotated with the caller's own membership role if any."""
    clubs_res = await db.execute(select(Club).where(Club.is_deleted.is_(False)).order_by(Club.name))
    clubs = clubs_res.scalars().all()[skip: skip + limit]

    memberships_res = await db.execute(
        select(ClubMembership).where(
            ClubMembership.user_id == current_user.id,
            ClubMembership.is_deleted.is_(False),
        )
    )
    role_by_club = {m.club_id: m.role for m in memberships_res.scalars().all()}

    presidents_res = await db.execute(
        select(ClubMembership, User)
        .join(User, ClubMembership.user_id == User.id)
        .where(ClubMembership.role == "President", ClubMembership.is_deleted.is_(False))
    )
    president_by_club = {m.club_id: u.full_name for m, u in presidents_res.all()}

    return [
        ClubResponse(
            id=c.id,
            name=c.name,
            description=c.description,
            category=c.category,
            member_count=c.member_count,
            current_user_role=role_by_club.get(c.id),
            president_name=president_by_club.get(c.id),
        )
        for c in clubs
    ]


@router.post("/{club_id}/join", response_model=ClubResponse)
async def join_club(
    club_id: str,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session),
):
    club_res = await db.execute(select(Club).where(Club.id == club_id, Club.is_deleted.is_(False)))
    club = club_res.scalar_one_or_none()
    if not club:
        raise HTTPException(status_code=404, detail="Club not found")

    existing_res = await db.execute(
        select(ClubMembership).where(
            ClubMembership.club_id == club_id,
            ClubMembership.user_id == current_user.id,
            ClubMembership.is_deleted.is_(False),
        )
    )
    if existing_res.scalar_one_or_none():
        raise HTTPException(status_code=400, detail="You are already a member of this club")

    membership = ClubMembership(club_id=club_id, user_id=current_user.id, role="Member")
    db.add(membership)
    club.member_count = (club.member_count or 0) + 1
    await db.commit()
    await db.refresh(club)

    return ClubResponse(
        id=club.id,
        name=club.name,
        description=club.description,
        category=club.category,
        member_count=club.member_count,
        current_user_role="Member",
        president_name=await _resolve_president_name(db, club.id),
    )


@router.post("/{club_id}/leave", response_model=ClubResponse)
async def leave_club(
    club_id: str,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session),
):
    club_res = await db.execute(select(Club).where(Club.id == club_id, Club.is_deleted.is_(False)))
    club = club_res.scalar_one_or_none()
    if not club:
        raise HTTPException(status_code=404, detail="Club not found")

    membership_res = await db.execute(
        select(ClubMembership).where(
            ClubMembership.club_id == club_id,
            ClubMembership.user_id == current_user.id,
            ClubMembership.is_deleted.is_(False),
        )
    )
    membership = membership_res.scalar_one_or_none()
    if not membership:
        raise HTTPException(status_code=400, detail="You are not a member of this club")

    # Hard-delete the membership row (rather than soft-delete) so a later
    # rejoin doesn't collide with the (club_id, user_id) unique constraint.
    await db.delete(membership)
    club.member_count = max(0, (club.member_count or 0) - 1)
    await db.commit()
    await db.refresh(club)

    return ClubResponse(
        id=club.id,
        name=club.name,
        description=club.description,
        category=club.category,
        member_count=club.member_count,
        current_user_role=None,
        president_name=await _resolve_president_name(db, club.id),
    )


@router.get("/announcements", response_model=list[ClubAnnouncementResponse])
async def get_club_announcements(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session),
):
    """Recent announcements from clubs the caller is a member of."""
    memberships_res = await db.execute(
        select(ClubMembership.club_id).where(
            ClubMembership.user_id == current_user.id,
            ClubMembership.is_deleted.is_(False),
        )
    )
    club_ids = [row[0] for row in memberships_res.all()]
    if not club_ids:
        return []

    rows = await db.execute(
        select(ClubAnnouncement, Club.name, User.full_name)
        .join(Club, ClubAnnouncement.club_id == Club.id)
        .join(User, ClubAnnouncement.posted_by == User.id)
        .where(ClubAnnouncement.club_id.in_(club_ids), ClubAnnouncement.is_deleted.is_(False))
        .order_by(ClubAnnouncement.created_at.desc())
        .limit(20)
    )
    return [
        ClubAnnouncementResponse(
            id=a.id, club_id=a.club_id, club_name=club_name, title=a.title,
            is_urgent=a.is_urgent, posted_by_name=poster_name, created_at=a.created_at
        )
        for a, club_name, poster_name in rows.all()
    ]


@router.post("/{club_id}/announcements", response_model=ClubAnnouncementResponse)
async def create_club_announcement(
    club_id: str,
    payload: ClubAnnouncementCreate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session),
):
    """Only club officers (any role other than plain Member) may post announcements."""
    club_res = await db.execute(select(Club).where(Club.id == club_id, Club.is_deleted.is_(False)))
    club = club_res.scalar_one_or_none()
    if not club:
        raise HTTPException(status_code=404, detail="Club not found")

    membership_res = await db.execute(
        select(ClubMembership).where(
            ClubMembership.club_id == club_id,
            ClubMembership.user_id == current_user.id,
            ClubMembership.is_deleted.is_(False),
        )
    )
    membership = membership_res.scalar_one_or_none()
    if not membership or membership.role == "Member":
        raise HTTPException(status_code=403, detail="Only club officers can post announcements")

    announcement = ClubAnnouncement(
        club_id=club_id, posted_by=current_user.id, title=payload.title, is_urgent=payload.is_urgent
    )
    db.add(announcement)
    await db.commit()
    await db.refresh(announcement)

    return ClubAnnouncementResponse(
        id=announcement.id, club_id=club.id, club_name=club.name, title=announcement.title,
        is_urgent=announcement.is_urgent, posted_by_name=current_user.full_name,
        created_at=announcement.created_at
    )
