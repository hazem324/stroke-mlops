from fastapi import FastAPI

from app.core.config import get_settings
from app.routes.health import router as health_router
from app.routes.prediction import router as prediction_router 
from app.routes.download import router as download_router


settings = get_settings() 

app = FastAPI(
    title=settings.app_name,
    description=settings.app_description,
    version=settings.app_version,
    debug=settings.debug,
)


app.include_router(health_router)

app.include_router(prediction_router)

app.include_router(download_router)

