# Setup inicial (repositorio, Flutter/Android, backend, .env)
Fecha: 2026-09-17
Fase: 0 — Setup inicial

## Qué se hizo

### Infraestructura y entorno
- Se creó el repositorio del backend en git.
- Se instaló el SDK de Flutter (vía snap) y Android Studio (vía snap) en la máquina de desarrollo (Ubuntu 24.04.5 LTS).
- Se instalaron las dependencias del sistema necesarias para Flutter/Android (`curl`, `git`, `unzip`, `xz-utils`, `libglu1-mesa`).
- Se instalaron los Android SDK Command-line Tools desde el SDK Manager de Android Studio (pestaña "SDK Tools").
- Se verificó el estado del toolchain con `flutter doctor -v`: Flutter, Android toolchain, Chrome y Connected device (desktop) en verde. Licencias de Android ya aceptadas (`All Android licenses accepted.`).
- Se creó un proyecto nuevo de Supabase (independiente de otros proyectos existentes en la misma cuenta), eligiendo Supabase por sobre un VPS propio para la base de datos.
- Se obtuvo la connection string de Postgres desde Supabase (Connect → Connection string → Direct connection).
- Se armó el `.env` del backend con las cuatro variables necesarias para arrancar: `DATABASE_URL`, `API_AUTH_TOKEN`, `ENVIRONMENT`, `CORS_ORIGINS`.
- Se generó el `.gitignore` del repo del backend (Python/FastAPI), incluyendo la exclusión de `.env`.

### Scaffold del proyecto Flutter (estructura feature-aware)
- Estructura de carpetas por feature (notes, habits, training) con separación presentation/data/core
- `lib/main.dart` - Punto de entrada
- `lib/core/theme/app_theme.dart` - Tema visual con Material3 (light/dark)
- `lib/core/init/dependency_injection.dart` - Inyección de dependencias con Supabase
- `lib/core/constants/app_constants.dart` - Constantes de Supabase (anon key + URL)
- `lib/features/notes/presentation/notes_page.dart` - Página principal de notas
- `lib/features/habits/presentation/habits_page.dart` - Página de hábitos
- `lib/features/training/presentation/training_page.dart` - Página de entrenamiento
- `pubspec.yaml` - Dependencias (flutter_bloc, equatable, supabase_flutter, freezed, json_annotation)
- `analysis_options.yaml` - Configuración de linting con rules personalizadas

### Scaffold del backend FastAPI
- `backend/app/main.py` - Entry point de FastAPI con CORS, health check y creación de tablas
- `backend/app/core/config.py` - Configuración usando pydantic-settings con variables .env
- `backend/app/core/db.py` - Motor SQLAlchemy + dependency `get_session`
- `backend/app/models/note.py` - Modelo SQLModel para notas
- `backend/app/schemas/note.py` - Esquemas Pydantic (Create/Response)
- `backend/app/api/v1/endpoints/note.py` - Endpoints CRUD completos (list, create, get, update, delete)
- `backend/app/api/v1/router.py` - Router principal que incluye todos los endpoints
- `backend/requirements.txt` - Dependencias Python (fastapi, uvicorn, pydantic, sqlmodel)
- `backend/pyproject.toml` - Configuración del proyecto con metadatos y scripts
- `backend/Dockerfile` - Imagen Docker para despliegue (python:3.12-slim, expone puerto 8000)

## Decisiones tomadas y por qué

### Infraestructura inicial
- **Backend: Supabase en vez de VPS propio.** Para un proyecto personal, minimizar el tiempo dedicado a administración de infraestructura (instalación de Postgres, firewall, backups manuales) pesó más que el control total que da un VPS. FastAPI se conecta a la DB de Supabase con una connection string estándar de Postgres, sin usar el resto del ecosistema de Supabase (auth, storage) por ahora.
- **Auth de la API: token fijo** (`API_AUTH_TOKEN`), generado con `secrets.token_urlsafe(32)` desde Python. Alcanza para uso personal de un solo usuario; no se implementó JWT ni sistema de usuarios múltiples.
- **Modo de conexión a Supabase: Direct connection** (puerto 5432) en vez de un connection pooler, por tratarse de un backend que corre siempre prendido (no serverless/edge). Si en el futuro el backend pasara a un entorno serverless, convendría reevaluar y usar el Session/Transaction pooler en su lugar.
- **Contraseña de la base de datos con carácter especial (`@`)**: se resolvió codificándola como `%40` en la connection string, en vez de regenerar una password sin caracteres especiales, ya que el `.env` ya estaba definido con esa contraseña.

### Flutter scaffold decisions
- **Estado global con GetIt/ServiceLocator**: Elección por simplicidad en proyecto personal vs. Riverpod/BLoC completo. Fácil de reemplazar si se necesita escalar.
- **freezed_annotation + json_annotation**: Para generación código tipo-safe de modelos y eliminación de boilerplate.
- **MaterialDesign3** con tema claro/oscuro por defecto.
- **Routing/navegación**: Pendiente de definir (GoRoute vs. named routes) en Fase 1.

