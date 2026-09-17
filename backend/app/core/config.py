from pydantic import BaseModel, Field
from typing import Optional
import os
from datetime import datetime

class Settings(BaseModel):
    environment: str = Field(default="development", alias="ENVIRONMENT")
    database_url: str = Field(alias="DATABASE_URL")
    api_auth_token: str = Field(alias="API_AUTH_TOKEN")
    cors_origins: list = Field(alias="CORS_ORIGINS", default=["*"])
    
    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"

settings = Settings(_env_file=".env", _env_file_encoding="utf-8")