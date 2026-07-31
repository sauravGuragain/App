"""Push notifications via Firebase Cloud Messaging.

Firebase is imported lazily and only when FCM_CREDENTIALS_FILE is set, so the
app (and the test suite) run fine with no Firebase configured — sends simply
become no-ops. Every send is best-effort and never raises into the request.
"""
import logging

from sqlalchemy import select
from sqlalchemy.orm import Session

from app.core.config import settings
from app.models.device_token import DeviceToken

logger = logging.getLogger(__name__)

_app = None


def _messaging():
    """Return the firebase messaging module, or None if FCM isn't configured."""
    global _app
    if not settings.FCM_CREDENTIALS_FILE:
        return None
    try:
        import firebase_admin
        from firebase_admin import credentials, messaging

        if _app is None:
            cred = credentials.Certificate(settings.FCM_CREDENTIALS_FILE)
            _app = firebase_admin.initialize_app(cred)
        return messaging
    except Exception as exc:  # pragma: no cover - depends on external creds
        logger.warning("FCM unavailable: %s", exc)
        return None


def send_to_user(
    db: Session, user_id: int, title: str, body: str, data: dict | None = None
) -> None:
    messaging = _messaging()
    if messaging is None:
        return  # FCM not configured — no-op
    tokens = db.scalars(
        select(DeviceToken.token).where(DeviceToken.user_id == user_id)
    ).all()
    for token in tokens:
        try:
            messaging.send(
                messaging.Message(
                    notification=messaging.Notification(title=title, body=body),
                    data={k: str(v) for k, v in (data or {}).items()},
                    token=token,
                )
            )
        except Exception as exc:  # pragma: no cover
            logger.warning("FCM send failed for token: %s", exc)
