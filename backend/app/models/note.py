from sqlmodel import SQLModel, Field, Column, Type, create_engine, select
from sqlmodel.types.array import ARRAY
from typing import Optional, List
import uuid
from datetime import datetime

class Note(SQLModel, table=True):
    id: uuid = Field(default_factory=uuid.uuid4, primary_key=True)
    title: str = Field(default="Untitled", max_length=200)
    content: Optional[str] = None
    is_pinned: Optional[bool] = False
    created_at: datetime = Field(default_factory=datetime.utcnow, sa_column_kwargs={"server_default": "now()"})
    updated_at: datetime = Field(default_factory=datetime.utcnow, sa_column_kwargs={"server_default": "now(), on update now()"})