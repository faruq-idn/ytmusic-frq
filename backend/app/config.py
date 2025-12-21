"""
Application configuration using Pydantic Settings.
Loads from environment variables and .env file.
"""
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    """Application settings loaded from environment."""
    
    # Server
    host: str = "0.0.0.0"
    port: int = 8000
    debug: bool = True
    
    # CORS
    cors_origins: str = "*"
    
    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"


# Singleton settings instance
settings = Settings()
