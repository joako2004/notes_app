"""initial schema

Revision ID: d21853629aa3
Revises: 
Create Date: 2026-09-17 15:24:50.712574

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa
import sqlalchemy.sql.functions as sa_functions


# revision identifiers, used by Alembic.
revision: str = 'd21853629aa3'
down_revision: Union[str, Sequence[str], None] = None
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.create_table(
        "note",
        sa.Column("id", sa.VARCHAR(36), primary_key=True, nullable=False),
        sa.Column("title", sa.VARCHAR(200), nullable=False),
        sa.Column("content", sa.TEXT(), nullable=True),
        sa.Column("is_pinned", sa.BOOLEAN(), nullable=False, server_default=sa.text('false')),
        sa.Column("tag_ids", sa.ARRAY(sa.VARCHAR()), nullable=True),
        sa.Column("deleted_at", sa.TIMESTAMP(timezone=True), nullable=True),
        sa.Column("device_id", sa.VARCHAR(36), nullable=True),
        sa.Column("created_at", sa.TIMESTAMP(timezone=True), nullable=False, server_default=sa.text('now()')),
        sa.Column("updated_at", sa.TIMESTAMP(timezone=True), nullable=False, server_default=sa.text('now()')),
    )
    op.create_table(
        "folder",
        sa.Column("id", sa.VARCHAR(36), primary_key=True, nullable=False),
        sa.Column("name", sa.VARCHAR(200), nullable=False),
        sa.Column("color", sa.VARCHAR(7), nullable=False, server_default="#3B82F6"),
        sa.Column("parent_id", sa.VARCHAR(36), nullable=True),
        sa.Column("device_id", sa.VARCHAR(36), nullable=True),
        sa.Column("created_at", sa.TIMESTAMP(timezone=True), nullable=False, server_default=sa.text('now()')),
        sa.Column("updated_at", sa.TIMESTAMP(timezone=True), nullable=False, server_default=sa.text('now()')),
        sa.Column("deleted_at", sa.TIMESTAMP(timezone=True), nullable=True),
        sa.ForeignKeyConstraint(["parent_id"], ["folder.id"], name="fk_folder_parent"),
    )
    op.create_table(
        "task",
        sa.Column("id", sa.VARCHAR(36), primary_key=True, nullable=False),
        sa.Column("title", sa.VARCHAR(200), nullable=False),
        sa.Column("content", sa.TEXT(), nullable=True),
        sa.Column("status", sa.VARCHAR(50), nullable=False, server_default="pending"),
        sa.Column("priority", sa.VARCHAR(50), nullable=False, server_default="medium"),
        sa.Column("due_date", sa.TIMESTAMP(timezone=True), nullable=True),
        sa.Column("checklist", sa.ARRAY(sa.VARCHAR()), nullable=True),
        sa.Column("is_pinned", sa.BOOLEAN(), nullable=False, server_default=sa.text('false')),
        sa.Column("deleted_at", sa.TIMESTAMP(timezone=True), nullable=True),
        sa.Column("device_id", sa.VARCHAR(36), nullable=True),
        sa.Column("folder_id", sa.VARCHAR(36), nullable=True),
        sa.Column("created_at", sa.TIMESTAMP(timezone=True), nullable=False, server_default=sa.text('now()')),
        sa.Column("updated_at", sa.TIMESTAMP(timezone=True), nullable=False, server_default=sa.text('now()')),
        sa.ForeignKeyConstraint(["folder_id"], ["folder.id"], name="fk_task_folder"),
    )
    op.create_table(
        "project",
        sa.Column("id", sa.VARCHAR(36), primary_key=True, nullable=False),
        sa.Column("name", sa.VARCHAR(200), nullable=False),
        sa.Column("color", sa.VARCHAR(7), nullable=False, server_default="#10B981"),
        sa.Column("order", sa.INTEGER(), nullable=False, server_default=sa.text('0')),
        sa.Column("archived", sa.BOOLEAN(), nullable=False, server_default=sa.text('false')),
        sa.Column("deleted_at", sa.TIMESTAMP(timezone=True), nullable=True),
        sa.Column("device_id", sa.VARCHAR(36), nullable=True),
        sa.Column("created_at", sa.TIMESTAMP(timezone=True), nullable=False, server_default=sa.text('now()')),
        sa.Column("updated_at", sa.TIMESTAMP(timezone=True), nullable=False, server_default=sa.text('now()')),
    )
    op.create_table(
        "habit",
        sa.Column("id", sa.VARCHAR(36), primary_key=True, nullable=False),
        sa.Column("name", sa.VARCHAR(200), nullable=False),
        sa.Column("periodicity", sa.VARCHAR(20), nullable=False, server_default="daily"),
        sa.Column("streak", sa.INTEGER(), nullable=False, server_default=sa.text('0')),
        sa.Column("last_completed", sa.TIMESTAMP(timezone=True), nullable=True),
        sa.Column("deleted_at", sa.TIMESTAMP(timezone=True), nullable=True),
        sa.Column("device_id", sa.VARCHAR(36), nullable=True),
        sa.Column("created_at", sa.TIMESTAMP(timezone=True), nullable=False, server_default=sa.text('now()')),
        sa.Column("updated_at", sa.TIMESTAMP(timezone=True), nullable=False, server_default=sa.text('now()')),
    )
    op.create_table(
        "habit_record",
        sa.Column("id", sa.VARCHAR(36), primary_key=True, nullable=False),
        sa.Column("habit_id", sa.VARCHAR(36), nullable=False),
        sa.Column("device_id", sa.VARCHAR(36), nullable=True),
        sa.Column("record_date", sa.TIMESTAMP(timezone=True), nullable=False, server_default=sa.text('now()')),
        sa.Column("completed", sa.BOOLEAN(), nullable=False, server_default=sa.text('true')),
        sa.Column("updated_at", sa.TIMESTAMP(timezone=True), nullable=False, server_default=sa.text('now()')),
        sa.Column("deleted_at", sa.TIMESTAMP(timezone=True), nullable=True),
    )
    op.create_table(
        "link",
        sa.Column("id", sa.VARCHAR(36), primary_key=True, nullable=False),
        sa.Column("note_id", sa.VARCHAR(36), nullable=True),
        sa.Column("task_id", sa.VARCHAR(36), nullable=True),
        sa.Column("habit_id", sa.VARCHAR(36), nullable=True),
        sa.Column("link_type", sa.VARCHAR(50), nullable=False, server_default="reference"),
        sa.Column("device_id", sa.VARCHAR(36), nullable=True),
        sa.Column("updated_at", sa.TIMESTAMP(timezone=True), nullable=False, server_default=sa.text('now()')),
        sa.Column("deleted_at", sa.TIMESTAMP(timezone=True), nullable=True),
        sa.Column("created_at", sa.TIMESTAMP(timezone=True), nullable=False, server_default=sa.text('now()')),
        sa.ForeignKeyConstraint(["note_id"], ["note.id"], name="fk_link_note"),
        sa.ForeignKeyConstraint(["task_id"], ["task.id"], name="fk_link_task"),
        sa.ForeignKeyConstraint(["habit_id"], ["habit.id"], name="fk_link_habit"),
    )
    op.create_table(
        "tag",
        sa.Column("id", sa.VARCHAR(36), primary_key=True, nullable=False),
        sa.Column("name", sa.VARCHAR(100), nullable=False),
        sa.Column("color", sa.VARCHAR(7), nullable=False, server_default="#6B7280"),
        sa.Column("used_on", sa.VARCHAR(20), nullable=False, server_default="note"),
        sa.Column("deleted_at", sa.TIMESTAMP(timezone=True), nullable=True),
        sa.Column("device_id", sa.VARCHAR(36), nullable=True),
        sa.Column("created_at", sa.TIMESTAMP(timezone=True), nullable=False, server_default=sa.text('now()')),
        sa.Column("updated_at", sa.TIMESTAMP(timezone=True), nullable=False, server_default=sa.text('now()')),
    )

    # Create trigger function and trigger for automatic updated_at updates
    op.execute("""
        CREATE OR REPLACE FUNCTION update_updated_at_column()
        RETURNS TRIGGER AS $$
        BEGIN
            NEW.updated_at = now();
            RETURN NEW;
        END;
        $$ LANGUAGE plpgsql;
    """)

    op.execute("""
        CREATE TRIGGER update_updated_at
        BEFORE UPDATE ON note
        FOR EACH ROW
        EXECUTE FUNCTION update_updated_at_column();
    """)
    op.execute("""
        CREATE TRIGGER update_updated_at
        BEFORE UPDATE ON folder
        FOR EACH ROW
        EXECUTE FUNCTION update_updated_at_column();
    """)
    op.execute("""
        CREATE TRIGGER update_updated_at
        BEFORE UPDATE ON task
        FOR EACH ROW
        EXECUTE FUNCTION update_updated_at_column();
    """)
    op.execute("""
        CREATE TRIGGER update_updated_at
        BEFORE UPDATE ON project
        FOR EACH ROW
        EXECUTE FUNCTION update_updated_at_column();
    """)
    op.execute("""
        CREATE TRIGGER update_updated_at
        BEFORE UPDATE ON habit
        FOR EACH ROW
        EXECUTE FUNCTION update_updated_at_column();
    """)
    op.execute("""
        CREATE TRIGGER update_updated_at
        BEFORE UPDATE ON habit_record
        FOR EACH ROW
        EXECUTE FUNCTION update_updated_at_column();
    """)
    op.execute("""
        CREATE TRIGGER update_updated_at
        BEFORE UPDATE ON link
        FOR EACH ROW
        EXECUTE FUNCTION update_updated_at_column();
    """)
    op.execute("""
        CREATE TRIGGER update_updated_at
        BEFORE UPDATE ON tag
        FOR EACH ROW
        EXECUTE FUNCTION update_updated_at_column();
    """)


def downgrade() -> None:
    # Drop all triggers first
    op.execute("DROP TRIGGER IF EXISTS update_updated_at ON tag;")
    op.execute("DROP TRIGGER IF EXISTS update_updated_at ON link;")
    op.execute("DROP TRIGGER IF EXISTS update_updated_at ON habit_record;")
    op.execute("DROP TRIGGER IF EXISTS update_updated_at ON habit;")
    op.execute("DROP TRIGGER IF EXISTS update_updated_at ON project;")
    op.execute("DROP TRIGGER IF EXISTS update_updated_at ON task;")
    op.execute("DROP TRIGGER IF EXISTS update_updated_at ON folder;")
    op.execute("DROP TRIGGER IF EXISTS update_updated_at ON note;")
    
    op.execute("DROP FUNCTION update_updated_at_column();")
    
    op.drop_table("tag")
    op.drop_table("link")
    op.drop_table("habit_record")
    op.drop_table("habit")
    op.drop_table("project")
    op.drop_table("task")
    op.drop_table("folder")
    op.drop_table("note")
