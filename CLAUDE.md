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

Desde este proyecto, `../stxt-web`, `../stxt-js` y `../stxt-vscode` son **también de escritura**: si al trabajar
aquí hay que corregir la spec o la implementación TypeScript, se puede hacer directamente en esos
repositorios (cada uno con su propio git). Eso no cambia el orden de autoridad: si el parser y la
spec discrepan, **la spec manda**; el punto de partida es ajustar el parser, y cambiar la spec solo
cuando se decida conscientemente que es ella la que está mal.

### Implementación hermana: `../stxt-js`

La otra implementación completa del lenguaje (parser, schemas, templates, tipos, writer, todo
TypeScript) vive en el repo `stxt-js` y se publica en npm como **`@stxt-lang/core`**. Originalmente
fue un port casi literal de este parser Java, con la misma arquitectura (pila de nodos,
`Observer`/`Validator`, `Schema`/`NodeDefinition`/`ChildDefinition`, `TypeRegistry`, meta-schemas
bootstrap), así que sus clases siguen teniendo casi siempre un homólogo directo aquí.

`../stxt-vscode` es **solo la extensión de VS Code** (diagnósticos, semantic tokens, hover,
autocompletado, formateo): desde su versión 0.5.1 borró su copia del lenguaje y consume
`@stxt-lang/core` como dependencia npm normal. No busques ahí el parser.

**Importante:** esa implementación TypeScript pasó durante 2026-07 un repaso de conformidad contra
las specs (versiones 0.4.3 a 0.5.1) cuyos ajustes se fueron portando aquí. El repaso de
conformidad de este repositorio se cerró el 2026-07-28 y su historial vive en el `git log`.
stxt-js y stxt-vscode no tienen `PENDIENTES.md` propio: la referencia de lo que les queda es su
`CHANGELOG.md`; sus `CLAUDE.md` documentan su arquitectura en detalle. Ante una ambigüedad de
comportamiento, el orden de autoridad es: **spec (`../stxt-web`) → parser TypeScript
(`../stxt-js`) → este parser Java**.

### `PENDIENTES.md`

Es el único sitio donde se acumula trabajo pendiente de este repositorio, con un formato fijo: lista
numerada, un punto por tarea, cada punto se borra al resolverse y **el fichero se elimina cuando
queda vacío**. No es un fichero permanente: se crea cuando arranca una tanda de trabajo con varios
puntos y desaparece al cerrarla. Ya ha pasado dos veces por ese ciclo — se eliminó al cerrar el
repaso de conformidad (2026-07-28) y se recreó el mismo día, esta vez para **preparar la publicación
en Maven Central**, que es la tanda abierta ahora mismo (ver la sección de más abajo). Si en el
futuro aparece una divergencia nueva con la spec o con stxt-js, va también ahí, no en otro sitio.

### Los demás repositorios hermanos

Ninguno es necesario para trabajar aquí, pero conviene saber qué son para no buscar cosas en el sitio
equivocado:

- `../stxt-cms` — el CMS que convierte `stxt-web` en un portal HTML.
- `../stxt-dev` — el sitio **generado** por stxt-cms (lo que se publica en `stxt.dev`). Es salida de
  build: no se edita a mano. Su dominio es además el que verifica por DNS el `groupId` `dev.stxt`.
- `../stxt-impl` — descripción del parser en **pseudocódigo** neutro de lenguaje (`core/`, `schema/`,
  `template/`, con la guía de estilo en su `README.md`), pensada como plano para portar el lenguaje a
  implementaciones nuevas. Sus ficheros son homólogos de las clases de este repo.
- `../stxt-python` — implementación en Python, hoy solo un esqueleto (README y nada más).
- `../stxt-js-OLD` — la implementación JavaScript anterior, sustituida por `../stxt-js`. No es
  referencia de nada.

## Comandos

Build con Maven (Java 17). No hay wrapper `mvnw`; usa `mvn` del sistema.

```bash
mvn compile                  # compilar src/main
mvn test                     # compilar + ejecutar toda la suite JUnit 5
mvn -o test                  # offline, si las deps ya están en ~/.m2
mvn package                  # genera target/stxt-core-0.5.2.jar

# Un solo test o método (surefire):
mvn test -Dtest=ParserTest
mvn test -Dtest=ParserTest#nombreDelMetodo
mvn test -Dtest='dev.stxt.core.*Test'
```

Nota: Jackson (databind, jsr310, parameter-names) es dependencia **de test únicamente** — se usa para
comparar el árbol parseado contra JSON de referencia en `src/test/resources/*_json/`. El parser de
producción (`src/main`) no depende de Jackson.

## Publicación en Maven Central

El artefacto es **`dev.stxt:stxt-core`**, licencia **MIT**. La versión se mantiene **alineada con
`@stxt-lang/core`** (npm): un mismo número debe significar el mismo comportamiento en las dos
implementaciones, así que subir versión aquí sin un cambio equivalente allí (o al revés) es una
señal de que algo se ha desalineado. El jar publicado **no arrastra dependencias** (todo es
`test`), apunta a Java 17 y declara `Automatic-Module-Name: dev.stxt`.

El `groupId` `dev.stxt` se verifica en el Central Portal con un **registro TXT en el DNS de
`stxt.dev`** (el dominio es de la organización); ya está registrado y validado. La publicación es
**manual**, no hay CI: el release se hace desde una máquina que tenga `../stxt-web` al lado, para
que las suites de corpus se ejecuten de verdad en vez de saltarse.

