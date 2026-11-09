# DECISIONS.md — Decisiones técnicas tomadas

Las decisiones de esta lista NO se discuten ni se cambian dentro de una tarea. Cambiarlas requiere decisión explícita de la persona y una entrada nueva acá.

## Producto
- App de uso personal (un solo usuario). Plataformas: Ubuntu (aplicación de escritorio, no web) y Android. Sin iOS por ahora.
- Offline-first: todo funciona sin internet y sincroniza después.
- UI en español; el editor debe aceptar también inglés. Autocorrector y mayúsculas automáticas.
- Sin MVP: se desarrollan todos los módulos en orden hasta terminar.

## Stack
- Tauri 2 (escritorio Linux + Android) con interfaz React + TypeScript (Vite).
- Editor de notas: CodeMirror 6 con vista en vivo de markdown (estilo Obsidian).
- Código nativo Kotlin dentro del proyecto Android para: widget del to-do, alarmas exactas y notificaciones.
- Plan B si Tauri en Android falla en el spike: Capacitor solo para Android, reutilizando la interfaz React.

## Datos y sincronización
- SQLite local en cada dispositivo (fuente de verdad de la interfaz).
- Todo registro: id UUID, updated_at, deleted_at (borrado lógico), device_id.
- Backend gratuito: Supabase (Postgres + Storage + Realtime), un solo usuario, seguridad por fila (RLS).
- Sync: push/pull por marca de tiempo; conflicto en notas = gana la última edición y se conserva copia del texto anterior.
- Limitación conocida: los proyectos gratuitos de Supabase se pausan tras 7 días sin actividad (los datos se conservan).
- Notas exportables/importables como archivos .md.

## Calendario, alarmas, to-do
- Google Calendar bidireccional vía API de Google (OAuth, sync incremental); detalles finos al desarrollar ese módulo.
- Alarmas y recordatorios se programan localmente en el teléfono (alarmas exactas de Android), también con la app cerrada; se reprograman al reiniciar el teléfono. Los eventos creados en otro dispositivo llegan por sync y se programan localmente.
- To-do: estados pendiente / en proceso / finalizada. Repetición por calendario: reinicia a las 00:00 hora local aunque se haya marcado a las 23:59. Finalizada se mantiene hasta el reinicio y entonces vuelve a pendiente. Se calcula al leer comparando el período actual con el período de la última finalización (no depende de procesos en segundo plano).
- Widget Android: solo tareas pendientes; al marcarla pasa a finalizada y desaparece hasta el reinicio.

## Estilo y código
- Funcional primero; estilo visual al final. Desde el inicio: tema centralizado (colores, tamaños, tipografías), modo claro/oscuro, componentes reutilizables, diseño responsive.
- Código e identificadores en inglés; textos de interfaz en español en un único archivo de strings.
- Tests desde temprano para lógica pura (recurrencias, reinicio de tareas, sync).
- Una rama por tarea, creada por la persona.
## Herramientas
- npm 10.8.2 falla con `npm install` plano en este proyecto; se usa `npm install --legacy-peer-deps` (el package-lock.json se generó así). Revisar si se actualiza npm.
- Android: Java 21 y NDK 30.0.16248370 funcionan con Tauri 2.12.5 (AGP 9.3.1, Gradle 9.6.1, compileSdk 37). El plan B de JDK 17 / NDK anterior no hace falta. Gradle instala solo platform android-37.0 y build-tools 36.0.0.
- Prueba en el celular: `npm run tauri android dev` con el teléfono conectado por USB (depuración USB autorizada) y en la misma red que la compu, porque Tauri usa la IP de la red local para el servidor de desarrollo.
- Editor: CodeMirror 6 confirmado en Android (Samsung SM-A528B, Android 14): mayúsculas, corrección y sugerencias OK en español e inglés. Requiere la opción de mayúsculas automáticas activada en el teclado.
- Widget: RemoteViews clásicos confirmado en el Samsung SM-A528B (Android 14): lista desplazable, marcar y desaparecer sin abrir la app, estado en SharedPreferences que sobrevive a forzar detención y reinicio. Glance/Compose no hacen falta.

## 1.4 — alarma exacta (provisional)
- Las alarmas se programan localmente en el teléfono con AlarmManager y suenan con la app cerrada y la pantalla apagada (pruebas de 10 min, `clock` y `exact`).
- Decisión provisional: `setAlarmClock` ("clock") para alarmas visibles y `setExactAndAllowWhileIdle` ("exact") para recordatorios. Se confirma tras la prueba de la noche; el diseño definitivo es de la tarea 7.2.
- Doze forzado no fue posible en este teléfono; se valida con la prueba de la noche.

