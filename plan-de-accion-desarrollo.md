# Plan de acción — App de Notas + To-Do + Hábitos + Entrenamiento

> Plan operativo para ejecutar con opencode, dividido en subtareas por fase.
> Convención: **[OPENCODE]** = lo ejecuta el agente. **[PROGRAMADOR]** = lo hacés vos (configuración, servicios externos, credenciales, pruebas en dispositivo real, decisiones de negocio/diseño).
> Última actualización: 2026-09-16

---

## 0. Lineamientos generales para el agente (opencode)

Estas reglas van al `AGENTS.md` / system prompt del agente en el proyecto, para que las siga en **todas** las tareas, no solo las de este documento.

### 0.1 Protocolo de fin de tarea (obligatorio, sin excepciones)

Al terminar **cualquier** tarea marcada [OPENCODE], antes de darla por cerrada, el agente debe:

1. **Generar/actualizar documentación en `docs/`**
   - Si no existe, crear la carpeta `docs/` en la raíz del repo correspondiente (backend o app).
   - Crear un archivo `docs/<fecha>_<slug-de-la-tarea>.md` (ej: `docs/2026-09-15_modelo-de-datos.md`) con esta estructura mínima:
     ```markdown
     # <Nombre de la tarea>
     Fecha: <fecha>
     Fase: <número y nombre de fase>

     ## Qué se hizo
     ...

     ## Decisiones tomadas y por qué
     ...

     ## Archivos creados/modificados
     - ruta/archivo.ext — qué cambió

     ## Cómo probarlo
     ...

     ## Pendientes / follow-ups para el programador
     ...
     ```
   - Este .md es el que mantiene a **Claude** (o cualquier LLM que retome el proyecto en una conversación normal, sin memoria del repo) con contexto completo leyendo solo esa carpeta.

2. **Persistir en Engram** (memoria del propio agente entre sesiones)
   - Guardar un registro equivalente y resumido vía `mem_save` (o el comando CLI `engram save "<título>" "<mensaje>"` si se usa por fuera del MCP), con al menos:
     - **Título**: nombre corto de la tarea.
     - **Tipo**: `decision` / `feature` / `bugfix` / `config` según corresponda.
     - **Qué / Por qué / Dónde / Aprendido**: igual que el .md pero condensado, pensado para que el agente lo recupere rápido en la próxima sesión sin tener que releer el repo entero.
   - Esto es lo que le da continuidad al agente entre sesiones de opencode. El .md de `docs/` es para vos y para Claude; Engram es para el agente.

3. Recién después de (1) y (2), reportar la tarea como completada.

### 0.2 Reglas de trabajo del agente

- **No instala dependencias nuevas ni cambia versiones de SDK sin avisar** antes en el chat — lo anota como pendiente y pregunta.
- **No maneja credenciales reales.** Cuando una tarea requiere una clave/token/secreto, el agente deja un placeholder (`<PENDIENTE: cargar TOKEN_X en .env>`) y lo lista en "Pendientes para el programador" del .md — nunca inventa ni pide que se le pase la clave por chat para guardarla en el código.
- **No toca infraestructura de producción/despliegue** (crear proyectos en servicios cloud, configurar dominios, etc.) — eso queda para las tareas [PROGRAMADOR].
- **Todo secreto va por variables de entorno**, nunca hardcodeado, y `.env` debe estar en `.gitignore` desde la fase 0.
- Antes de marcar una tarea de código como terminada, corre lo que exista de tests/lint del proyecto. Si no hay tests para esa parte, lo anota como pendiente en el .md (no bloquea la tarea, pero queda registrado).
- Cada fase termina con un commit por tarea (no un commit gigante por fase), con mensaje descriptivo.
- **Cada módulo funcional (notas, to-do, hábitos, entrenamiento) se implementa como una feature independiente y desacoplada** (carpeta/paquete propio en el código), compartiendo únicamente la infraestructura común: DB local (SQLite), motor de sync, autenticación. Ningún módulo debe importar lógica interna de otro. Esto es un requisito de diseño explícito, pensado para poder seguir agregando módulos nuevos sin reescribir los existentes.

