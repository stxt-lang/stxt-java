# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Qué es este proyecto

Parser de referencia en Java del lenguaje **STXT (Semantic Text)**: un formato textual jerárquico
y semántico, "Human-First", trivial de parsear y diseñado con la seguridad del parseo como prioridad
(inmune a expansión de entidades, ejecución de código, inyección en bloques literales, ataques
homográficos).

La especificación normativa **no vive en este repositorio**: está en el proyecto hermano
`stxt-web` (`../stxt-web` respecto a este directorio), escrita en el propio STXT. La versión
canónica es la española en `es/`, con espejo en inglés en `en/`:

- `../stxt-web/es/stxt-core-ref.stxt` — sintaxis base del lenguaje (la fuente de verdad).
- `../stxt-web/es/stxt-schema-ref.stxt` — capa de validación semántica vía `@stxt.schema`.
- `../stxt-web/es/stxt-template-ref.stxt` — `@stxt.template`, azúcar sintáctico equivalente a un schema.

Al tocar comportamiento del parser o de la validación, **contrasta siempre con la spec**: usa
palabras clave RFC 2119 (DEBE / NO DEBE / DEBERÍA / PUEDE) y casos normativos numerados por sección.

Desde este proyecto, `../stxt-web` y `../stxt-vscode` son **también de escritura**: si al trabajar
aquí hay que corregir la spec o la implementación TypeScript, se puede hacer directamente en esos
repositorios (cada uno con su propio git). Eso no cambia el orden de autoridad: si el parser y la
spec discrepan, **la spec manda**; el punto de partida es ajustar el parser, y cambiar la spec solo
cuando se decida conscientemente que es ella la que está mal.

### Implementación hermana: `../stxt-vscode/stxt`

Existe otra implementación completa del lenguaje en el repo `stxt-vscode` (el proyecto vive en la
subcarpeta `stxt/` de ese repo). Contiene dos cosas en `src/` (todo TypeScript):

1. Una implementación del lenguaje (parser, schemas, templates, tipos, writer) en `src/core`,
   `src/schema`, `src/template`, `src/runtime`, `src/processors`, `src/exceptions` — sin
   dependencia de `vscode`. Originalmente fue un port casi literal de este parser Java, con la
   misma arquitectura (pila de nodos, `Observer`/`Validator`, `Schema`/`NodeDefinition`/
   `ChildDefinition`, `TypeRegistry`, meta-schemas bootstrap).
2. La extensión de VS Code que la consume, en `src/extension.ts` + `src/extension/`
   (diagnósticos, semantic tokens, hover, autocompletado, formateo).

**Importante:** esa implementación TypeScript está hoy **más alineada con la spec que este parser
Java** — pasó un repaso de conformidad contra las specs (2026-07) cuyos ajustes se aplicaron allí
(versiones 0.4.3/0.4.4) pero aún no se han portado aquí: **normalizar stxt-java respecto a la spec
es trabajo pendiente**, y el `PENDIENTES.md` de este repositorio es la lista viva de esos
desajustes (consultarla antes de trabajar en conformidad y mantenerla al día). El `PENDIENTES.md`
de stxt-vscode lista los desajustes spec ↔ implementación que le
quedan, y su `CLAUDE.md` documenta su arquitectura en detalle. Ante una ambigüedad de
comportamiento, el orden de autoridad es: **spec (`../stxt-web`) → parser TypeScript
(`../stxt-vscode/stxt`) → este parser Java**.

Existe también `../stxt-cms` (el CMS que convierte `stxt-web` en un portal HTML); **no es
relevante para este proyecto**.

## Comandos

Build con Maven (Java 17). No hay wrapper `mvnw`; usa `mvn` del sistema.

```bash
mvn compile                  # compilar src/main
mvn test                     # compilar + ejecutar toda la suite JUnit 5
mvn -o test                  # offline, si las deps ya están en ~/.m2
mvn package                  # genera target/stxt-parser-0.1.0.jar

# Un solo test o método (surefire):
mvn test -Dtest=ParserTest
mvn test -Dtest=ParserTest#nombreDelMetodo
mvn test -Dtest='dev.stxt.core.*Test'
```

