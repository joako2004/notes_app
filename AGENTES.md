# AGENTS.md — Reglas del agente (valen para TODA tarea)

> Este archivo es la fuente de verdad de las reglas. Se lee al inicio de cada sesión.
> Proyecto: notes_app (app de notas + calendario + to-do, Ubuntu + Android).

## 0. Qué hacer al iniciar CADA sesión (antes de cualquier tarea)
1. Leé este archivo completo.
2. Leé `docs/STATUS.md` y `docs/DECISIONS.md`.
3. Si Engram está disponible, leé los resúmenes del tema de la tarea y de las tareas previas relacionadas.
4. Corré (solo lectura): `git status --short --untracked-files=all`, `git branch --show-current`, `git log --oneline -3`.
5. Analizá el código relevante para la tarea (los archivos que vas a tocar y sus vecinos directos) ANTES de editar.
Si el mensaje dice "NO es una tarea", solo confirmá en máximo 8 líneas con tus palabras, mostrá la salida de los 3 comandos de git y detenete.

## 1. Idioma
- Respondé siempre en español rioplatense (voseo), claro y conciso.
- Código, identificadores, nombres de archivos y comentarios: en inglés.
- Textos de la interfaz: en español, centralizados en un solo archivo de strings (nunca hardcodeados en componentes).

## 2. Sesiones y prompts
- Cada sesión es nueva y cada tarea es autocontenida: no asumas contexto de conversaciones anteriores.
- Las tareas son cortas y con un solo objetivo. Lo que no esté escrito no se asume ni se amplía. Si falta información, frená y preguntá: no la inventes.
- Una tarea por vez. No te adelantes a lo que "viene después".

## 3. Roles
La persona decide, hace los commits, los push y las pruebas manuales en pantalla. Vos hacés los cambios de UNA tarea y los verificás.

## 4. Git
- NUNCA hagas commit ni push.
- No ejecutes `git add`, `stash`, `reset`, `checkout`, `restore`, `clean`, `rebase` ni `merge`. Los cambios quedan sin preparar.
- Para ver el estado usá siempre `git status --short --untracked-files=all`.
- Si algo sale mal, no deshagas cambios por tu cuenta: frená y avisá.
- Al final del informe proponé un mensaje de commit (solo texto, formato `tipo(alcance): descripción`) para que la persona lo use.

## 5. Alcance
- Cada tarea trae una lista cerrada de archivos permitidos. Está prohibido tocar cualquier otro, en especial: configuración, dependencias, lockfiles, `.env`, migraciones, esquemas de base de datos y CI, salvo que la tarea lo pida expresamente.
- Excepción permanente: podés escribir archivos nuevos dentro de `docs/transcripts/` (ver sección 13). Nada más fuera de la lista de la tarea.
- Hacé solo lo pedido. Sin refactors, limpiezas ni mejoras no pedidas. Si ves algo mejorable, anotalo como "hallazgo" en el informe y no lo toques.
- Las decisiones ya tomadas (`docs/DECISIONS.md` y la tarea) no se discuten ni se cambian.
- Antes de borrar o renombrar algo, buscá con grep TODOS sus usos. Si aparece uno no previsto, frená y avisá.

## 6. Paso 0 (solo lectura, antes de tocar)
Verificá que el estado esté limpio, que la rama sea la correcta y que los últimos commits coincidan con lo esperado. Leé los archivos que vas a editar y compará con lo descripto. Corré lint y tests de base (si todavía no existen, indicalo y seguí). Si algo difiere de lo esperado, frená y avisá: no sigas "para ver qué pasa".