---

## 1. Fase 0 — Setup inicial

- [PROGRAMADOR] Crear el repositorio (o repositorios: backend / app) en git.
- [PROGRAMADOR] Instalar Flutter SDK, Android Studio (o el toolchain mínimo) en la máquina de desarrollo.
- [PROGRAMADOR] Elegir y crear la cuenta/proyecto del servicio de backend (Supabase, o VPS propio) — esto define credenciales iniciales.
- [PROGRAMADOR] Definir qué variables van en `.env` y confirmar que `.gitignore` las cubra.
- [OPENCODE] Generar el scaffold del proyecto Flutter (estructura de carpetas por feature, linting, arquitectura base).
- [OPENCODE] Generar el scaffold del backend FastAPI (estructura, `requirements.txt`/`pyproject.toml`, Dockerfile opcional).
- [OPENCODE] Crear `docs/` y el primer registro (setup del proyecto) + primer `mem_save` en Engram.

## 2. Fase 1 — Modelo de datos

- [OPENCODE] Diseñar el esquema: notas, carpetas, tareas, proyectos/listas, hábitos, registros diarios de hábitos, vínculos nota↔tarea/hábito, tags, metadata de sync (`updated_at`, `device_id`, `deleted_at` para soft-delete).
- [OPENCODE] Generar las migraciones (Alembic u ORM elegido).
- [PROGRAMADOR] Revisar y aprobar el esquema — es una decisión de negocio, no solo técnica.
- [PROGRAMADOR] Crear la base de datos real (instancia Postgres) y aplicar las migraciones ahí.
- [OPENCODE] Documentar el modelo en `docs/` + `mem_save`.

## 3. Fase 2 — Backend API

- [OPENCODE] Implementar endpoints CRUD para cada entidad.
- [OPENCODE] Implementar endpoint de sync (pull de cambios desde un timestamp/versión dados).
- [OPENCODE] Implementar autenticación simple (token fijo o JWT — alcanza para uso personal).
- [PROGRAMADOR] Generar y guardar el secreto de autenticación fuera del repo.
- [PROGRAMADOR] Desplegar el backend (elegir hosting, configurar dominio/HTTPS si aplica).
- [OPENCODE] Tests unitarios de los endpoints principales.
- [OPENCODE] Documentar la API en `docs/` + `mem_save`.

## 4. Fase 3 — App Android: To-Do

- [OPENCODE] Setup de SQLite local (sqflite/drift) con modelos espejo del esquema del backend.
- [OPENCODE] UI de lista de tareas: click (en proceso), doble click (finalizada), menú de estados al click izquierdo desde "finalizada".
- [OPENCODE] CRUD local de tareas y proyectos/listas, con metadatos (fecha creación, fecha límite opcional, enlaces opcionales).
- [PROGRAMADOR] Probar en un dispositivo Android real y dar feedback de UX (la sensibilidad del doble click, por ejemplo, se ajusta mejor probando en mano).
- [OPENCODE] Documentar + `mem_save`.

## 5. Fase 4 — Habit tracker

- [OPENCODE] Modelo local de hábitos con frecuencia configurable (diario, día por medio, días específicos).
- [OPENCODE] Lógica de reinicio lazy (comparación de fecha actual vs. `last_completed_date` al abrir la app) + cálculo de streaks.
- [OPENCODE] UI de lista diaria + calendario heatmap.
- [PROGRAMADOR] Validar el cálculo de streaks/reinicio con casos reales (ej: cerrar la app 2-3 días y verificar que recalcule bien al reabrir).
- [OPENCODE] Documentar + `mem_save`.

## 6. Fase 5 — Notas

- [OPENCODE] CRUD de notas/carpetas, editor de texto plano/markdown.
- [OPENCODE] Búsqueda por contenido (índice local).
- [OPENCODE] Vínculos nota↔tarea/hábito.
- [PROGRAMADOR] Revisar la experiencia de edición markdown (evaluar si el editor elegido es cómodo en el uso diario).
- [OPENCODE] Documentar + `mem_save`.

