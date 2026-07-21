from pydantic import BaseModel, EmailStr

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
    email_notifications_enabled: bool = True


class NotificationPreferencesRequest(BaseModel):
    email_notifications_enabled: bool


class ChangePasswordRequest(BaseModel):
    current_password: str
    new_password: str


class MessageResponse(BaseModel):
    detail: str


class RequestEmailChangeRequest(BaseModel):
    new_email: EmailStr
    # Re-authentication. Without this, a stolen session token is enough to move the
    # account to an attacker-controlled address and then take it over completely via
    # the password-reset flow, locking the real owner out.
    current_password: str


class ConfirmEmailChangeRequest(BaseModel):
    token: str


class ForgotPasswordRequest(BaseModel):
    email: EmailStr


class ResetPasswordRequest(BaseModel):
    token: str
    new_password: str
