#!/bin/sh
set -e

export PYTHONPATH=/app

# alembic upgrade head
python init_db.py
python -m scripts.seed
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