## 7. Fase 6 — Sync end-to-end

- [OPENCODE] Cliente de sync: pull periódico, comparación de `updated_at`, subida de cambios locales.
- [OPENCODE] UI de resolución de conflictos estilo git merge (elegir versión local/remota/manual).
- [PROGRAMADOR] Probar sync real entre Android y el backend desplegado.
- [PROGRAMADOR] Resolver acceso de red al backend si corre en infraestructura propia (VPN, IP pública, túnel, etc.) — decisión de infraestructura.
- [OPENCODE] Documentar + `mem_save`.

## 8. Fase 7 — Widget Android

- [OPENCODE] Implementar el widget (código nativo Kotlin + `home_widget` u equivalente) para marcar hábitos/tareas.
- [PROGRAMADOR] Probar el widget en dispositivo real (los widgets no se testean bien en emulador).
- [OPENCODE] Documentar + `mem_save`.

## 9. Fase 8 — App Linux (desktop)

- [OPENCODE] Adaptar layouts para pantalla grande (Flutter desktop).
- [PROGRAMADOR] Compilar y probar en la máquina Linux real.
- [PROGRAMADOR] Decidir empaquetado (AppImage/deb) si se quiere una instalación prolija — decisión de infraestructura.
- [OPENCODE] Documentar + `mem_save`.

## 10. Fase 9 — Exportación / backup

- [OPENCODE] Implementar exportación de datos a JSON y markdown.
- [PROGRAMADOR] Definir dónde se guardan los backups (carpeta local, nube personal, etc.) — decisión personal, no técnica.
- [OPENCODE] Documentar + `mem_save`.

## 11. Fase 10 — Notificaciones

- [OPENCODE] Notificaciones locales (ej. `flutter_local_notifications`) para tareas con fecha límite y recordatorios de hábitos.
- [PROGRAMADOR] Configurar y probar permisos de notificaciones en Android (el sistema operativo suele bloquearlas por default).
- [OPENCODE] Documentar + `mem_save`.

## 12. Fase 11 — Tags y búsqueda global

- [OPENCODE] Modelo de tags transversal + UI de etiquetado en notas/tareas/hábitos.
- [OPENCODE] Búsqueda global combinada (notas + tareas + hábitos).
- [OPENCODE] Documentar + `mem_save`.

## 13. Fase 12 — Módulo de Entrenamiento (registro de gimnasio)

Módulo independiente y desacoplado del resto de la app (feature propia, sin dependencias internas de notas/to-do/hábitos), pensado **mobile-first** — sin adaptación de desktop ni widget en esta etapa.

### 13.1 Modelo de datos

- [OPENCODE] Diseñar el esquema:
  - `bloque_entrenamiento` (id, nombre, fecha_inicio, `updated_at`, `deleted_at`, `device_id`)
  - `semana` (id, bloque_id, numero)
  - `dia` (id, semana_id, nombre, orden) — ej. "Día 1 · Espalda"
  - `ejercicio` (id, nombre, es_predefinido bool, `updated_at`, `deleted_at`) — **biblioteca global**, no atada a un bloque: los ejercicios que crea el usuario quedan disponibles para reusar en cualquier bloque futuro
  - `dia_ejercicio` (id, dia_id, ejercicio_id, orden, notas) — instancia de un ejercicio dentro de un día concreto; el campo `notas` es el apartado de texto libre debajo del ejercicio
  - `serie` (id, dia_ejercicio_id, orden, peso, reps, peso_previo, reps_previo)
  - Metadata de sync en todas las tablas igual que el resto del esquema (`updated_at`, `device_id`, `deleted_at`)
- [OPENCODE] Cargar un set inicial de ejercicios predefinidos (`es_predefinido = true`) como seed de la biblioteca.
- [OPENCODE] Generar las migraciones.
- [PROGRAMADOR] Revisar y aprobar el esquema.
- [OPENCODE] Documentar + `mem_save`.

