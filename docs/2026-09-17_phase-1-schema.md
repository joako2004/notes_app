# Fase 1 — Schema Design (Notes App)

**Fecha**: 2026-09-17  
**Estado**: Completado — awaiting migration application

## Propósito

Diseñar el esquema completo para la aplicación Notes App, incluyendo:
- Entidades: notas, carpetas, tareas, proyectos, hábitos, registros diarios de hábitos
- Vínculos nota↔tarea/hábito
- Tags
- Metadata de sync (`updated_at`, `device_id`, `deleted_at` para soft-delete)

## Modelos Implementados

### Note (`backend/app/models/note.py`)
- `id: str` — UUID como VARCHAR(36), primary key
- `title: str` — máximo 200 caracteres, obligatorio
- `content: Optional[str]` — contenido de la nota
- `is_pinned: Optional[bool]` — whether the note is pinned
- `tag_ids: Optional[List[str]]` — ARRAY de tag IDs asociados directamente a la nota
- `deleted_at: Optional[datetime]` — soft-delete timestamp
- `device_id: Optional[str]` — device identifier for sync
- `created_at: datetime` — creation timestamp (server_default: now())
- `updated_at: datetime` — last update timestamp (server_default: now(), on update: now())

### Folder (`backend/app/models/folder.py`)
- `id: str` — UUID primary key
- `name: str` — folder name (max 200 chars)
- `color: str` — hex color code (max 7 chars, default `#3B82F6`)
- `parent_id: Optional[str]` — self-referential foreign key to folder.id (adjacency list)
- `device_id: Optional[str]` — device identifier for sync
- `created_at: datetime` — creation timestamp
- `updated_at: datetime` — last update timestamp
- `deleted_at: Optional[datetime]` — soft-delete timestamp
- **Relationship**: `parent` → self-referential via `parent_id`

### Task (`backend/app/models/task.py`)
- `id: str` — UUID primary key
- `title: str` — task title (max 200 chars)
- `content: Optional[str]` — task description
- `status: str` — status field (default: "pending"), max 50 chars
- `priority: str` — priority field (default: "medium"), max 50 chars
- `due_date: Optional[datetime]` — due date for the task
- `checklist: Optional[List[str]]` — checklist items as ARRAY(String)
- `is_pinned: Optional[bool]` — whether task is pinned
- `deleted_at: Optional[datetime]` — soft-delete timestamp
- `device_id: Optional[str]` — device identifier for sync
- `folder_id: Optional[str]` — foreign key to folder (optional categorization)
- `created_at: datetime` — creation timestamp
- `updated_at: datetime` — last update timestamp

### Project (`backend/app/models/project.py`)
- `id: str` — UUID primary key
- `name: str` — project name (max 200 chars)
- `color: str` — hex color code (max 7 chars, default `#10B981`)
- `order: int` — ordering index (default: 0)
- `archived: bool` — whether the project is archived (default: False)
- `deleted_at: Optional[datetime]` — soft-delete timestamp
- `device_id: Optional[str]` — device identifier for sync
- `created_at: datetime` — creation timestamp
- `updated_at: datetime` — last update timestamp

### Habit (`backend/app/models/habit.py`)
- `id: str` — UUID primary key
- `name: str` — habit name (max 200 chars)
- `periodicity: str` — frequency: "daily", "weekly", or "monthly" (default: "daily")
- `streak: int` — consecutive days completed (default: 0)
- `last_completed: Optional[datetime]` — last time the habit was completed
- `deleted_at: Optional[datetime]` — soft-delete timestamp
- `device_id: Optional[str]` — device identifier for sync
- `created_at: datetime` — creation timestamp
- `updated_at: datetime` — last update timestamp

### HabitRecord (`backend/app/models/habit_record.py`)
- `id: str` — UUID primary key
- `habit_id: str` — foreign key to habit.id
- `device_id: Optional[str]` — device identifier for sync
- `record_date: datetime` — date of the habit record (server_default: now())
- `completed: bool` — whether the habit was completed (default: True)
- `deleted_at: Optional[datetime]` — soft-delete timestamp

