# STATUS.md — Estado del proyecto

Lo actualiza la persona al cerrar cada tarea (el agente NO lo edita salvo pedido expreso).

## Tareas completadas
- 0.1 Verificar entorno (solo lectura).
- 0.2 Preparar entorno (manual): Rust 1.99.1 + 4 targets Android; paquetes Linux de Tauri; Android SDK android-36, build-tools 36.1.0, cmdline-tools, NDK 30.0.16248370; ANDROID_HOME, NDK_HOME y JAVA_HOME en ~/.zshrc.
- 0.3 Aislar Engram: prefijo notes_app-tauri/ probado (memorias #264 y #265); memorias viejas intactas.
- FASE 0 COMPLETA.

## Tarea en curso
(ninguna)

## Próxima tarea
- 1.1a Spike: scaffold de Tauri 2 + React + TypeScript (solo escritorio) en rama task/1.1a-tauri-scaffold.

## Hallazgos pendientes
- Java 21 instalado; Tauri Android documenta JDK 17: verificar en el spike 1.1b.
- NDK 30.0.16248370 es muy nuevo: si falla en el spike 1.1b, instalar una versión anterior.
- Engram CLI 1.10.1 tiene actualización disponible (3.6.0): decidir después.
- Engram: #250 (0.1 de la sesión anterior, tema distinto) duplica a #265; #134 sin proyecto. Inofensivas, no se tocan.
