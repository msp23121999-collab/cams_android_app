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
        print("[AUTH DEBUG] No token provided")
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Not authenticated")

    try:
        payload = decode_token(token)
    except ValueError as exc:
        print(f"[AUTH DEBUG] Token decoding failed: {exc}")
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid token") from exc
    if payload.get("type") != "access":
        print(f"[AUTH DEBUG] Invalid token type: {payload.get('type')}")
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid token type")

    user_id = payload.get("sub")
    if not user_id:
        print("[AUTH DEBUG] Invalid token payload: no sub")
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid token payload")

    service = AuthService(db)
    user = await service.get_user_by_id(user_id)
    if not user:
        print(f"[AUTH DEBUG] User {user_id} not found in database")
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Inactive user")
    if not user.is_active:
        print(f"[AUTH DEBUG] User {user_id} is inactive")
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Inactive user")

    print(f"[AUTH DEBUG] Token valid for user {user.email}")
    return user


def role_required(allowed_roles: list[UserRole]) -> Callable[[User], Any]:
    async def _role_dependency(current_user: User = Depends(get_current_user)) -> User:
        allowed_values = [r.value if hasattr(r, 'value') else str(r) for r in allowed_roles]
        user_role_val = current_user.role.value if hasattr(current_user.role, 'value') else str(current_user.role)
        
        print(f"role_required debug: user={current_user.email}, role={user_role_val}")
        print(f"role_required debug: allowed_roles={allowed_values}")
        
        # Principal and Super Admin have access to HOD and Faculty endpoints
        if user_role_val == "PRINCIPAL" or user_role_val == "SUPER_ADMIN":
            return current_user
            
        if user_role_val not in allowed_values:
            print(f"role_required debug: role check failed! {user_role_val} not in {allowed_values}. Raising 403")
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Insufficient permissions")
        return current_user

    return _role_dependency

