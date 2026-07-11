from app.workers.celery_app import celery


@celery.task(name="notifications.send_email")
def send_email_notification(recipient: str, subject: str, body: str) -> dict[str, str]:
    return {"recipient": recipient, "subject": subject, "status": "queued"}
