# STATUS.md — Estado del proyecto

Lo actualiza la persona al cerrar cada tarea (el agente NO lo edita salvo pedido expreso).

## Tareas completadas
- 0.1 Verificar entorno (solo lectura).
- 0.2 Preparar entorno (manual): Rust 1.99.1 + 4 targets Android; paquetes Linux de Tauri; Android SDK platform android-36, build-tools 36.1.0, cmdline-tools, NDK 30.0.16248370; variables ANDROID_HOME, NDK_HOME y JAVA_HOME en ~/.zshrc.

## Tarea en curso
(ninguna)

## Próxima tarea
- 0.3 Aislar Engram (ignorar memorias viejas de notes_app, prefijo notes_app-tauri/) y probar lectura/escritura.

## Hallazgos pendientes
- Engram tiene 5 observaciones de 2026-09-17 sobre un backend Python que NO corresponden a este proyecto (se ignoran, no se tocan).
- Java 21 instalado; Tauri Android documenta JDK 17: verificar en el spike 1.1.
- NDK 30.0.16248370 es muy nuevo: si falla en el spike 1.1, instalar una versión anterior.
- Engram CLI 1.10.1 tiene actualización disponible (3.6.0): decidir después.
