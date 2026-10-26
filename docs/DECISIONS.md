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
