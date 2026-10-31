# STATUS.md — Estado del proyecto

Lo actualiza la persona al cerrar cada tarea (el agente NO lo edita salvo pedido expreso).

## Tareas completadas
- FASE 0 COMPLETA (0.1 verificar entorno, 0.2 preparar entorno, 0.3 aislar Engram con prefijo notes_app-tauri/).
- 1.1a Scaffold Tauri 2 + React + TypeScript (escritorio): react 19.3.1, vite 8.3.5, typescript 6.0.3, tauri 2.12.5.
- 1.1b Android init + APK debug aarch64. Java 21 y NDK 30.0.16248370 funcionan. Prueba en el Samsung SM-A528B OK con `npm run tauri android dev`.
- 1.2 Spike CodeMirror 6 en Android: mayúsculas, corrección y sugerencias OK en español e inglés. El código del spike queda SOLO en la rama task/1.2-codemirror-spike (no se integró a main).
- 1.3 Spike widget Android nativo (RemoteViews, Kotlin, SharedPreferences): lista de 6 tareas de prueba, marcar y desaparecer sin abrir la app, persiste tras forzar detención y reinicio, restablecer, vista vacía "Todo al día", modo oscuro OK. Código integrado a main en src-tauri/gen/android (paquete widget/).

## Tarea en curso
(ninguna)

## Próxima tarea
- 1.4 Spike: alarma exacta que suena con la app cerrada y el teléfono en reposo (permisos, Doze, reinicio, batería de Samsung).

## Hallazgos pendientes
- Widget (Fase 6): selector un poco más grande; animación de marcado (tilde y luego salida) — RemoteViews no permite animaciones propias, evaluar el efecto en dos pasos.
- NO volver a correr `tauri android init`: puede pisar el AndroidManifest.xml modificado.
- Las mayúsculas automáticas dependen de la opción del teclado del teléfono (ya activada).
- Las palabras mal escritas no se marcan con subrayado rojo en el WebView; el teclado sí corrige y sugiere.
- CodeMirror agrega ~600 kB al bundle: hacer code splitting más adelante.
- npm 10.8.2 requiere `npm install --legacy-peer-deps` en este proyecto.
- Para probar en el celular: depuración USB autorizada; para `tauri android dev`, mismo Wi-Fi. Para instalar un APK ya compilado alcanza `adb install -r`.
- README.md genérico de Tauri y <title> de index.html de la plantilla: reemplazar en la fase de estilo.
- Android: la plantilla usa source/target Java 8 y APIs obsoletas de AGP; el widget usa 2 APIs clásicas marcadas como obsoletas (solo warnings).
- Engram CLI 1.10.1 tiene actualización disponible (3.6.0): decidir después.
- Engram: #250 duplica a #265 y #134 no tiene proyecto: inofensivas, no se tocan.

## Tarea 1.4 — spike de alarma exacta (cerrada con validaciones pendientes)
- Probado en Samsung SM-A528B (Android 14), pruebas del 2026-10-30: base, app cerrada con pantalla apagada (`clock` y `exact`, 10 min) y cancelar. Atrasos de 34 a 838 ms.
- Pendiente de validar: (a) noche real, una con `clock` y otra con `exact` (cubre Doze real, porque `deviceidle force-idle` no entró en Doze profundo en este teléfono); (b) reinicio: hay evidencia parcial con `exact` (línea "reprogramada tras reinicio"), falta repetirlo a propósito y probar `clock` y el caso "perdida durante el apagado".
- Hallazgo: la notificación no tiene `contentIntent`, tocarla solo la descarta.
- Próxima tarea: 1.5

## Tarea 1.5 — Investigación SQLite compartido app/widget (cerrada 2026-10-31)
- Solo investigación, sin código. Transcript: docs/transcripts/2026-10-31_1111_1.5.md.
- Hallazgo clave: Rust (sqlx/libsqlite3-sys, enlazado estático) y Kotlin (SQLite del sistema) son dos copias de SQLite; en el mismo proceso, SQLite documenta riesgo de corrupción (howtocorrupt, sec. 2.3). WAL no lo resuelve.
- Pendientes de verificar en 1.5b: (1) si sqlx fija WAL/busy_timeout por defecto (no verificado, fijarlos explícito igual); (2) que separar procesos (`:widget`) alcance en la práctica (inferencia, no dato de la fuente); (3) ruta real de la base en Android (dataDir); (4) lectura read-only de una base WAL desde Kotlin.
- Hallazgo no cubierto por el informe: los receivers de alarmas (1.4, Fase 7.2) corren en el proceso principal y tendrían el mismo riesgo si leen la base. Se resuelve en 1.5d.
- Pendientes de la 1.4 siguen abiertos (prueba de la noche y de reinicio).

## Tarea 1.5b — Spike SQLite compartido Rust→widget (cerrada 2026-10-31)
- Rust (sqlx 0.8.6, SQLite embebido 3.46.0) crea notes_spike.db en WAL; el widget, en proceso `:widget`, la lee con el SQLite del sistema (3.39.2, Samsung Android 14) en OPEN_READONLY: pending_count=3, journal_mode=wal, rust_lib_loaded=false, dos PIDs distintos, misma ruta (dataDir/databases/notes_spike.db). Transcript: docs/transcripts/2026-10-31_1145_1.5b.md.
- Widget antes de abrir la app: db_exists=false, no crea nada, muestra el estado vacío.
- Sin probar todavía: API 24 (SQLite 3.9.2, el read-only sobre WAL requiere 3.22; hay fallback sin verificar), escrituras concurrentes/SQLITE_BUSY, refresco app→widget, integrity_check.
- Hallazgos: "Todo al día" se usa también cuando falta la base; WidgetTasks.kt y WidgetTaskStore quedaron sin uso; toggle/reset de SharedPreferences desconectados de la lista hasta la 1.5c.

## Tarea 1.5c — El widget escribe en la base compartida (cerrada 2026-10-31)
- En el Samsung (Android 14): tocar una fila del widget marca la tarea como 'done' desde :widget (SharedDbWriter, goAsync + executor) en 4–27 ms, rows=1, retries=0, con la app abierta y cerrada. El widget refresca y la fila desaparece. Rust (hilo vigía cada 2 s) detecta el cambio con ~1 s de retraso. Dos toques seguidos (~125 ms): sin bloqueos. Reset del encabezado: rows=4. Transcript: docs/transcripts/2026-10-31_1843_1.5c.md.
- HALLAZGO: con la app cerrada, la escritura del widget deja la base en journal_mode=delete (el widget abre RW sin ENABLE_WRITE_AHEAD_LOGGING). Con la app abierta se mantiene wal. Rust vuelve a fijar WAL al arrancar. Mecanismo probable, sin verificar: el framework de Android intenta pasar la base a su modo por defecto y solo lo logra sin otras conexiones abiertas. Se corrige en 1.5d.
- Sin probar: API 24 (SQLite 3.9.2), contención real con Rust escribiendo a la vez, estrés.
- Receptor del widget exported=true: cualquier app puede mandar ACTION_TOGGLE_TASK/ACTION_RESET_TASKS. Evaluar exported=false o permiso signature en la Fase 6.
- Conocido: la lista sale ordenada por updated_at y título; WidgetTaskStore.kt y WidgetTasks.kt siguen sin uso; ACTION_RESET_TASKS es herramienta del spike.
