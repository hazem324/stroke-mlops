from fastapi import APIRouter, HTTPException, status

from app.core.config import get_settings
from app.schemas.health import HealthResponse


router = APIRouter(
    prefix="/health",
    tags=["Health"],
)

settings = get_settings()


@router.get(
    "",
    response_model=HealthResponse,
    status_code=status.HTTP_200_OK,
    summary="Check service health",
    description="Check whether the Stroke MRI Segmentation API is operational.",
)
def health_check() -> HealthResponse:
    try:
        return HealthResponse(
            status="healthy",
            service=settings.app_name,
            version=settings.app_version,
            environment=settings.environment,
        )

    except Exception:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Service health check failed",
        )