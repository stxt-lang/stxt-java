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

El punto 20 (códigos de error divergentes con stxt-vscode, consecuencia de haber cerrado
el punto 16 por separado en cada implementación) se resolvió el 2026-07-27 adoptando los
códigos de TS, que separan casos que Java compartía: `VALUES_DEFINITION_NOT_ALLOWED` se
partió en `VALUES_NOT_ALLOWED_IN_EXTERNAL_NAMESPACE` y `VALUES_NOT_ALLOWED_IN_REFERENCE`,
alineando de paso los mensajes. Los `CHILDREN_*` y `TYPE_DEFINITION_NOT_ALLOWED` ya
coincidían.

25. **`VALUES_NOT_IN_ENUM` de TS: alinear al revés (arreglar stxt-vscode)** — la tercera
    divergencia detectada **no se adoptó a propósito**. Para la misma condición (`[values]`
    con un tipo distinto de `ENUM`), TS usa `VALUES_ONLY_SUPPORTED_BY_ENUM` en
    `SchemaParser.ts:88` pero `VALUES_NOT_IN_ENUM` en `TemplateParser.ts:131`; Java usa
    `VALUES_ONLY_SUPPORTED_BY_ENUM` en los dos sitios. Como un template es azúcar
    equivalente a un schema (STXT-TEMPLATE-SPEC 13/14), el mismo error no debería cambiar de
    código según la puerta de entrada: la inconsistencia parece un descuido del port, no una
    decisión. Pendiente cambiar `TemplateParser.ts:131` a `VALUES_ONLY_SUPPORTED_BY_ENUM`
    (una línea, en `../stxt-vscode`).

26. **Falta `DUPLICATE_ENUM_VALUE` (STXT-TEMPLATE-SPEC 14.14)** — detectado al inventariar
    códigos. `NodeDefinition.addValue` guarda en un `Set` y se traga los duplicados en
    silencio; TS los rechaza (`NodeDefinition.ts:57`). Afecta tanto a schemas como a
    templates. Es del mismo tipo que las validaciones que faltaban en el punto 19.

27. **`cl.getValues() != null` vs lista vacía** — Java rechaza por `!= null`; TS exige
    además `length > 0`. Afecta al caso límite `[]`, que en Java entra por la rama de
    error de cross-namespace/referencia y en TS no. Decidir cuál es el criterio correcto
    (probablemente el de Java: una lista vacía explícita también es una redefinición).

28. **`Node.getChild` con nombre ambiguo** — TS lanza `AMBIGUOUS_CHILD` cuando hay más de un
    hijo con el mismo nombre (`Node.ts:103`); Java devuelve el primero. Divergencia de API,
    sin reflejo en la spec.

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
