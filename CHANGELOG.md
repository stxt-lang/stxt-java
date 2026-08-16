# Change Log

All notable changes to `dev.stxt:stxt-core` are documented in this file.

Check [Keep a Changelog](http://keepachangelog.com/) for recommendations on how to structure this file.

## [Unreleased]

## [0.7.0]

**Breaking: a new node model.** `Node` is now a sealed abstract class with two forms,
`InlineNode` (`Name: value`) and `TextNode` (`Name >>`), designed from real use of the API. Only
the in-memory model changes: the language, STXT-TREE-SPEC and every error code are the same.

- **Two forms.** `InlineNode` has `getValue()`/`setValue()`, children and the factories; `TextNode`
  has `getTextLines()`, `setText()`, `setTextLines()`, `addTextLine()`, `clearText()` and no
  children. `Node` keeps what is common: name, canonical name, namespaces, line, parent, child
  lookups (`getChildren()` is empty for a text node, so tree walks need no distinction),
  `isTextNode()` and `getText()`. Both concrete classes are `final`.
- **Parent links with integrity.** `getParent()`, `addChild(node)`, `addChild(index, node)`,
  `removeChild(node)` and `detach()`. `addChild` links both ends and refuses a node that already
  has a parent (`NODE_ALREADY_ATTACHED`) or that is an ancestor (`NODE_CYCLE`); `removeChild`
  compares by identity. The parser attaches each node when it opens it, so observers already see
  its parent, effective namespace and level in `onCreate`.
- **Declared vs effective namespace.** A node stores the namespace it *declares*
  (`getDeclaredNamespace()`, `setNamespace()`); `getNamespace()` is the effective one, resolved
  through the parent chain. Changing a declared namespace changes the whole inheriting subtree,
  and so does moving a subtree. `NodeWriter` writes the namespace where it is declared, exactly
  as in the source; the canonical tree is unchanged.
- **Level is derived** from the chain of parents (`getLevel()`, 0 for a root); it is no longer a
  constructor argument.
- **Line is optional and mutable**: `getLine()`/`setLine()`, `Node.NO_LINE` when unknown. The
  parser sets it; code building trees usually does not.
- **Everything is mutable**: `setName()` (revalidated, canonical name recomputed),
  `setNamespace()` (validated), `setValue()`, and the text setters above.
- **Factories** on `InlineNode`: `addInlineNode(name[, value])`, `addInlineNode(name, ns, value)`,
  `addTextNode(name[, text])`, `addTextNode(name, ns, text)`, `addTextNode(name, ns, lines)`. With
  two strings the second one is always the content; the namespace only exists in the
  three-argument forms.
- **Renamed** `getNormalizedName()` → `getCanonicalName()` on `Node`, `NodeDefinition` and
  `ChildDefinition` ("canonical name" is the term of the specifications). The old getters remain
  as `@Deprecated` aliases and will be removed in a later version.
- **Removed**: the `Node` constructors (use `InlineNode`/`TextNode`), the `level` argument, and
  `getValue()`/`getTextLines()`/`addTextLine()` on the base class (use `getText()`, or the concrete
  form). `getTextLines()` and `getChildren()` are now read-only views.
- New `NodeTest` (19 cases); the historical `docs_json/` fixtures are compared through a
  test-side serializer that keeps their pre-0.7 shape.

## [0.6.1]

- `SchemaProvider` contract: providers never throw "not found". `SchemaProviderMeta` and
  `MetaTemplateSchemaProvider` return `null` for any namespace but their own (they used to throw
  `RESOURCE_NOT_FOUND`), `SchemaProviderResources` and `TemplateSchemaProvider` return `null`
  when the `ResourcesLoader` has no such resource, and `SchemaProviderCache` returns `null` when
  no member has the namespace (it used to throw a `NOT_FOUND_SCHEMA` `SchemaException`). The
  only code for "no schema" is now `SCHEMA_NOT_FOUND`, reported once by `SchemaValidator` as a
  finding; through the `STXT.parser(loader)` facade an unknown namespace used to surface as
  `VALIDATION_ERROR`. New `SchemaProviderContractTest`.

- Tests: the `stxt-web` corpus is now mandatory. `Corpus.findStxtWeb()` throws when the sibling
  project (or `STXT_WEB`) cannot be found, so the `dev.stxt.corpus` suites fail instead of being
  skipped through `Assumptions`. A silently skipped corpus can no longer hide a broken locator.

## [0.6.0]

Adds `dev.stxt.discovery`, the reference port of STXT-DISCOVERY-SPEC, aligned with the
`src/discovery/` package `stxt-js` introduced in its own 0.6.0.

- `DiscoveryResolver` builds the resolution chain of a document (project ascent, user level,
  system level, or the `STXT_PATH` override), loads every `.stxt` definition of every level and
  applies the per-namespace precedence, with a per-directory cache (`clearCache()` when files may
  have changed).
- `DiscoveryResult` implements `SchemaProvider` and additionally exposes `getDefinition`,
  `getActiveDefinitions`, `getChain` and `getErrors`, so the provenance of every active definition
  is inspectable.
- `DiscoveryError` reports the four resolution errors of spec section 8
  (`DISCOVERY_DUPLICATE_NAMESPACE`, `DISCOVERY_NOT_PARSEABLE`, `DISCOVERY_NOT_A_DEFINITION`,
  `DISCOVERY_INVALID_DEFINITION`) without aborting the rest of the load.
- `DiscoveryEnvironment`/`SystemDiscoveryEnvironment` isolate `STXT_PATH` and the user/system
  directories; file access itself uses `java.nio.file.Path` directly, since (unlike `stxt-js`) this
  port has no need to run inside a browser or an editor host.
- 22 new tests in `DiscoveryResolverTest`, ported from `stxt-js`'s `discovery.test.ts`.

## [0.5.4]

Conformance release aligned with the 2026-08-09 pseudocode audit.

- Node names are validated after NFC normalization, so decomposed and precomposed Unicode
  spellings are treated equivalently.
- Schema `Node` and `Child` values now enforce the full STXT node-name grammar.
- Template `Structure` lines must use the inline `:` form; core BLOCK (`>>`) lines are rejected.
- The corpus equivalence test now loads templates from `examples/definitions/templates`, their
  current location in `stxt-web`.

## [0.5.3]

A documentation release: the published javadoc is now complete and in one language. No parser,
schema or template behaviour changed.

- All source comments and javadoc are now written in **English**, so the whole project is in one
  language (the README, the license and the error messages already were). This covers `src/main` and
  `src/test`: class descriptions, `@param`/`@return`/`@throws` tags, inline comments and the
  references to the normative specs.
- **Every documented member now has a main description.** 100 doc comments carried block tags only,
  with no summary sentence, which left their row empty in the *Method Summary* tables of the
  generated documentation — the first thing anyone reading the API on javadoc.io sees. Simple
  accessors use the `{@return ...}` inline tag; the rest gained a summary line. `mvn javadoc:jar`
  went from **106 warnings to none**.
- `Parser` and `ParseResult` now declare their no-argument constructor explicitly, with javadoc.
  The signature is the same one the compiler generated implicitly, so the API is unchanged.
- `NamespaceValidator`, `SchemaParser`, `TemplateParser` and `TypeRegistry` now have a **private**
  constructor, like the other 26 utility classes of the project. They only expose static members, so
  this removes an implicit public constructor that could only ever build useless instances. It is
  the single API-visible change of this release.
- The one exception message that was still in Spanish is now English: `NOT_STXT_SCHEMA` reads
  `Expected schema(...) but got ...`. The error code is unchanged.
- Fixed the javadoc of `Parser.parse(String)` and `Parser.parseResult(String)`: each had two
  consecutive doc comments, so the description of the fail-fast and multi-error modes never reached
  the generated documentation.
- The renames that came with the translation are local variables and test method names only:
  `NameNamespaceParser` now uses `openIndex`/`closeIndex`, and the corpus `@TestFactory` methods are
  named in English. The Spanish text kept on purpose is test data — node names with accents
  (`Título`, `Año`, `café`) exercise the canonical-name rules of STXT-SPEC 4.3.

## [0.5.2]

- First release published to Maven Central, as `dev.stxt:stxt-core`. The artifact was
  `dev.stxt:stxt-parser:0.1.0` while it was only built locally; the name now mirrors the npm package
  `@stxt-lang/core`, and the version is aligned with it so that the same number means the same
  behaviour in both implementations.
- The published jar carries **no runtime dependencies**: Jackson and JUnit are test-scoped. It targets
  **Java 17** and declares the automatic module name `dev.stxt`.
- The conformance review against the STXT specs (`stxt-web`, versions 0.4.3 to 0.5.1) is closed: this
  parser and `@stxt-lang/core` agree on syntax, schemas, templates, value types and error codes. The
  test suite includes the real `stxt-web` corpus, so a divergence from the normative documents breaks
  the build.
- No parser, schema or template behaviour changed in this release; it is a packaging milestone.
