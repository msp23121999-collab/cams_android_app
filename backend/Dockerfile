# CAMS backend
FROM python:3.11-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1

WORKDIR /app

# Build deps for asyncpg/psycopg2, removed after install to keep the image small
RUN apt-get update && apt-get install -y --no-install-recommends \
        build-essential libpq-dev curl \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

# Uploaded documents are served through an authenticated endpoint, but the
# directory must exist and be writable by the app process.
RUN mkdir -p app/static/uploads

EXPOSE 8000

HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
    CMD curl -fsS http://localhost:8000/api/v1/test_connection || exit 1

# Migrations are NOT run here. Schema changes are a deploy step, not a
# container-start side effect: with several replicas they would race, and a
# failed migration would take every replica down. Run this once per deploy:
#   docker compose run --rm backend alembic upgrade head
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000", \
     "--proxy-headers", "--forwarded-allow-ips", "*"]
