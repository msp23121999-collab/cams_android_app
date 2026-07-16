from celery import Celery

from app.core.config import settings

celery = Celery("cams", broker=settings.REDIS_URL, backend=settings.REDIS_URL)
celery.conf.task_default_queue = "cams"
