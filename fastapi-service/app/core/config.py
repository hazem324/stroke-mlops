from functools import lru_cache

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    # Application
    app_name: str = "stroke-segmentation-api"
    app_version: str = "1.0.0"
    app_description: str = "REST API for ischemic stroke lesion segmentation from DWI MRI"

    # Server
    host: str = "0.0.0.0"
    port: int = 8080

    # Environment
    environment: str = "development"
    debug: bool = False

    # ML Model
    model_name: str = "stroke-unet3d"
    model_path: str = "models/unet3d_dwi_best.pth"
    device: str = "cpu"
    prediction_threshold: float = 0.5

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        case_sensitive=False,
        extra="ignore",
    )


@lru_cache
def get_settings() -> Settings:
    return Settings()