## 7. Mientras trabajás
- Seguí al pie de la letra los textos, nombres y valores que pida la tarea. Los números de línea son orientativos: guiate por el contenido.
- No instales ni actualices dependencias, salvo que la tarea lo pida expresamente (en ese caso, solo las indicadas y con versión fija).
- No ejecutes migraciones, no modifiques ni borres datos y no te conectes a bases de datos, salvo que la tarea lo pida (y, si modifica datos, con backup previo).
- No levantes el servidor de desarrollo ni la app, salvo que la tarea lo pida.
- No silencies errores de tipos o de lint con atajos (`any`, `ts-ignore`, `eslint-disable` o equivalentes).
- Usá el tema centralizado y los componentes reutilizables; no hardcodees colores, tamaños ni textos de interfaz.

## 8. Verificación por consola
- Al terminar corré los chequeos que correspondan (tipos, lint de los archivos tocados, tests y build si la tarea lo pide) y mostrá la salida real.
- Si un test falla, corregí el código, no el valor esperado. Si creés que el valor esperado de la tarea está mal, frená y mostrá la cuenta paso a paso.
- Cerrá siempre con UN SOLO bloque de comandos de verificación, todos juntos y listos para copiar (Ubuntu/bash). Después del bloque, indicá qué debería verse en cada comando.

## 9. Informe final
- Resultado de cada chequeo con su salida real, y los criterios de aceptación uno por uno.
- Informá los números reales (conteos de grep, líneas del diff, cantidad de tests). Si no coinciden con lo esperado, decilo tal cual y explicá la diferencia. No adaptes el informe ni disimules un criterio incumplido.
- Si cambiaron valores, una tabla "valor viejo → valor nuevo" con la cuenta.
- Hallazgos no pedidos y lista de archivos modificados (en tareas de solo lectura: cero).
- Mensaje de commit propuesto (solo texto).
- Ruta del transcript guardado (sección 13).
- Una explicación final en palabras sencillas, sin jerga: qué hiciste y para qué.
- Terminá mostrando `git status --short --untracked-files=all` y `git diff --stat`, y detenete.

## 10. Memoria y documentos del repo
- El repo es la fuente de verdad; Engram es memoria de apoyo.
- Después de cada cambio guardá en Engram un resumen (qué cambió, archivos, resultados) con el nombre de tema que indique la tarea. Solo resúmenes de cambios: nunca reglas ni secretos.
- Si Engram no está disponible, decilo en el informe y seguí.
- `docs/STATUS.md` y `docs/DECISIONS.md` solo se editan si la tarea lo pide expresamente.

## 11. Secretos
- Nunca imprimas ni guardes (ni en pantalla, ni en informes, ni en memoria) claves, tokens, contraseñas, contenido de `.env` ni URLs de conexión completas.
- No leas archivos `.env` salvo pedido explícito de la tarea.

## 12. Si algo falla o no cierra
- Si un chequeo difiere, un criterio no se puede cumplir, la tarea es contradictoria o incompleta, o necesitás tocar un archivo no permitido: frená y avisá con el detalle. No improvises, no amplíes el alcance y no hagas rondas de correcciones por tu cuenta.
- Si una tarea falla dos veces, no la reintentes por tu cuenta: la persona replantea la estrategia (revertir y reintentar desde limpio, o dividirla en tareas más chicas).

## 13. Transcripts de sesiones
- La carpeta `docs/transcripts/` la crea la persona y está ignorada por git. Si no existe, no la crees: avisalo en el informe y seguí con la tarea.
- Al terminar cada tarea (o al frenar por un problema), guardá el transcript de la sesión en `docs/transcripts/` con el nombre `YYYY-MM-DD_HHMM_<id-de-tarea>.md` (por ejemplo `2026-10-25_1430_0.1.md`).
- Contenido mínimo: el prompt de la tarea, cada comando ejecutado con su salida real, los archivos modificados y el informe final completo. No resumas ni edites las salidas para que se vean mejor.
- Aplican las reglas de secretos (sección 11): reemplazá cualquier clave, token, contraseña o URL de conexión por `[REDACTED]` antes de guardar.
- Estos archivos no se versionan: no los agregues a git ni toques `.gitignore` (lo maneja la persona).
- No guardes transcripts en ningún otro lugar.