## Decisión 1.5 — Base SQLite compartida entre app y widget (2026-10-31)
- Camino principal (A1): archivo SQLite único, Rust dueño del esquema y migraciones (sqlx directo, WAL, busy_timeout), widget Kotlin en proceso propio (`android:process=":widget"`).
- Plan B: Kotlin dueño de la base en Android + plugin Tauri como puente.
- Descartadas como arquitectura principal: C (snapshot + cola; escritura diferida) y D (ContentProvider; no resuelve el acceso de Rust).
- Prohibido: widget en el proceso principal con SQLite propio abierto sobre el mismo archivo que Rust.
- Provisoria hasta que 1.5b/1.5c/1.5d la confirmen o se pase al plan B.

## Confirmación 1.5b — A1 sostenida en lectura (2026-10-31)
- La lectura desde `:widget` sobre una base WAL creada por Rust funciona en Android 14 con dos copias de SQLite en procesos distintos. A1 sigue como camino principal; sigue provisoria hasta 1.5c (escritura), 1.5d (refresco, alarmas) y la prueba en API 24.
- Corrección: sqlx 0.8.6 NO fija journal_mode por defecto (SQLite queda en DELETE en bases no WAL); busy_timeout 5 s, foreign_keys ON y synchronous FULL son los defaults. Fijamos WAL explícito. Fuente: docs.rs/sqlx/0.8.6 SqliteConnectOptions.

## Confirmación 1.5c — Escritura desde :widget (2026-10-31)
- A1 sostenida también en escritura en Android 14: el widget (SQLite del sistema) escribe en el archivo creado por Rust (SQLite embebido) y la app lo detecta.
- Regla nueva (provisoria hasta 1.5d): toda conexión Android de lectura/escritura debe abrir la base conservando WAL; si no, la base puede quedar en journal_mode=delete cuando la app está cerrada.
- Riesgo anotado: receptor del widget exported=true acepta acciones propias de cualquier app.

## Confirmación 1.5d — Regla de apertura desde Android (2026-11-01)
- Toda apertura de lectura/escritura de la base desde Kotlin usa ENABLE_WRITE_AHEAD_LOGGING. No depender del journal mode por defecto del fabricante.
- Regla derivada, a confirmar en la Fase 7.2: ningún código Kotlin que abra SQLite corre en el proceso principal (donde vive Rust); widget y receivers de alarmas que lean/escriban la base van en proceso propio.

## Cierre 1.5e: Rust → widget con broadcast explícito por JNI
- Elegido: (b) `ndk-context` + `jni` 0.21.1 desde un hilo nativo propio: `Intent(action)` + `ComponentName(paquete, clase)` + `Context.sendBroadcast`. Solo clases del framework; los nombres de las clases de la app van como String (sin `FindClass` de clases propias).
- Descartados: (a) `PlatformWebview::jni_handle` (salto asíncrono al hilo principal y canal para el Result); (c) plugin móvil de Tauri (crate, proyecto Android library y registro; fuera del alcance de un spike).
- Kotlin: `ACTION_REFRESH_LIST` → `refreshWidgets(context)`, sin SQLite ni `goAsync`; manifiesto sin cambios.
- `refresh=ok` significa "broadcast enviado". En el Motorola se vio que el widget relee tras cada envío y que no leyó entre la creación de la base y el primer broadcast.
- Riesgo conocido: `ndk_context::android_context()` entra en pánico si la actividad fue destruida. Aceptado en el spike; el envío definitivo (Fase 2) debe resolverlo.

## Cierre 1.6a: qué queda decidido y qué no
- El backend sigue siendo Supabase (sin cambios). El cliente de sync, la autenticación, el manejo de URL y clave y el almacenamiento de la sesión quedan SIN decidir hasta el spike 1.6c.
- Hecho verificado que lo condiciona todo: el build actual no tiene TLS. Antes de hablar con Supabase hay que lograr HTTPS desde Rust en escritorio y Android (spike 1.6b, sin Supabase).
- Regla de diseño: la lista que ve la persona sale siempre del SQLite local; la red solo se usa para sincronizar.
- Esquema a respetar en 1.6c y la Fase 3: updated_at bigint; las filas borradas deben poder bajar a los otros dispositivos; políticas de select, insert y update; cursor de pull por (updated_at, id).

## Cierre 1.6b — Camino HTTPS desde Rust (2026-11-09)
- Elegido: reqwest directo con rustls y raíces de certificados embebidas (webpki-roots). Sin JNI, sin Kotlin, sin Gradle.
- No evaluada: tauri-plugin-http (el agente la descartó por requerir registrar el plugin; no se probó).
- Descartado: verificador de plataforma por defecto (rustls-platform-verifier). En Android exige inicializarlo por JNI.
- Contras aceptados: no respeta CAs instaladas por la persona en el teléfono; las raíces solo se actualizan con una versión nueva de la app.
- Plan B si hiciera falta: inicializar rustls-platform-verifier (JNI + Gradle), como tarea aparte.