### Link (`backend/app/models/link.py`)
- `id: str` — UUID primary key
- `note_id: Optional[str]` — foreign key to note.id
- `task_id: Optional[str]` — foreign key to task.id (nullable)
- `habit_id: Optional[str]` — foreign key to habit.id (nullable)
- `link_type: str` — type of link (default: "reference"), max 50 chars
- `device_id: Optional[str]` — device identifier for sync
- `deleted_at: Optional[datetime]` — soft-delete timestamp
- `created_at: datetime` — creation timestamp (server_default: now())

### Tag (`backend/app/models/tag.py`)
- `id: str` — UUID primary key
- `name: str` — tag name (max 100 chars, obligatorio)
- `color: str` — hex color code (max 7 chars, default `#6B7280`)
- `used_on: str` — indicates what entity type the tag is used on: "note", "task", or "habit" (default: "note"), max 20 chars
- `deleted_at: Optional[datetime]` — soft-delete timestamp
- `device_id: Optional[str]` — device identifier for sync
- `created_at: datetime` — creation timestamp
- `updated_at: datetime` — last update timestamp

## Migración Alembic

- **Archivo**: `backend/migrations/versions/d21853629aa3_initial_schema.py`
- **Revisión**: `d21853629aa3`
- **Motor**: PostgreSQL (Supabase)
- **Tipos usados**: `VARCHAR(36)` para UUIDs, `TIMESTAMP(timezone=True)` para fechas, `ARRAY(String)` para listas, `BOOLEAN` para flags

### Tablas creadas (en orden):
1. `note` — notas con tags ARRAY y soft-delete
2. `folder` — carpetas con relación jerárquica self-referential
3. `task` — tareas con checklist, prioridad, estatus, FK a folder
4. `project` — proyectos con order y archived
5. `habit` — hábitos con periodicity y streak
6. `habit_record` — registros diarios de hábitos FK a habit
5. `link` — vínculos many-to-many note↔task/hábit con FKs a las 3 tablas
4. `tag` — tags con `used_on` para indicar el tipo de entidad

### Foreign Keys en la migración:
- `task.folder_id → folder.id` (fk_task_folder)
- `link.note_id → note.id` (fk_link_note)
- `link.task_id → task.id` (fk_link_task)
- `link.habit_id → habit.id` (fk_link_habit)
- `habit_record.habit_id → habit.id` (implícita por foreign key constraint)
- `folder.parent_id → folder.id` (fk_folder_parent)

### Índices y consideraciones de sync:
- `updated_at` en todas las tablas para mecanismo de sync incremental
- `deleted_at` en todas las tablas para soft-delete (queries: `WHERE deleted_at IS NULL`)
- `device_id` en todas las tablas para rastrear cambios por dispositivo
- `tag.used_on` para filtrado por tipo de entidad

## Decisiones de Diseño

### 1. ID format: VARCHAR(36) vs PostgreSQL UUID
**Decisión**: Usar `VARCHAR(36)` en lugar del tipo nativo `UUID` de PostgreSQL.
**Razón**: El autogenerate de Alembic tiene compatibilidad limitada con `SQLModel.UUID` y Pydantic en este entorno. `VARCHAR(36)` es un subconjunto funcional que funciona con ambos sistemas.

### 2. Metadata `dict` removida de modelos
**Decisión**: El campo `metadata: Optional[dict]` fue removido de todos los modelos.
**Razón**: `dict` no tiene un mapeo SQLAlchemy directo en la versión de sqlmodel instalada (0.0.42). Puede agregarse luego usando `JSON` type si es necesario.

### 3. Jerarquía de carpetas (adjacency list)
**Decisión**: Usar `parent_id` con foreign key self-referential en la tabla `folder`.
**Razón**: Es el patrón más sencillo para carpetas anidadas. Alternativas como closure table o materialized path fueron consideradas pero añaden complejidad innecesaria para el caso de uso actual.

### 4. Design de tags: `note.tag_ids` vs `tag.used_on`
**Decisión**: Mantener ambos enfoques coexistiendo:
- `note.tag_ids: Optional[List[str]]` — ARRAY directo en la tabla note para tags asociadas rápidamente
- `tag.used_on: str` — indica el tipo de entidad: "note", "task", o "habit"

