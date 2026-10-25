# STATUS.md — Estado del proyecto

Lo actualiza la persona al cerrar cada tarea (el agente NO lo edita salvo pedido expreso).

## Tareas completadas
- FASE 0 COMPLETA (0.1 verificar entorno, 0.2 preparar entorno, 0.3 aislar Engram con prefijo notes_app-tauri/).
- 1.1a Scaffold Tauri 2 + React + TypeScript (escritorio): react 19.3.1, vite 8.3.5, typescript 6.0.3, tauri 2.12.5.
- 1.1b Android init + APK debug aarch64. Java 21 y NDK 30.0.16248370 funcionan. Prueba en el Samsung SM-A528B OK con `npm run tauri android dev`.
- 1.2 Spike CodeMirror 6 en Android: mayúsculas automáticas, corrección y sugerencias funcionan en español e inglés, y CodeMirror se comporta igual que un textarea. Versiones probadas: codemirror 6.0.2, @codemirror/lang-markdown 6.5.2, @codemirror/state 6.7.6, @codemirror/view 6.43.15. El código del spike queda SOLO en la rama task/1.2-codemirror-spike (no se integra a main).

## Tarea en curso
(ninguna)

## Próxima tarea
- 1.3 Spike: widget Android nativo (Kotlin, RemoteViews) con lista de prueba donde tocar el selector hace desaparecer la tarea.

## Hallazgos pendientes
- Las mayúsculas automáticas dependen de la opción del teclado del teléfono (ya activada en el Samsung de pruebas).
- Las palabras mal escritas no se marcan con subrayado rojo en el WebView; el teclado sí corrige y sugiere. Revisar en el módulo del editor si se quiere.
- CodeMirror agrega ~600 kB al bundle (chunk de 829 kB, warning de Vite): hacer code splitting más adelante.
- npm 10.8.2 requiere `npm install --legacy-peer-deps` en este proyecto.
- Para probar en el celular: depuración USB autorizada y teléfono en la misma red que la compu.
- README.md genérico de Tauri y <title> de index.html de la plantilla: reemplazar en la fase de estilo.
- Android: la plantilla usa source/target Java 8 y APIs obsoletas de AGP (solo warnings).
- Engram CLI 1.10.1 tiene actualización disponible (3.6.0): decidir después.
- Engram: #250 duplica a #265 y #134 no tiene proyecto: inofensivas, no se tocan.
