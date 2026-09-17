from sqlmodel import SQLModel, Field, Column, ARRAY, String, create_engine, select
from typing import Optional, List
import uuid
from datetime import datetime


class HabitRecord(SQLModel, table=True):
    id: str = Field(default_factory=lambda: str(uuid.uuid4()), primary_key=True)
    habit_id: str = Field(foreign_key="habit.id")
    device_id: Optional[str] = Field(default=None)
    record_date: datetime = Field(default_factory=datetime.utcnow, sa_column_kwargs={"server_default": "now()"})
    completed: bool = Field(default=True)
    updated_at: datetime = Field(default_factory=datetime.utcnow, sa_column_kwargs={"server_default": "now()"})
    deleted_at: Optional[datetime] = Field(default=None, sa_column_kwargs={"server_default": None})