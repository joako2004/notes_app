from typing import Generator
from sqlmodel import create_engine, Session, SQLModel
import os

DATABASE_URL = os.getenv("DATABASE_URL", "postgresql://postgres:Joako12112004%40@db.aohxseiknpqagsypspwv.supabase.co:5432/postgres")

engine = create_engine(DATABASE_URL, echo=False)

def get_session() -> Generator[Session, None, None]:
    with Session(engine) as session:
        yield session

def create_db_and_tables():
    SQLModel.metadata.create_all(engine)