from sqlmodel import SQLModel, Field, Column, ARRAY, String, create_engine, select
from typing import Optional, List
import uuid
from datetime import datetime


class Tag(SQLModel, table=True):
    id: str = Field(default_factory=lambda: str(uuid.uuid4()), primary_key=True)
    name: str = Field(default="", max_length=100)
    color: str = Field(default="#6B7280", max_length=7)
    used_on: str = Field(default="note", max_length=20)
    deleted_at: Optional[datetime] = Field(default=None, sa_column_kwargs={"server_default": None})
    device_id: Optional[str] = Field(default=None)
    created_at: datetime = Field(default_factory=datetime.utcnow, sa_column_kwargs={"server_default": "now()"})
    updated_at: datetime = Field(default_factory=datetime.utcnow, sa_column_kwargs={"server_default": "now()"})