### FastAPI scaffold decisions
- **SQLModel sobre SQLAlchemy puro**: Menor boilerplate, Pydantic integration nativo, y migration-friendly.
- **Connection string directo (puerto 5432)** en lugar de pooler: El backend corre siempre prendido (no serverless), así que connection directa es más eficiente.
- **Variables de entorno via .env con pydantic-settings**: En lugar de os.getenv directo, para validación y defaults tipo-safe.
- **Docker imagen multi-stage no definida**: Usar imagen `python:3.12-slim` simple por ser proyecto personal; optimizar later con multi-stage si se necesita.

### Arquitectura General
- **Características aisladas**: Cada feature (notes, habits, training) tiene su propio directorio con presentation, data y lógica propia. Ningún módulo importa lógica interna de otro.
- **Backend y frontend comparten DATABASE_URL y .env structure**: El Flutter app lee `AppConstants` y el backend lee `.env` con los mismos valores de Supabase.

## Archivos creados/modificados

### Estructura Flutter
- `.env` — variables `DATABASE_URL` (connection string de Supabase, con la password URL-encoded), `API_AUTH_TOKEN`, `ENVIRONMENT=development`, `CORS_ORIGINS=*`.
- `.gitignore` — excluye `.env`/`.env.*` (con excepción de `.env.example`), artefactos de Python (`__pycache__/`, `.venv/`, `build/`, `*.egg-info/`, etc.), cachés de testing/linting (`.pytest_cache/`, `.mypy_cache/`, `.ruff_cache/`, `.coverage`), bases de datos locales (`*.db`, `*.sqlite3`) y archivos de editor/OS (`.vscode/`, `.idea/`, `.DS_Store`).
- `lib/main.dart` - Punto de entrada
- `lib/core/theme/app_theme.dart` - Tema visual
- `lib/core/init/dependency_injection.dart` - Inyección de dependencias
- `lib/core/constants/app_constants.dart` - Constantes
- `lib/features/notes/presentation/notes_page.dart` - Home de notas
- `lib/features/habits/presentation/habits_page.dart` - Home de hábitos
- `lib/features/training/presentation/training_page.dart` - Home de entrenamiento
- `pubspec.yaml` - Dependencias del proyecto
- `analysis_options.yaml` - Configuración de linting

### Estructura FastAPI backend
- `backend/app/main.py` - Entry point
- `backend/app/core/config.py` - Configuración
- `backend/app/core/db.py` - Motor y sesión DB
- `backend/app/models/note.py` - Modelo SQLModel
- `backend/app/schemas/note.py` - Esquemas Pydantic
- `backend/app/api/v1/endpoints/note.py` - Endpoints CRUD
- `backend/app/api/v1/router.py` - Router principal
- `backend/requirements.txt` - Dependencias
- `backend/pyproject.toml` - Configuración proyecto
- `backend/Dockerfile` - Despliegue Docker

### Documentación
- `docs/2026-09-17_phase-0-setup.md` - Este archivo resumen

## Cómo probarlo

### Flutter
1. `flutter doctor -v` → Android toolchain, Flutter, Chrome y Connected device en verde.
2. Conectar el dispositivo Android real por USB y correr `flutter devices` → debería listarlo junto al dispositivo desktop.
3. `flutter pub get` - obtener dependencias
4. Ejecutar en dispositivo/emulador: `flutter run`

### FastAPI
1. `cd backend && pip install -r requirements.txt`
2. `uvicorn app.main:app --reload`
3. Health check: `curl http://localhost:8000/healthz` → `{"status":"ok"}`
4. API notes: `curl http://localhost:8000/notes/`

### Verificación general
1. `cat .gitignore | grep .env` → debe listar `.env` (confirma que el archivo de secretos está excluido).
2. `git log --all --full-history -- .env` → no debe devolver nada (confirma que el `.env` nunca se subió al historial del repo).
3. Verificar estructura de carpetas feature-aware en `lib/features/`.

## Pendientes / follow-ups para el programador

- Generar un `.gitignore` separado para el repo de la app Flutter cuando se create (cubre `build/`, `.dart_tool/`, etc. — distinto al de Python).
- Evaluar si conviene dejar un `.env.example` en el repo (con los nombres de variables sin valores reales) para referencia futura — el `.gitignore` ya lo contempla con la excepción `!.env.example`.
- Evaluar reemplazo de GetIt por Riverpod o BLoC completo en Fase 1
- Implementar migration system (Alembic) para el backend
- Definir estrategia de navegación/router en Flutter
- Agregar autenticación real (actualmente token fijo en .env)
- Crear tests unitarios para endpoints FastAPI