# 10. Admin Guide

## System Administration

### Configuration
Managed heavily via environment variables in the backend (`.env`) and `.properties` in Android.

### User Management
- Accessible via the **Admin Portal** -> **User Directory**.
- Create users, assign roles, and force password resets.

### Permissions
Permissions are strictly defined in `app/core/security.py` and are applied via FastAPI dependency injection (`Depends(get_current_active_user)`).

### Maintenance
Database migrations must be run sequentially using `alembic upgrade head`. Backups are configured via the Backup Service.
