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

Los puntos 25 y 26 se resolvieron el 2026-07-27, los dos alrededor del mismo principio: **una
condición de error no debe cambiar de código según la puerta de entrada** (schema o template),
porque un template es azúcar equivalente a un schema (STXT-TEMPLATE-SPEC 13).

- **25** — la tercera divergencia de códigos **no se adoptó de TS**, se arregló al revés. Para
  `[values]` con un tipo distinto de `ENUM`, TS usaba `VALUES_ONLY_SUPPORTED_BY_ENUM` en
  `SchemaParser.ts` pero `VALUES_NOT_IN_ENUM` en `TemplateParser.ts`, mientras Java ya usaba el
  primero en las dos vías. Se cambió `TemplateParser.ts` en `../stxt-vscode` (con entrada en su
  CHANGELOG, sección Unreleased); sus 224 tests siguen pasando.
- **26** — duplicados de valores (STXT-SCHEMA-SPEC 13.9 / STXT-TEMPLATE-SPEC 14.14). Resultó ser
  más estrecho de lo anotado: `ChildLineParser` ya los rechazaba, así que el hueco era sólo la vía
  de schema, donde `NodeDefinition.addValue` los guardaba en un `Set` y los perdía en silencio. El
  check está ahora en `addValue`, que es el punto por el que pasan las dos vías, con el código que
  Java ya usaba (`VALUE_DUPLICATED`) y trim explícito. **No** se adoptó el `DUPLICATE_ENUM_VALUE`
  de TS: ese repo tiene aquí el mismo split-brain que el punto 25 (`VALUE_DUPLICATED` en
  `ChildLineParser.ts:78`, `DUPLICATE_ENUM_VALUE` en `NodeDefinition.ts:61`), y copiarlo lo habría
  importado. Queda pendiente el arreglo simétrico en `../stxt-vscode` → punto 29.

El punto 29 (el simétrico del 25 para duplicados) se resolvió el 2026-07-27 en `../stxt-vscode`:
`NodeDefinition.addValue` pasa a `VALUE_DUPLICATED`, el código que su `ChildLineParser` ya usaba, y
de paso trimea antes de comparar —tenía el mismo hueco que Java en el punto 26, comparaba el valor
crudo—. Compilado, lint limpio y sus 224 tests pasando, con entrada en su CHANGELOG.

27. **`cl.getValues() != null` vs lista vacía** — Java rechaza por `!= null`; TS exige
    además `length > 0`. Afecta al caso límite `[]`, que en Java entra por la rama de
    error de cross-namespace/referencia y en TS no. Decidir cuál es el criterio correcto
    (probablemente el de Java: una lista vacía explícita también es una redefinición).

28. **`Node.getChild` con nombre ambiguo** — se resolvió el 2026-07-28. No era cierto que Java
    devolviera el primer hijo en silencio: ya lanzaba excepción, pero con dos divergencias reales
    frente a `Node.ts`: usaba `IllegalArgumentException` (JDK, sin código, fuera de
    `dev.stxt.exceptions`) en vez de una excepción con código `AMBIGUOUS_CHILD`; y
    `getChildren(cname)`/`getChild(cname)` no filtraban por namespace (solo por nombre
    normalizado), mientras que TS acepta un `namespace` opcional que por defecto es el propio
    namespace del nodo (`getChildrenByName(cname, namespace?)`). Ahora `getChild`/`getChildren`
    tienen sobrecarga `(cname, namespace)` —con la de un solo argumento delegando en el namespace
    propio del nodo, igual que el default de TS— y lanzan `STXTException("AMBIGUOUS_CHILD", ...)`.

## Cobertura de tests

El punto 22 se resolvió el 2026-07-27: `src/test/java/dev/stxt/corpus/` corre 222 tests
dinámicos contra el corpus real de `../stxt-web` (25 schemas/templates de `.stxt/**`, 44
documentos de `docs/`, `es/` y `en/`). El corpus **no se copia** aquí a propósito: stxt-web es la
fuente normativa y los tests deben fallar cuando la implementación se separa de los documentos
reales, no de una copia congelada. Si el proyecto hermano no está, las suites se saltan
(`Assumptions.assumeTrue`) en vez de fallar; se puede forzar la ruta con `STXT_WEB=/ruta`.

El puente es `test.Corpus.CorpusLoader`, un `ResourcesLoader` en memoria que indexa por el
namespace que declara cada fichero, porque el layout de stxt-web (`schemas/`, `templates/`,
`website/`, …) no es el `<ns>/<resource>.stxt` que espera `ResourcesLoaderDirectory`.

Dos diferencias con el equivalente de stxt-vscode, ambas derivadas del (ya resuelto) punto 18:
donde el TS compara listas de errores, aquí la comprobación sigue siendo "no lanza excepción" (o
compara el primer error) porque las suites de corpus usan `parse()`/`parseFile()`, no el nuevo
`parseResult()`; adaptarlas a comparar la lista completa de errores queda pendiente si se quiere
paridad total con TS.

23. **`docs_raw/` sigue reservada y vacía** — cobertura de parseo puro (documentos sin
    namespace, sin validación de schema) todavía sin escribir.

## Decisiones de diseño (no estrictamente spec)

(el punto 18 —modelo de errores fail-fast vs multi-error— se resolvió el 2026-07-28,
siguiendo el diseño de `../stxt-vscode`: `Validator.validate` devuelve
`List<ValidationException>` en vez de lanzar; `SchemaValidator` acumula en esa lista
los errores de tipo, hijos no declarados y cardinalidad de un mismo nodo en vez de
abortar en el primero; `Parser` gana `parseResult(String)`/`parseResultFile(File)`,
que recorren todo el documento acumulando errores de sintaxis y de validación en un
nuevo `ParseResult` (nodos + errores) sin interrumpirse en el primero. `parse(String)`/
`parseFile(File)` no cambian de contrato: delegan en `parseResult` y lanzan el primer
error acumulado, preservando el comportamiento fail-fast para el código existente.)

24. **Tipo de excepción en errores de `Structure`** — Java lanza `ParseException` en todos
    los errores de template; TS los mantiene como `ValidationException` (fue un punto de su
    0.4.2, motivado por la severidad Warning en el editor). Aquí no hay severidades, pero es
    divergencia de contrato de API; con el punto 18 ya resuelto, esta decisión queda
    independiente y sigue pendiente.
