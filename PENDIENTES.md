# Pendientes de alineación stxt-java ↔ spec / stxt-vscode

Lista viva de desajustes del parser Java respecto a las specs de `../stxt-web/es/`
(STXT-SPEC, STXT-SCHEMA-SPEC, STXT-TEMPLATE-SPEC), verificados contra el código el
2026-07-27. La referencia de implementación alineada es `../stxt-vscode/stxt` (0.5.0,
ya sin PENDIENTES.md propio); los números de versión indican en qué release de
stxt-vscode se hizo el ajuste equivalente. Los puntos resueltos se eliminan de aquí.

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

Los puntos 13 a 16 —cardinalidades malformadas en `ChildLineParser`, `[values]`
restringido a `ENUM`/`ENUM` sin valores, `Description` de template y schema ignorada,
y cross-namespace/referencias que ignoraban `[values]`/hijos en silencio (más el NPE
de `type.startsWith("@")`)— se resolvieron el 2026-07-27, junto con `Children` en
nodos de tipo hoja (`CHILDREN_NOT_ALLOWED_FOR_TYPE`, para lo que
`TypeRegistry.admitsChildren` pasó a ser público).

El mismo día se cerró el punto 19 (validaciones que faltaban en `TemplateParser`,
equivalentes a stxt-vscode 0.5.0): tipo desconocido `TYPE_NOT_VALID`
(STXT-TEMPLATE-SPEC 14.6; en schemas ya lo cubría el meta-schema, pero el `Structure`
de un template es bloque de texto y la meta-validación no lo alcanza), referencia que
no resuelve `REFERENCE_NOT_FOUND` (14.11; antes creaba un nodo cuyo tipo era
literalmente `@Nombre`) y referencia con tipo explícito
`REFERENCE_WITH_TYPE_NOT_ALLOWED` (14.13; antes se reportaba como
`NODE_REFERENCE_NOT_VALID`, cuyo mensaje culpaba al nombre). El helper
`referenceType` distingue `@Nombre TIPO` de una referencia cuyo nombre lleva espacios
(`@Max Threads`): sólo hay tipo si el último token es un tipo conocido **y** lo que
queda delante es el nombre del propio nodo.

20. **Códigos de error divergentes con stxt-vscode** — al cerrar el punto 16 en las dos
    implementaciones por separado, los códigos no coinciden. La spec no los normativiza,
    pero conviene unificar; los de TS separan mejor los casos y son los que se propone
    adoptar:

    | Caso | Java (actual) | TS 0.5.0 |
    |---|---|---|
    | `[values]` en cross-namespace | `VALUES_DEFINITION_NOT_ALLOWED` | `VALUES_NOT_ALLOWED_IN_EXTERNAL_NAMESPACE` |
    | `[values]` en referencia `@Nodo` | `VALUES_DEFINITION_NOT_ALLOWED` (mismo código) | `VALUES_NOT_ALLOWED_IN_REFERENCE` |
    | `[values]` con tipo ≠ `ENUM` | `VALUES_ONLY_SUPPORTED_BY_ENUM` | `VALUES_NOT_IN_ENUM` |

    Los códigos `CHILDREN_*` y `TYPE_DEFINITION_NOT_ALLOWED` sí coinciden. Al tocarlo hay
    que actualizar `TemplateParserTest`, que afirma los códigos actuales.

21. **`cl.getValues() != null` vs lista vacía** — Java rechaza por `!= null`; TS exige
    además `length > 0`. Afecta al caso límite `[]`, que en Java entra por la rama de
    error de cross-namespace/referencia y en TS no. Decidir cuál es el criterio correcto
    (probablemente el de Java: una lista vacía explícita también es una redefinición).

## Cobertura de tests

22. **No hay tests contra el corpus real de `stxt-web`** — stxt-vscode corre 224 tests que
    cargan todos los schemas y templates de `../stxt-web/.stxt` y validan contra ellos todos
    los documentos de `docs/`, `es/` y `en/`, más un test de que el schema y el template de
    un mismo namespace validan idéntico, y otro de round-trip del writer en los dos estilos
    de indentación. Aquí seguimos con 60 tests sobre fixtures propios. Es lo que hace que
    los desajustes con la spec se descubran tarde.

23. **`docs_raw/` sigue reservada y vacía** — cobertura de parseo puro (documentos sin
    namespace, sin validación de schema) todavía sin escribir.

## Decisiones de diseño (no estrictamente spec)

18. **Modelo de errores fail-fast vs multi-error** — el `Validator` Java lanza la
    primera excepción; el TS devuelve `ValidationException[]` y `parseResult()`
    acumula errores sin abortar. Para una librería de parseo el fail-fast puede
    ser aceptable; decidir si se porta el modelo multi-error (útil para
    herramientas construidas encima).

24. **Tipo de excepción en errores de `Structure`** — Java lanza `ParseException` en todos
    los errores de template; TS los mantiene como `ValidationException` (fue un punto de su
    0.4.2, motivado por la severidad Warning en el editor). Aquí no hay severidades, pero es
    divergencia de contrato de API y va ligada a la decisión del punto 18.