### 13.2 Backend API

- [OPENCODE] Endpoints CRUD para bloque, semana, día, ejercicio (biblioteca), día-ejercicio y serie.
- [OPENCODE] Endpoint `POST /semanas/{id}/duplicar` (o equivalente) que implementa la lógica de "crear semana N+1 a partir de N":
  - Copia todos los días de la semana N (mismo nombre/orden).
  - Para cada día, copia cada `dia_ejercicio` en el mismo orden.
  - Para cada `dia_ejercicio`, copia **la cantidad de series que existan en N en el momento de la duplicación** (si se borró una serie en N antes de duplicar, no se copia a N+1).
  - Cada serie copiada setea `peso_previo`/`reps_previo` = `peso`/`reps` de la serie original en N; `peso`/`reps` quedan en blanco (null) en N+1.
  - El campo `notas` de cada `dia_ejercicio` **no** se copia automáticamente (arranca vacío en la semana nueva) — confirmar si se prefiere lo contrario más adelante.
- [OPENCODE] Extender el endpoint de sync existente para incluir las tablas de este módulo (mismo mecanismo de pull por `updated_at`, sin lógica de sync nueva).
- [OPENCODE] Tests unitarios, en particular de la lógica de duplicación de semana (casos: serie borrada, ejercicio nuevo agregado, serie nueva agregada).
- [OPENCODE] Documentar + `mem_save`.

### 13.3 App Android (mobile-first)

- [OPENCODE] Setup de tablas espejo en SQLite local para este módulo.
- [OPENCODE] Navegación: lista de bloques → semanas de un bloque → días de una semana → vista de un día.
- [OPENCODE] Vista de un día: scroll vertical con, por cada ejercicio, su nombre, la tabla de series (fila = serie, columnas `previo` peso×reps / `peso` / `reps`, editable) y el campo de notas debajo. Agregar/eliminar series dentro de un ejercicio.
- [OPENCODE] Selector de ejercicio al agregar uno a un día: buscar en la biblioteca global (predefinidos + propios) o crear uno nuevo al vuelo.
- [OPENCODE] Pantalla de gestión de la biblioteca de ejercicios (ver/editar/borrar los propios; los predefinidos no se borran).
- [OPENCODE] Botón "Crear semana N+1 a partir de esta" dentro de la vista de una semana, que llama al endpoint de duplicación (o replica la lógica localmente si se prioriza offline-first para este módulo — a definir con el programador).
- [PROGRAMADOR] Probar en dispositivo real: carga de una semana completa, duplicación a la semana siguiente, edición de series, borrado de series y su efecto en la próxima duplicación.
- [OPENCODE] Documentar + `mem_save`.

## 14. Fase 13 — Diseño visual final

- [PROGRAMADOR] Buscar referencias visuales y definir paleta/tipografía propias (decisión de diseño personal, no delegable al agente).
- [OPENCODE] Implementar el sistema de diseño definido (tema, componentes reutilizables) sobre toda la app, incluyendo el módulo de entrenamiento.
- [OPENCODE] Documentar + `mem_save`.

---

## 15. Resumen de responsabilidades del programador (fuera de tareas de código)

Para tener a mano, todo lo que **no** es código y necesita tu intervención en algún punto:

- Cuentas y credenciales de servicios externos (backend hosting, base de datos).
- Instalación de SDKs/toolchains en tus máquinas.
- Decisiones de esquema/negocio que el agente propone pero no aprueba solo.
- Pruebas en dispositivos reales (Android físico, widget, notificaciones, doble click, módulo de entrenamiento).
- Despliegue y configuración de red/dominio del backend.
- Empaquetado y distribución de la app de escritorio.
- Decisiones de diseño visual y dónde se guardan los backups.
- Confirmar si la duplicación de semana ("crear N+1 a partir de N") se resuelve online (llamando al backend) u offline-first en el propio dispositivo, dado que en el gimnasio no siempre hay buena conexión.