# Pendientes de alineación stxt-java ↔ spec / stxt-vscode

Lista viva de desajustes del parser Java respecto a las specs de `../stxt-web/es/`
(STXT-SPEC, STXT-SCHEMA-SPEC, STXT-TEMPLATE-SPEC), verificados contra el código el
2026-07-27. La referencia de implementación alineada es `../stxt-vscode/stxt` (0.4.4);
los números de versión indican en qué release de stxt-vscode se hizo el ajuste
equivalente. Los puntos resueltos se eliminan de aquí.

## Núcleo (`dev.stxt`)

(sin pendientes; los puntos 1 a 4 —normalización IDN, `INVALID_NODE_NAME`,
`MIXED_INDENTATION` y espacios en namespace— se resolvieron el 2026-07-27,
junto con el punto 9, modelo de contenido cerrado)

## Tipos (`dev.stxt.schema.type`)

(sin pendientes; los puntos 5 a 8 —tipos `TIME`/`UUID`/`BINARY`/`MARKDOWN`,
`HEXADECIMAL` sin prefijo `#` ni paridad, validación en bloque de tipos binarios
sobre líneas trimmeadas, y formas de valor `NOT_ALLOWED_TEXT`/`BLOCK_FORM_REQUIRED`/
`GROUP` vacío— se resolvieron el 2026-07-27. De paso se corrigieron en
`../stxt-web/docs/` los dos documentos con `Color Tema: #...`
(`intro_programacion.stxt` y `receta_2.stxt`), quitando el prefijo `#`.)

## Validación de schema (`dev.stxt.schema`)

(sin pendientes; los puntos 10 a 12 —`Children` en tipos hoja`, `Min` > `Max`
y meta-schema desactualizado (`Values` sin `Type: GROUP`, `Type` sin `ENUM`)—
se resolvieron el 2026-07-27.)

## Templates (`dev.stxt.template`)

(sin pendientes; los puntos 13 a 16 —cardinalidades malformadas en
`ChildLineParser`, `[values]` restringido a `ENUM`/`ENUM` sin valores,
`Description` de template y schema ignorada, y cross-namespace/referencias que
ignoraban `[values]`/hijos en silencio (más el NPE de `type.startsWith("@")`)—
se resolvieron el 2026-07-27. De paso se cerró el hueco de conformidad que
quedó abierto al marcar resuelto el punto 10: `TemplateParser` ahora también
rechaza `Children` en nodos de tipo hoja (`CHILDREN_NOT_ALLOWED_FOR_TYPE`,
STXT-TEMPLATE-SPEC 8.2/14.9), para lo que `TypeRegistry.admitsChildren` pasó a
ser público. Los nuevos códigos de error introducidos son
`VALUES_DEFINITION_NOT_ALLOWED`, `CHILDREN_NOT_ALLOWED_IN_EXTERNAL_NAMESPACE`,
`CHILDREN_NOT_ALLOWED_IN_REFERENCE`, `EXTERNAL_DESCRIPTION_NOT_ALLOWED`,
`CHILDREN_DESCRIPTION_NOT_ALLOWED`, `NODE_NOT_FOUND` y
`DESCRIPTION_ALREADY_DEFINED`. Pendiente coordinar con `../stxt-vscode` (su
PENDIENTES.md, punto 7), que aún no tiene el fix de cross-namespace/referencias.)

## Decisiones de diseño (no estrictamente spec)

18. **Modelo de errores fail-fast vs multi-error** — el `Validator` Java lanza la
    primera excepción; el TS devuelve `ValidationException[]` y `parseResult()`
    acumula errores sin abortar. Para una librería de parseo el fail-fast puede
    ser aceptable; decidir si se porta el modelo multi-error (útil para
    herramientas construidas encima).

