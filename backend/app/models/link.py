from sqlmodel import SQLModel, Field, Column, ARRAY, String, create_engine, select
from typing import Optional, List
import uuid
from datetime import datetime


class Link(SQLModel, table=True):
    id: str = Field(default_factory=lambda: str(uuid.uuid4()), primary_key=True)
    note_id: str = Field(default=None, foreign_key="note.id")
    task_id: Optional[str] = Field(default=None, foreign_key="task.id")
    habit_id: Optional[str] = Field(default=None, foreign_key="habit.id")
    link_type: str = Field(default="reference", max_length=50)
    device_id: Optional[str] = Field(default=None)
    updated_at: datetime = Field(default_factory=datetime.utcnow, sa_column_kwargs={"server_default": "now()"})
    deleted_at: Optional[datetime] = Field(default=None, sa_column_kwargs={"server_default": None})
    created_at: datetime = Field(default_factory=datetime.utcnow, sa_column_kwargs={"server_default": "now()"})