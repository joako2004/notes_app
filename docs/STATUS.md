# STATUS.md — Estado del proyecto

Lo actualiza la persona al cerrar cada tarea (el agente NO lo edita salvo pedido expreso).

## Tareas completadas
- FASE 0 COMPLETA (0.1 verificar entorno, 0.2 preparar entorno, 0.3 aislar Engram con prefijo notes_app-tauri/).
- 1.1a Scaffold Tauri 2 + React + TypeScript (escritorio): react 19.3.1, vite 8.3.5, typescript 6.0.3, @tauri-apps/api y cli 2.12.4, tauri 2.12.5. productName "Notas", identifier com.joako2004.notesapp. Ventana probada a mano.

## Tarea en curso
(ninguna)

## Próxima tarea
- 1.1b Spike: inicializar Android (tauri android init) y compilar APK debug aarch64, sin correr en el celular.

## Hallazgos pendientes
- npm 10.8.2 requiere `npm install --legacy-peer-deps` en este proyecto.
- README.md genérico de Tauri y <title> de index.html de la plantilla: reemplazar en la fase de estilo.
- Java 21 instalado; Tauri Android documenta JDK 17: verificar en 1.1b.
- NDK 30.0.16248370 es muy nuevo: si falla en 1.1b, instalar una versión anterior.
- Verificar que exista la memoria notes_app-tauri/1.1a en Engram (el transcript no mostró la salida del guardado).
- Engram CLI 1.10.1 tiene actualización disponible (3.6.0): decidir después.
- Engram: #250 duplica a #265 y #134 no tiene proyecto: inofensivas, no se tocan.
