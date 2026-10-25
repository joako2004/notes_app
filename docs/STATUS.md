# STATUS.md — Estado del proyecto

Lo actualiza la persona al cerrar cada tarea (el agente NO lo edita salvo pedido expreso).

## Tareas completadas
- FASE 0 COMPLETA (0.1 verificar entorno, 0.2 preparar entorno, 0.3 aislar Engram con prefijo notes_app-tauri/).
- 1.1a Scaffold Tauri 2 + React + TypeScript (escritorio): react 19.3.1, vite 8.3.5, typescript 6.0.3, @tauri-apps/api y cli 2.12.4, tauri 2.12.5. productName "Notas", identifier com.joako2004.notesapp. Ventana probada a mano.
- 1.1b Android init + APK debug aarch64 compilado (131M). Gradle 9.6.1, AGP 9.3.1, compileSdk/targetSdk 37, minSdk 24, Java 21 y NDK 30.0.16248370 funcionan. Gradle instaló solo platform android-37.0 y build-tools 36.0.0.

## Tarea en curso
- 1.1b prueba manual en el celular (la hace la persona): instalar el APK y abrir la app.

## Próxima tarea
- 1.2 Spike: CodeMirror 6 en Android (autocorrector, mayúsculas automáticas, español/inglés). Se define después de la prueba en el celular.

## Hallazgos pendientes
- npm 10.8.2 requiere `npm install --legacy-peer-deps` en este proyecto.
- README.md genérico de Tauri y <title> de index.html de la plantilla: reemplazar en la fase de estilo.
- Android: la plantilla usa source/target Java 8 y APIs obsoletas de AGP (solo warnings); revisar en la fase de pulido.
- Engram CLI 1.10.1 tiene actualización disponible (3.6.0): decidir después.
- Engram: #250 duplica a #265 y #134 no tiene proyecto: inofensivas, no se tocan.
