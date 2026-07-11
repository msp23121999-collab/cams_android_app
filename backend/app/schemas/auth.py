from pydantic import BaseModel

from app.db.models.user import UserRole


class LoginRequest(BaseModel):
    email: str
    password: str


class LoginResponse(BaseModel):
    access_token: str
    role: UserRole
    subdomain_target: str
    refresh_token: str | None = None


class RefreshRequest(BaseModel):
    refresh_token: str | None = None


class RefreshResponse(BaseModel):
    access_token: str
    refresh_token: str | None = None


class UserMeResponse(BaseModel):
    id: str
    email: str
    full_name: str
    role: UserRole
    department_id: str | None = None
