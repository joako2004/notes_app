from sqlmodel import SQLModel, Field, Column, ARRAY, String, create_engine, select
from typing import Optional, List
import uuid
from datetime import datetime


class Habit(SQLModel, table=True):
    id: str = Field(default_factory=lambda: str(uuid.uuid4()), primary_key=True)
    name: str = Field(default="", max_length=200)
    periodicity: str = Field(default="daily", max_length=20)
    streak: int = Field(default=0)
    last_completed: Optional[datetime] = None
    deleted_at: Optional[datetime] = Field(default=None, sa_column_kwargs={"server_default": None})
    device_id: Optional[str] = Field(default=None)
    created_at: datetime = Field(default_factory=datetime.utcnow, sa_column_kwargs={"server_default": "now()"})
    updated_at: datetime = Field(default_factory=datetime.utcnow, sa_column_kwargs={"server_default": "now()"})