# PLAN.md — Plan de desarrollo por fases

Cada fase se divide en tareas mínimas (un objetivo, archivos permitidos cerrados, criterios verificables por consola). El detalle de cada módulo se decide al empezarlo.

## Fase 0 — Base del proyecto
- 0.1 Verificar entorno (solo lectura). HECHA.
- 0.2 Preparar entorno Ubuntu/Android (manual). HECHA.
- 0.3 Aislar Engram con prefijo notes_app-tauri/. HECHA.

## Fase 1 — Spikes (pruebas chicas de lo riesgoso; se descartan o se reutilizan)
- 1.1a Scaffold de Tauri 2 + React + TypeScript en escritorio (build y chequeo de Rust). HECHA.
- 1.1b Inicializar Android y correr la app en el celular físico. (build APK HECHO; prueba en celular pendiente)
- 1.2 CodeMirror 6 en Android: autocorrector, mayúsculas automáticas, español/inglés.
- 1.3 Widget Android nativo (Kotlin) con lista fija y un selector que la saca de pantalla.
- 1.4 Alarma exacta con la app cerrada y el teléfono en reposo (permisos, batería, reinicio).
- 1.5 SQLite compartido entre la app y el widget.
- 1.6 Supabase: tabla mínima, push/pull entre compu y celular.
- 1.7 Google Calendar: login OAuth y lectura/escritura de un evento de prueba.
- Salida de la fase: decisión confirmada (o plan B) por cada spike, anotada en DECISIONS.md.

## Fase 2 — Núcleo de la app
- 2.1 Estructura del proyecto, tema centralizado, strings en español, navegación responsive.
- 2.2 Capa de datos SQLite (migraciones, repositorios, tests).
- 2.3 Notas, carpetas y notas dentro de carpetas (CRUD).
- 2.4 Editor markdown con vista en vivo.

## Fase 3 — Sincronización
- 3.1 Esquema en Supabase y reglas de seguridad.
- 3.2 Motor de sync (push/pull, borrados lógicos, conflictos) con tests.
- 3.3 Sync de imágenes adjuntas.
- 3.4 Actualización en tiempo real entre dispositivos.

## Fase 4 — Notas avanzadas
- 4.1 Wikilinks y backlinks.
- 4.2 Etiquetas.
- 4.3 Imágenes adjuntas.
- 4.4 Búsqueda de texto completo.
- 4.5 Exportar/importar .md.

## Fase 5 — To-do y hábitos
- 5.1 Lógica de períodos y reinicio (con tests).
- 5.2 Pantalla de tareas, 3 estados y alternancia fácil.
- 5.3 Repeticiones configurables (diaria, semanal, mensual, otras).

## Fase 6 — Widget Android del to-do
- 6.1 Widget definitivo (solo pendientes, marcar y desaparecer, reinicio).

## Fase 7 — Calendario propio
- 7.1 Vistas del calendario y CRUD de eventos.
- 7.2 Recordatorios y alarmas locales (Kotlin) con notificaciones.

## Fase 8 — Google Calendar
- 8.1 Conexión OAuth y sync incremental bidireccional.
- 8.2 Conflictos y eventos recurrentes.

## Fase 9 — Estilo y pulido
- 9.1 Diseño visual, claro/oscuro, accesibilidad.
- 9.2 Empaquetado (.deb/AppImage y APK) y revisión final.