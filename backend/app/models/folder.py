from sqlmodel import SQLModel, Field, Column, ARRAY, String, create_engine, select
import uuid
from typing import Optional, List
from datetime import datetime


class Folder(SQLModel, table=True):
    id: str = Field(default_factory=lambda: str(uuid.uuid4()), primary_key=True)
    name: str = Field(default="", max_length=200)
    color: str = Field(default="#3B82F6", max_length=7)
    parent_id: Optional[str] = Field(default=None, foreign_key="folder.id")
    device_id: Optional[str] = Field(default=None)
    created_at: datetime = Field(default_factory=datetime.utcnow, sa_column_kwargs={"server_default": "now()"})
    updated_at: datetime = Field(default_factory=datetime.utcnow, sa_column_kwargs={"server_default": "now()"})
    deleted_at: Optional[datetime] = Field(default=None, sa_column_kwargs={"server_default": None})