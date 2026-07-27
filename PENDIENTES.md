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

5. **Tipos que faltan**: `TIME`, `UUID`, `BINARY` (STXT-SCHEMA-SPEC 9.4/9.5;
   vscode 0.4.1) y `MARKDOWN` (9.7; vscode 0.3.4). Añadir clase + registro en
   `TypeRegistry` + meta-schema (ver punto 12).

6. **HEXADECIMAL** (STXT-SCHEMA-SPEC 9.5; vscode 0.4.4) — Java admite prefijo `#`,
   exige longitud par y borra whitespace interno. La spec: cualquier cadena
   `[0-9A-Fa-f]+`, sin prefijo ni paridad. Nota: al portarlo, corregir en
   `../stxt-web/docs/` los dos documentos con `Color Tema: #...`
   (`intro_programacion.stxt` y `receta_2.stxt`) o decidir cambiar la spec 9.5
   (pendiente 10 de stxt-vscode; ese repo ya es escribible desde aquí).

7. **Validación en bloque de los tipos binarios** (`HEXADECIMAL`, `BASE64`, y el
   futuro `BINARY`) (STXT-SCHEMA-SPEC 9.5; vscode 0.4.4) — `cleanSpaces` elimina
   todo el whitespace, también el interior de una línea. Debe validarse la
   concatenación de líneas **trimmeadas**; el whitespace dentro de una línea no se
   elimina en silencio.

8. **Formas de valor por tipo** (STXT-SCHEMA-SPEC 9.2–9.4; vscode 0.4.4) —
   - Los tipos inline-only con regex (`NUMBER`, `DATE`, `BOOLEAN`, `URL`, `EMAIL`,
     `INTEGER`, `NATURAL`, `TIMESTAMP`) no rechazan explícitamente la forma `>>`
     (validan `getText()` contra la regex); deben fallar con `NOT_ALLOWED_TEXT`
     como ya hacen `INLINE` y `ENUM`.
   - `BLOCK` solo rechaza valor inline no vacío; un `Nombre:` vacío pasa. Debe
     exigir la forma `>>` con `BLOCK_FORM_REQUIRED`.
   - `GROUP` no rechaza la forma `>>` vacía; debe rechazar tanto valor inline como
     forma de bloque.

## Validación de schema (`dev.stxt.schema`)

10. **`Children` en tipos hoja** (STXT-SCHEMA-SPEC 13.5; vscode 0.4.4) —
    `SchemaParser` acepta `Children` en nodos de cualquier tipo. Debe fallar la
    carga con `CHILDREN_NOT_ALLOWED_FOR_TYPE` para todo tipo salvo `INLINE` y
    `GROUP`. Equivalente en templates: STXT-TEMPLATE-SPEC 14.9.

11. **`Min` > `Max`** (STXT-SCHEMA-SPEC 10; vscode 0.4.2) — no se valida al cargar
    el schema. Debe rechazarse con `MIN_GREATER_THAN_MAX`.

12. **Meta-schema desactualizado** (`SchemaProviderMeta`; vscode 0.4.1/0.4.4) —
    - `Node: Values` no declara `Type: GROUP` → `Values: texto` pasa la
      meta-validación (STXT-SCHEMA-SPEC 15.2).
    - `Node: Type` no está declarado como `ENUM` con la lista de tipos válidos →
      un schema con tipo desconocido **carga sin error** y solo revienta al validar
      documentos (`TYPE_NOT_SUPPORTED`). Debe fallar al cargar.
    - Actualizar también `MetaTemplateSchemaProvider` y añadir los tipos nuevos del
      punto 5 a ambos.

## Templates (`dev.stxt.template`)

13. **Cardinalidades malformadas** (STXT-TEMPLATE-SPEC 7.1; vscode 0.4.2) —
    `ChildLineParser`:
    - `(-2)` se acepta como min=max=-2; los números deben ser enteros no negativos.
    - `(1,2,3)` descarta el tercer valor en silencio; debe ser `INVALID_CHILD_COUNT`.
    - `(x+)` / `(x-)` lanzan `NumberFormatException` pelada en vez de
      `INVALID_CHILD_COUNT`.
    - Falta `MIN_GREATER_THAN_MAX` en `(min,max)`.

14. **`[values]` solo para ENUM y ENUM sin valores** (STXT-TEMPLATE-SPEC 9, 13.7;
    vscode 0.3.0/0.4.1) — `TemplateParser` acepta lista de valores en cualquier
    tipo y no exige lista no vacía cuando el tipo es `ENUM`
    (`VALUES_EMPTY_FOR_ENUM`). El lado schema sí lo hace (`SchemaParser`).

15. **`Description` del template ignorada** (STXT-TEMPLATE-SPEC 12; vscode
    0.3.0/0.4.2) — `TemplateParser` no parsea el nodo `Description` (ni
    `SchemaParser` la de los schemas): las descripciones no llegan a
    `NodeDefinition` y no se detectan entradas duplicadas
    (`DESCRIPTION_ALREADY_DEFINED`).

16. **Cross-namespace y referencias ignoran cosas en silencio** (STXT-TEMPLATE-SPEC
    14.13/14.15) — mismo pendiente que stxt-vscode (su PENDIENTES.md, punto 7): en
    líneas cross-namespace los `[a, b]` y los hijos se ignoran sin error; los hijos
    de una referencia `@Nodo` también. Además, en Java `type.startsWith("@")`
    (`TemplateParser`, rama de nodo repetido) lanza NPE si el nodo repetido no
    lleva tipo. Coordinar la solución con stxt-vscode.

## Decisiones de diseño (no estrictamente spec)

17. **Modelo de errores fail-fast vs multi-error** — el `Validator` Java lanza la
    primera excepción; el TS devuelve `ValidationException[]` y `parseResult()`
    acumula errores sin abortar. Para una librería de parseo el fail-fast puede
    ser aceptable; decidir si se porta el modelo multi-error (útil para
    herramientas construidas encima).
