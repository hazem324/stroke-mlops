from typing import Literal

from pydantic import BaseModel


class HealthResponse(BaseModel):
    status: Literal["healthy", "unhealthy"]
    service: str
    version: str
    environment: str
    detail: str | None = None