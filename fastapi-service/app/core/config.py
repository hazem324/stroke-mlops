from functools import lru_cache

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    # Application
    app_name: str
    app_version: str
    app_description: str

    # Server
    host: str
    port: int

    # Environment
    environment: str
    debug: bool

    # ML Model
    model_name: str
    model_path: str
    device: str
    prediction_threshold: float

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        case_sensitive=False,
        extra="ignore",
    )


@lru_cache
def get_settings() -> Settings:
    return Settings()