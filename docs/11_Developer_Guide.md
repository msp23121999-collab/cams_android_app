# 11. Developer Guide

## Workspace Setup
1. **Android:** Open `/app` in Android Studio. Ensure `local.properties` contains SDK path. Sync Gradle.
2. **Backend:** CD into `/backend`. Create `.venv`. `pip install -r requirements.txt`. 

## Coding Standards
- **Kotlin:** Follow standard ktlint formatting. Use ViewModels for all state.
- **Python:** PEP8 compliant. Use Pydantic for ALL incoming/outgoing payload validation.

## Deployment / Run
- `uvicorn app.main:app --reload`
- Execute Compose via Android Studio.

## Environment Variables
- `DATABASE_URL`: PostgreSQL connection string.
- `JWT_SECRET_KEY`: High entropy string for signing tokens.
