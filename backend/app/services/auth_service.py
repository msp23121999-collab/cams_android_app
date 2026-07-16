from fastapi import HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.security import create_access_token, create_refresh_token, verify_password
from app.db.models.user import User
from app.db.repositories.auth_repository import AuthRepository


class AuthService:
    def __init__(self, db: AsyncSession) -> None:
        self.repo = AuthRepository(db)

    async def authenticate(self, email: str, password: str) -> tuple[User, str, str]:
        user = await self.repo.get_user_by_email(email)
        if not user or not verify_password(password, user.hashed_password):
            raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid credentials")

        access_token = create_access_token(subject=str(user.id), role=user.role.value)
        refresh_token = create_refresh_token(subject=str(user.id), role=user.role.value)
        return user, access_token, refresh_token

    async def get_user_by_id(self, user_id: str) -> User | None:
        return await self.repo.get_user_by_id(user_id)
