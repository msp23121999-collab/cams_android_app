# 13. Deployment Guide

## Prerequisites
- Docker Engine & Docker Compose.
- PostgreSQL 13+.

## Production Deployment
1. Provision a server instance.
2. Clone repository (Backend only required for server).
3. Configure `.env` with production secrets.
4. `docker compose up -d db`
5. `docker compose run --rm backend alembic upgrade head`
6. `docker compose up -d backend`

## Rollback Strategy
- Use `alembic downgrade -1` for DB regressions.
- Use Docker image tagging for app version rollbacks.
