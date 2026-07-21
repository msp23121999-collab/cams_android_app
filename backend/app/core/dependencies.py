from collections.abc import AsyncGenerator, Callable
from typing import Any

from fastapi import Depends, HTTPException, Request, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.security import decode_token
from app.db.models.user import User, UserRole
from app.db.session import get_db
from app.services.auth_service import AuthService

bearer_scheme = HTTPBearer(auto_error=False)


async def get_db_session() -> AsyncGenerator[AsyncSession, None]:
    async for session in get_db():
        yield session


async def get_current_user(
    request: Request,
    credentials: HTTPAuthorizationCredentials | None = Depends(bearer_scheme),
    db: AsyncSession = Depends(get_db_session),
) -> User:
    token = credentials.credentials if credentials else request.cookies.get("access_token")
    if not token:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Not authenticated")

    try:
        payload = decode_token(token)
    except ValueError as exc:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid token") from exc
    if payload.get("type") != "access":
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid token type")

    user_id = payload.get("sub")
    if not user_id:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid token payload")

    service = AuthService(db)
    user = await service.get_user_by_id(user_id)
    if not user:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Inactive user")
    if not user.is_active:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Inactive user")

    return user


def role_required(allowed_roles: list[UserRole]) -> Callable[[User], Any]:
    async def _role_dependency(current_user: User = Depends(get_current_user)) -> User:
        allowed_values = [r.value if hasattr(r, 'value') else str(r) for r in allowed_roles]
        user_role_val = current_user.role.value if hasattr(current_user.role, 'value') else str(current_user.role)

        # SUPER_ADMIN is the platform superuser and intentionally satisfies every check.
        if user_role_val == "SUPER_ADMIN":
            return current_user

        # PRINCIPAL has institution-wide oversight of teaching staff, so it satisfies
        # FACULTY and HOD checks. It is deliberately NOT a wildcard.
        #
        # This used to return early for PRINCIPAL on *every* guard, before the allow-list
        # was consulted. The comment said "HOD and Faculty endpoints", but the effect was
        # that a principal passed all 219 guards that exclude the role — including 33
        # ADMIN/SUPER_ADMIN-only operations (backups, academic-year setup, user
        # management) and the STUDENT/PARENT endpoints. Every principal-facing route
        # declares PRINCIPAL in its own guard, so scoping the inheritance costs nothing.
        if user_role_val == "PRINCIPAL" and (
            "FACULTY" in allowed_values or "HOD" in allowed_values
        ):
            return current_user

        if user_role_val not in allowed_values:
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Insufficient permissions")
        return current_user

    return _role_dependency

