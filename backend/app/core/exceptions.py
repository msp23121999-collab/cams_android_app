import logging
import uuid

from fastapi import FastAPI, HTTPException, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse

logger = logging.getLogger(__name__)


def register_exception_handlers(app: FastAPI) -> None:
    @app.exception_handler(HTTPException)
    async def http_exception_handler(_: Request, exc: HTTPException) -> JSONResponse:
        return JSONResponse(
            status_code=exc.status_code,
            content={
                "detail": exc.detail,
                "code": "HTTP_ERROR",
                "status": exc.status_code,
            },
        )

    @app.exception_handler(RequestValidationError)
    async def validation_exception_handler(_: Request, exc: RequestValidationError) -> JSONResponse:
        def sanitize(v):
            if isinstance(v, bytes):
                return v.decode("utf-8", errors="replace")
            elif isinstance(v, dict):
                return {k: sanitize(val) for k, val in v.items()}
            elif isinstance(v, list):
                return [sanitize(val) for val in v]
            elif isinstance(v, tuple):
                return tuple(sanitize(val) for val in v)
            return v

        return JSONResponse(
            status_code=422,
            content={
                "detail": "Validation failed",
                "code": "VALIDATION_ERROR",
                "status": 422,
                "errors": sanitize(exc.errors()),
            },
        )

    @app.exception_handler(Exception)
    async def unhandled_exception_handler(_: Request, exc: Exception) -> JSONResponse:
        error_id = str(uuid.uuid4())
        logger.exception("Unhandled exception %s", error_id, exc_info=exc)
        return JSONResponse(
            status_code=500,
            content={
                "detail": "Internal server error",
                "code": "INTERNAL_ERROR",
                "status": 500,
                "error_id": error_id,
            },
        )
