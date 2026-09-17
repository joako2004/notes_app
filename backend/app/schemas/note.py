from pydantic import BaseModel, Field
from typing import Optional
from datetime import datetime
from uuid import UUID

class NoteCreate(BaseModel):
    title: str = Field(default="Untitled", max_length=200)
    content: Optional[str] = None
    is_pinned: Optional[bool] = False

class NoteResponse(NoteCreate):
    id: UUID
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True