Nota: Jackson (databind, jsr310, parameter-names) es dependencia **de test únicamente** — se usa para
comparar el árbol parseado contra JSON de referencia en `src/test/resources/*_json/`. El parser de
producción (`src/main`) no depende de Jackson.

## Arquitectura

El flujo es **parseo sintáctico → árbol de `Node` → validación semántica opcional vía hooks**. El
núcleo no conoce los schemas: la validación es una capa desacoplada que se engancha al parser.

### Núcleo de parseo (`dev.stxt`)

- [Parser.java](src/main/java/dev/stxt/Parser.java) — motor línea a línea. Mantiene una pila
  (`ArrayDeque<Node>`) cuyo tamaño es el nivel actual. Por cada línea: calcula indentación, cierra
  nodos hasta el nivel correspondiente (`closeToLevel`, que adjunta cada nodo cerrado a su padre o a
  la lista de documentos raíz), y abre el nodo nuevo. Distingue nodo INLINE (`:`) de bloque de texto
  BLOCK (`>>`) por la primera posición de cada token. Un documento puede tener **varios nodos raíz**.
- [Node.java](src/main/java/dev/stxt/Node.java) — nodo inmutable del árbol. Guarda nombre original,
  `normalizedName` (nombre canónico para igualdad/búsqueda), namespace, valor inline o líneas de texto,
  e hijos en orden de aparición. La igualdad lógica entre nodos es por nombre canónico.
- [LineIndentParser.java](src/main/java/dev/stxt/LineIndentParser.java) — calcula el nivel de
  indentación (espacios múltiplos de 4, tabs, o mezcla por columnas) y separa indentación del contenido.
  Aquí viven las reglas de comentarios (`#`) y de continuación de bloques `>>`.
- [NameNamespaceParser.java](src/main/java/dev/stxt/NameNamespaceParser.java) /
  [NamespaceValidator.java](src/main/java/dev/stxt/NamespaceValidator.java) — extraen y validan el
  namespace `(a.b.c)`. Los namespaces se restringen a ASCII y se normalizan a minúsculas durante el
  parseo; hay herencia del padre pero **no herencia lateral entre nodos raíz**.
- [utils/StringUtils.java](src/main/java/dev/stxt/utils/StringUtils.java) — `normalize` (NFKD,
  minúsculas, sin diacríticos, `[^a-z0-9]→-`) produce el nombre canónico; `compactSpaces` el nombre
  mostrable. Toda comparación de identidad estructural pasa por `normalize`.

### Hooks de proceso (`dev.stxt.processors`)

El parser no valida por sí mismo. Expone dos puntos de extensión que se registran con
`registerObserver` / `registerValidator`:

- `Observer` — `onCreate(node)` al abrir un nodo, `onFinish(node)` al cerrarlo.
- `Validator` — `validate(node)` al cerrar un nodo. Permite validar en streaming sin esperar al EOF.

### Capa de schema y tipos (`dev.stxt.schema`)

- [SchemaValidator.java](src/main/java/dev/stxt/schema/SchemaValidator.java) — `Validator` que, por
  cada nodo, resuelve el `Schema` de su namespace vía un `SchemaProvider`, comprueba que el nodo existe
  en el schema, valida su tipo (`validateValue`) y las cardinalidades min/max de sus hijos (`validateCount`).
- `SchemaProvider` — interfaz `getSchema(namespace) → Schema`. Implementaciones componibles:
  - [SchemaProviderResources](src/main/java/dev/stxt/schema/SchemaProviderResources.java) — carga un
    documento `@stxt.schema` desde un `ResourcesLoader` y lo transforma en `Schema`.
  - [TemplateSchemaProvider](src/main/java/dev/stxt/template/TemplateSchemaProvider.java) — equivalente
    para `@stxt.template`, transformando la sintaxis de template al mismo `Schema`.
  - [SchemaProviderCache](src/main/java/dev/stxt/schema/SchemaProviderCache.java) — envuelve una lista
    de providers, prueba en orden y cachea.
  - [SchemaProviderMeta](src/main/java/dev/stxt/schema/SchemaProviderMeta.java) — **meta-schema
    bootstrap**: define en código el schema del propio lenguaje de schemas (`@stxt.schema`), para que
    un schema cargado pueda validarse a sí mismo. Hay un meta-template análogo
    (`MetaTemplateSchemaProvider`).