Esta sección describe el flujo previsto; **todavía no se ha publicado ninguna versión**. Lo que falta
para la primera (clave GPG, `settings.xml`, plugin de publicación, ensayo) está en
[PENDIENTES.md](PENDIENTES.md).

```bash
mvn package                  # build normal, sin firmar
mvn -Prelease verify         # + firma GPG de los 4 ficheros (pom y 3 jars)
mvn -Prelease deploy         # publica (requiere ~/.m2/settings.xml con el token del Portal)
```

El perfil `release` aísla la firma GPG para que el build diario no pida clave. Al preparar una
versión hay que tocar, además del código:

1. `<version>` en [pom.xml](pom.xml) y `project.build.outputTimestamp` (fija las fechas dentro del
   jar; si no se actualiza, el build deja de ser reproducible respecto a esa release).
2. La versión en los dos snippets de instalación del [README.md](README.md) (Maven y Gradle).
3. Una entrada nueva en [CHANGELOG.md](CHANGELOG.md), formato Keep a Changelog como stxt-vscode.

El README es **la portada del artefacto en Central y en javadoc.io**, no un fichero interno: sus
ejemplos están compilados y ejecutados contra el parser real, y deben seguir estándolo si se tocan.

Una release publicada en Central es **inmutable**: no se puede borrar ni sustituir, solo publicar
una versión nueva encima.

## Arquitectura

El flujo es **parseo sintáctico → árbol de `Node` → validación semántica opcional vía hooks**. El
núcleo no conoce los schemas: la validación es una capa desacoplada que se engancha al parser.

### Núcleo de parseo (`dev.stxt`)

- [Parser.java](src/main/java/dev/stxt/Parser.java) — motor línea a línea. Mantiene una pila
  (`ArrayDeque<Node>`) cuyo tamaño es el nivel actual. Por cada línea: calcula indentación, cierra
  nodos hasta el nivel correspondiente (`closeToLevel`, que adjunta cada nodo cerrado a su padre o a
  la lista de documentos raíz), y abre el nodo nuevo. Distingue nodo INLINE (`:`) de bloque de texto
  BLOCK (`>>`) por la primera posición de cada token. Un documento puede tener **varios nodos raíz**.
- [Node.java](src/main/java/dev/stxt/Node.java) — nodo del árbol. **Mutable durante el parseo**
  (`addChild`/`addTextLine` son públicos y no existe `freeze()`); una vez cerrado el documento se
  trata como de solo lectura, que es lo que documenta el README. Guarda nombre original,
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

Aparte de los fixtures propios, `src/test/java/dev/stxt/corpus/` valida contra el **corpus real de
`../stxt-web`** (`@TestFactory` con un test dinámico por fichero): que carguen todos los schemas y
templates de `.stxt/**`, que parseen y validen todos los documentos de `docs/`, `es/` y `en/`, que
el schema y el template de un mismo namespace validen igual, y que la salida de `NodeWriter` vuelva
a parsear al mismo árbol en los dos estilos de indentación. El corpus **no se copia** a este
repositorio: es la fuente normativa y los tests deben fallar cuando el parser se separa de los
documentos reales. Si `../stxt-web` no está, esas suites se saltan (`Assumptions.assumeTrue`) en vez
de fallar; `STXT_WEB=/ruta` fuerza la localización. El puente es `test.Corpus.CorpusLoader`, un
`ResourcesLoader` en memoria que indexa por el namespace que declara cada fichero.

Los fixtures en `src/test/resources/` siguen una convención por carpetas; los tests cargan rutas vía
`test.FileTestLoction.getFile(dir)`:

- `docs/` — documentos STXT válidos; `docs_json/` el árbol esperado en JSON; `docs_txt/` el render texto esperado.
- `error_docs/`, `error_schema/`, `error_template/` — entradas que **deben** fallar (con su contraparte `*_json/`).
- `@stxt.schema/<ns>.stxt` y `@stxt.template/<ns>.stxt` — schemas/templates indexados por namespace.
- `schema_json/<ns>.json` — el `Schema` resultante esperado, tanto si viene de un schema como de un
  template (por eso `TemplateToSchema*Test` y `SchemaReaderTestAll` comparten esta carpeta).
- `docs_raw/` — documentos sin namespace (parseo puro con `STXT.rawParser()`, sin validación de
  schema); `docs_raw_json/` el árbol esperado y `docs_raw_txt/` el render texto. Los recorre
  `ParserAllRawDocsTest`.

Al añadir un caso, respeta el emparejamiento documento ↔ JSON/TXT esperado y la carpeta `error_*` si la entrada debe rechazarse.

## Excepciones

Jerarquía en `dev.stxt.exceptions` bajo `STXTException` (runtime). Usa la específica según la fase:
`ParseException` (sintaxis), `ValidationException` (schema/tipo/cardinalidad), `SchemaException`
(schema mal formado), `ResourceNotFoundException`, `STXTIOException`. Todas llevan un **código de error
en MAYÚSCULAS** (p. ej. `INVALID_LINE`, `NODE_NOT_EXIST_IN_SCHEMA`, `SCHEMA_NOT_FOUND`); mantén esa
convención al lanzar errores nuevos, porque los tests de error a menudo dependen de ellos.
