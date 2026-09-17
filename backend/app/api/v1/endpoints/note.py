from fastapi import APIRouter, Depends, HTTPException, status
from sqlmodel import Session, select
from db import engine, Note
from schemas import NoteCreate, NoteResponse

router = APIRouter(prefix="/notes", tags=["notes"])

@router.get("/", response_model=list[NoteResponse])
def list_notes(session: Session = Depends(get_session)):
    statement = select(Note)
    notes = session.exec(statement).all()
    return notes

@router.post("/", response_model=NoteResponse)
def create_note(note: NoteCreate, session: Session = Depends(get_session)):
    db_note = Note(**note.model_dump())
    session.add(db_note)
    session.commit()
    session.refresh(db_note)
    return db_note

@router.get("/{note_id}", response_model=NoteResponse)
def get_note(note_id: UUID, session: Session = Depends(get_session)):
    note = session.get(Note, note_id)
    if not note:
        raise HTTPException(status_code=404, detail="Nota no encontrada")
    return note

@router.put("/{note_id}", response_model=NoteResponse)
def update_note(note_id: UUID, note: NoteCreate, session: Session = Depends(get_session)):
    db_note = session.get(Note, note_id)
    if not db_note:
        raise HTTPException(status_code=404, detail="Nota no encontrada")
    db_note.title = note.title
    db_note.content = note.content
    db_note.is_pinned = note.is_pinned
    session.add(db_note)
    session.commit()
    session.refresh(db_note)
    return db_note

@router.delete("/{note_id}")
def delete_note(note_id: UUID, session: Session = Depends(get_session)):
    db_note = session.get(Note, note_id)
    if not db_note:
        raise HTTPException(status_code=404, detail="Nota no encontrada")
    session.delete(db_note)
    session.commit()
    return {"ok": True}