- [TypeRegistry.java](src/main/java/dev/stxt/schema/TypeRegistry.java) — registro estático de tipos de
  valor. Cada tipo vive en `dev.stxt.schema.type` como singleton `INSTANCE` que implementa `Type`
  (`getName()` + `validate(NodeDefinition, Node)`): TEXT, BLOCK, INLINE, BOOLEAN, URL, INTEGER, NATURAL,
  NUMBER, DATE, TIME, TIMESTAMP, UUID, EMAIL, HEXADECIMAL, BINARY, BASE64, GROUP, ENUM, MARKDOWN.
  **Añadir un tipo nuevo = nueva clase `Type` + `register(...)` en `TypeRegistry`.**

### Fachada de uso (`dev.stxt.runtime.STXT`)

[STXT.java](src/main/java/dev/stxt/runtime/STXT.java) es el punto de entrada conveniente:
`STXT.parser(loader)` devuelve un `Parser` ya configurado con un `SchemaValidator` que combina schemas
y templates con caché. `STXT.rawParser()` da un parser sin validación (solo sintaxis).

### Carga de recursos (`dev.stxt.resources`)

`ResourcesLoader.retrieve(namespace, resource)` abstrae de dónde vienen los schemas.
[ResourcesLoaderDirectory](src/main/java/dev/stxt/resources/ResourcesLoaderDirectory.java) los busca en
`<dir>/<namespace>/<resource>.stxt` — de ahí el layout `@stxt.schema/<ns>.stxt` y
`@stxt.template/<ns>.stxt` en los recursos de test.

## Tests y fixtures

JUnit 5, organizados por capa bajo `src/test/java/dev/stxt/{core,schema,template,...}` y helpers en
`src/test/java/test/`. Las suites `ParserAll*Test` recorren directorios de fixtures completos.

Los fixtures en `src/test/resources/` siguen una convención por carpetas; los tests cargan rutas vía
`test.FileTestLoction.getFile(dir)`:

- `docs/` — documentos STXT válidos; `docs_json/` el árbol esperado en JSON; `docs_txt/` el render texto esperado.
- `error_docs/`, `error_schema/`, `error_template/` — entradas que **deben** fallar (con su contraparte `*_json/`).
- `@stxt.schema/<ns>.stxt` y `@stxt.template/<ns>.stxt` — schemas/templates indexados por namespace.
- `schema_json/<ns>.json` — el `Schema` resultante esperado, tanto si viene de un schema como de un
  template (por eso `TemplateToSchema*Test` y `SchemaReaderTestAll` comparten esta carpeta).
- `docs_raw/` — **reservada, aún sin tests**: documentos sin namespace (parseo puro, sin validación de
  schema). No borrarla; es el sitio donde añadir esa cobertura cuando toque.

Al añadir un caso, respeta el emparejamiento documento ↔ JSON/TXT esperado y la carpeta `error_*` si la entrada debe rechazarse.

## Excepciones

Jerarquía en `dev.stxt.exceptions` bajo `STXTException` (runtime). Usa la específica según la fase:
`ParseException` (sintaxis), `ValidationException` (schema/tipo/cardinalidad), `SchemaException`
(schema mal formado), `ResourceNotFoundException`, `STXTIOException`. Todas llevan un **código de error
en MAYÚSCULAS** (p. ej. `INVALID_LINE`, `NODE_NOT_EXIST_IN_SCHEMA`, `SCHEMA_NOT_FOUND`); mantén esa
convención al lanzar errores nuevos, porque los tests de error a menudo dependen de ellos.
