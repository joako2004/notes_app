from sqlmodel import SQLModel, Field, Column, ARRAY, String, create_engine, select
import uuid
from typing import Optional, List
from datetime import datetime


class Task(SQLModel, table=True):
    id: str = Field(default_factory=lambda: str(uuid.uuid4()), primary_key=True)
    title: str = Field(default="Untitled", max_length=200)
    content: Optional[str] = None
    status: str = Field(default="pending", max_length=50)
    priority: str = Field(default="medium", max_length=50)
    due_date: Optional[datetime] = None
    checklist: Optional[List[str]] = Field(default=None, sa_column=Column(ARRAY(String)))
    is_pinned: Optional[bool] = False
    deleted_at: Optional[datetime] = Field(default=None, sa_column_kwargs={"server_default": None})
    device_id: Optional[str] = Field(default=None)
    folder_id: Optional[str] = Field(default=None, foreign_key="folder.id")
    created_at: datetime = Field(default_factory=datetime.utcnow, sa_column_kwargs={"server_default": "now()"})
    updated_at: datetime = Field(default_factory=datetime.utcnow, sa_column_kwargs={"server_default": "now()"})