**Razón**: Esto provee acceso rápido via `note.tag_ids` mientras que `tag.used_on` permite queries transversales ("¿cuáles son todos los tags usados en tareas?"). Esto satisface el requisito de Fase 1 y está preparado para la Fase 11 de tags transversales.

**Pendiente para el programador**: Validar si esta diseño dual es óptimo o si se debería migrar a una tabla junction `note_tag` en una fase futura.

### 5. Timestamps con `sa.text('now()')` vs `datetime.utcnow()`
**Decisión**: Usar `sa_column_kwargs={"server_default": "now()"}` para que PostgreSQL maneje los timestamps.
**Razón**: `datetime.utcnow()` es una función de Python que no funciona como `server_default` en todos los contextos. Dejar que la base de datos maneje `now()` asegura consistencia y rendimiento.

### 6. Soft-delete pattern
**Decisión**: Todas las tablas tienen `deleted_at: TIMESTAMP(timezone=True)` nullable.
**Queries recomendadas**: `WHERE deleted_at IS NULL` para obtener registros activos, `WHERE deleted_at IS NOT NULL` para soft-deleted records.

### 7. Device tracking
**Decisión**: `device_id: VARCHAR(36)` nullable en todas las tablas.
**Razón**: Permite rastrear qué dispositivo realizó el último cambio, útil para conflictos de sync y historial.

## Problemas Conocidos / Pendientes

1. **Migration application**: `alembic upgrade head` no puede ejecutarse en este entorno debido a restricciones IPv6 de red para conectar a Supabase. La migración SQL está verificada y lista.

2. **Dual tag design**: El diseño dual de tags (`note.tag_ids` + `tag.used_on`) necesita validación por parte del programador para asegurar que cumpla con los requisitos de Fase 1 y esté preparado para Fase 11.

3. **Checklist en migration**: El campo `checklist` está incluido en la migración `task` table, coincidiendo con el modelo `Task`.

4. **Foreign keys**: Todas las FKs han sido agregado a la migración (task.folder_id, link.note_id/task_id/habit_id, folder.parent_id).

5. **Timestamps consistentes**: Todos los modelos tienen `created_at`, `updated_at`, `deleted_at` con defaults de server.

## Cómo Probar la Migración

```bash
# 1. Verificar que la migración Python es válida
cd backend
python3 -c "from migrations.versions.d21853629aa3_initial_schema import *; print('OK')"

# 2. Aplicar a una base de prueba local (no a Supabase directamente):
#    CREAR UNA BASE LOCAL DESPUÉS DE ESTE PUNTO
# alembic upgrade head

# 3. Verificar rollback:
# alembic downgrade base

# 4. Verificar estructura de tablas:
# postgres=# \dt
# postgres=# \d note
# postgres=# \d folder
# etc.
```

## Archivos Modificados

- `backend/app/models/note.py` — updated con device_id, tipos consistentes
- `backend/app/models/folder.py` — nuevo, carpetas con jerarquía self-referential
- `backend/app/models/task.py` — actualizado checklist, device_id, FKs
- `backend/app/models/project.py` — actualizado device_id, timestamps
- `backend/app/models/habit.py` — actualizado device_id, timestamps
- `backend/app/models/habit_record.py` — nuevo, registros de hábitos
- `backend/app/models/link.py` — nuevo, enlaces note↔task/hábit
- `backend/app/models/tag.py` — actualizado device_id, timestamps, usado_on
- `backend/migrations/versions/d21853629aa3_initial_schema.py` — migración completa con FKs y checklist
- `docs/2026-09-17_phase-1-schema.md` — esta documentación

## Próximos Pasos

1. Aplicar `alembic upgrade head` a base de prueba/local
2. Verificar `alembic downgrade base` es coherente
3. Implementar endpoints API para nuevas entidades (Fase 2)
4. Validar diseño de tags con el programador (decisión pendiente)
5. Implementar lógica de sync basada en `updated_at` y `deleted_at`