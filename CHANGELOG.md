# Change Log

All notable changes to `dev.stxt:stxt-core` are documented in this file.

Check [Keep a Changelog](http://keepachangelog.com/) for recommendations on how to structure this file.

## [0.14.0] - 2026-08-26

Same number and scope as `@stxt-lang/core` and `stxt` (Python) 0.14.0: the parser limits of
STXT-SPEC §11.2 in the three ports, plus the streaming API.

### Added

- Parser limits (STXT-SPEC §11.2): the parser aborts on inputs that exceed its nesting depth
  (default 100 levels), line length (default 10 000 characters) or total input size (default
  10 000 000 characters). Each limit is configurable per parser (`setMaxNesting`,
  `setMaxLineLength`, `setMaxInputSize`; -1 disables one) and its error is a `LimitException`
  (codes `LIMIT_NESTING_EXCEEDED`, `LIMIT_LINE_LENGTH_EXCEEDED`, `LIMIT_INPUT_SIZE_EXCEEDED`),
  in every case the last one emitted: after it, no further input is processed and the nodes
  still open are not closed.
- `StreamObserver` (`dev.stxt.processors`), registered with `registerStreamObserver`: notified
  with each completed root node (`onRootNode`) and every error (`onError`), in every mode.
- `Parser.parseStream(Reader)` and `parseStream(Iterable<String>)`: streaming mode. The input
  is read line by line and nothing is retained — no nodes, no errors —; the results reach the
  program only through the registered `StreamObserver`s, so memory holds one root tree at a
  time. Made for files that do not fit in memory.

### Changed

- A document that exceeds a default limit — deeper than 100 levels, a line longer than 10 000
  characters, or more than 10 000 000 characters in total — no longer parses unless the limit
  is raised or disabled. This is the language change of the 0.14.0 cycle (STXT-SPEC §11.2,
  `Last modif: 2026-08-26`).

## [0.13.0] - 2026-08-23

Same number and scope as `@stxt-lang/core` and `stxt` (Python) 0.13.0: the writing operations of
STXT-TREE-SPEC §11–12, now normative, in the three ports.

### Added

- `Formatter` (`dev.stxt.runtime`): the reformatting of STXT-TREE-SPEC §12, a replica of the
  TypeScript `Formatter` of `@stxt-lang/core` 0.11.1. `Formatter.format(text, style)` returns a
  `FormatResult(text, errors)`: the document rewritten line by line —node lines in canonical
  form, block lines at the level of the block, comments and blank lines kept with their
  indentation units converted— plus the syntax errors found; CRLF and the final newline are
  kept, an initial BOM is removed.

### Changed

- `NodeWriter` writes the canonical text form of STXT-TREE-SPEC §11 (2026-08-23): the
  namespace is declared only where it changes from the parent's — on a root when not empty, on
  a child when it differs — wherever the source declared it. A child repeating its parent's
  namespace used to come out with it; the tree it re-parses to is the same.

### Fixed

- `TemplateParser`: a parse error inside a `Structure >>` or `Description >>` block (a line
  without `:`, a bad indentation) is now reported at the line of the template, like the other
  template errors and like the TypeScript and Python ports (`stxt-impl` `template_parser`: the
  line is shifted by the block offset). It was reported at the line within the block.
- Tests: `ConformanceKitTest` runs the `validate`, `validate-error`, `definition-error` and
  `discovery` categories of the conformance kit (kit 1.0, 276 cases in all).

## [0.12.0] - 2026-08-22

Same number and scope as `@stxt-lang/core` and `stxt` (Python) 0.12.0: the three ports are level again
(the 0.11.1 of `@stxt-lang/core` was the JS-only `Formatter`).

### Changed

- An indented first line is now a parse error, `INDENTATION_LEVEL_NOT_VALID` (STXT-SPEC §8.3,
  clarified on 2026-08-22): with no open node the reference level is -1, so the first node or
  comment of the document, and the first line after every node has been closed, must be at
  level 0. Until now `\tRoot: x` parsed as a root. As a consequence, a level-1 line after a
  comment closed a root `>>` block is `INDENTATION_LEVEL_NOT_VALID` instead of `INVALID_LINE`.
  Conformance kit cases `parse/indentation-first-line`, `parse/comment-first-line` and
  `parse/comment-closes-root-block-text-after`.
- Tests: `ConformanceKitTest` runs `stxt-lang/conformance/manifest.json` (the conformance kit,
  `tree` and `parse-error` categories) and replaces `CanonicalTreeTest`.

## [0.11.0] - 2026-08-21

The preview of 1.0: everything 1.0 will ship, published first as a 0.x so that the consumers can
move to it and anything left can still be fixed. The public API is the one 1.0 will freeze for
the whole 1.x line. Same scope as `@stxt-lang/core` and `stxt` (Python) 0.11.0.

### Removed

- `Node.getNormalizedName()`, `NodeDefinition.getNormalizedName()` and
  `ChildDefinition.getNormalizedName()`, deprecated since 0.7.0: use `getCanonicalName()`.
- `dev.stxt.runtime.ConditionalValidator`, deprecated since 0.8.0: `SchemaValidator` already
  lets the nodes without a namespace through (STXT-SCHEMA-SPEC 5), so register it directly.
  `STXT.parser(loader)` does so now.

### Added (API parity audit against `@stxt-lang/core` and `stxt`)

- `Observer` gets the two streaming callbacks the other ports had, `onComment(int, String)` and
  `onTextLine(TextNode, int, String, LineIndent)`, and `onCreate` receives the source line:
  `onCreate(Node, String)`. **Breaking for implementors of `Observer`** (the interface gains
  two abstract methods and changes one signature); the only one in the ecosystem was updated.
- `LineIndent` and `LineIndentParser.parseLine` are public, with the fields of the pseudocode
  (`indentLevel`, `lineWithoutIndent`, `isComment`, `isBlock`, `contentStart`) and `isEmpty()`.
  `parseLine` never returns `null` any more: a comment comes back with `isComment`, an empty line
  with `isEmpty()`.
- In-memory providers, as in the other ports: `SchemaProviderMemory` (`addSchema`),
  `TemplateSchemaProviderMemory` (`addTemplate`) and `dev.stxt.runtime.UnifiedSchemaProvider`
  (`addFile`, either kind), all with `clear()` and `getAllSchemas()`, and all refusing to register
  a definition that does not validate against its meta-schema.
- Discovery over an injectable file system: `DiscoveryFileSystem` (`isDirectory`, `listDirectory`,
  `readFile`), `DiscoveryEntry`, `NioDiscoveryFileSystem` (the default) and the constructor
  `DiscoveryResolver(fs, env, maxAscent)`; `DiscoveryLevel` and the `DiscoveryResult` constructor
  are public, so a result can be built from levels held in memory. `STXT_DIR` and
  `STXT_EXTENSION` are public constants of the resolver.
- `TreeJson.toCanonicalTree(List<Node>)`: the canonical tree of STXT-TREE-SPEC as plain
  `java.util` maps and lists, ready for any JSON library; `toCanonicalJson` now serializes that
  tree (same output as before).
- `NodeDefinition(name, type, line, description)`; the three-argument constructor and
  `setDescription` stay.
- `Schema.getDescription()` and the constructor `Schema(namespace, line, description)`;
  `SchemaParser` fills it from the `Description` of the root (STXT-SCHEMA-SPEC 6.1). The
  two-argument constructor stays.
- `Constants.SEP_TEXT_NODE = ">>"`.

### Fixed

- `LineIndent.indentLength` of a text line of a block was the index of the last indentation
  character, one less than the number of characters the indentation took (the comment and node
  cases were right). Fixed in the pseudocode and in the three ports at once, and the field is
  **renamed `contentStart`** (the index where the content starts) so that the consumers that
  added `+ 1` to compensate stop compiling instead of silently drifting by one.

### Tests

- `Constants.SPEC_VERSION` is compared against the `Metadata/Version` that STXT-SPEC declares
  in `stxt-web/es/stxt-core-ref.stxt`, not only against a literal.

## [0.10.0] - 2026-08-21

The 0.10.0 cycle, decided on 2026-08-21 and made in the specifications first
(`Last modif: 2026-08-21`), then in `stxt-impl` and in the three ports at once. `@stxt-lang/core`
and `stxt` (Python) ship the same scope as 0.10.0.

- **Blanks in binary values** (STXT-SCHEMA-SPEC 9.5). `HEXADECIMAL`, `BINARY` and `BASE64` remove
  every space (U+0020) and tab (U+0009), wherever it is, before applying their grammar, in both
  the inline and the block form (the block lines are joined first). `DE AD BE EF`, `DE\tAD`,
  `1010 1010`, `SG Vs bG 8=` and Base64 wrapped at 76 columns now validate; `DE:AD`, `DE-AD`, a
  value that is empty after removing the blanks, and Base64 with characters outside the standard
  alphabet are `INVALID_VALUE`. Before, only the leading and trailing blanks of each block line
  were ignored.
- **Empty ENUM value** is an error (STXT-SCHEMA-SPEC 7.2, condition 14; STXT-TEMPLATE-SPEC 14.14).
  An empty `Value:` under `Values:` is `VALUE_EMPTY` at the line of that `Value`; an empty item in
  the `[...]` list of a template (`[a, , b]`, `[a, b,]`) is `VALUE_EMPTY` at the line of the
  Structure line. A whole empty list (`[]`) stays `VALUES_REQUIRED`. Before, empty values were
  silently skipped.
- **Message framing** (breaking for anyone parsing messages). `getMessage()` of every
  `ParseException`/`ValidationException` returns only the description, exactly as built (for
  example `Level of indent incorrect: 2`), with no code and no line: the `Error at line: N, `
  prefix is gone. The frame moved to `toString()`: `[CODE] line N: message` for
  `ParseException`/`ValidationException` and `[CODE] message` for the rest of `STXTException`
  (before, `toString()` was `ClassName[CODE]: message`). `getCode()` and `getLine()` are unchanged.
- **`Constants.SPEC_VERSION`** (`"1.0"`): the version of the five STXT specifications this library
  implements, distinct from the artifact version. `dev.stxt.Constants` is public from now on.

## [0.9.1] - 2026-08-21

The last items of the STXT-SCHEMA-SPEC review before 1.0, decided on 2026-08-21 and made in the
specifications first (`Last modif: 2026-08-21`), then in `stxt-impl` and in the three ports at
once. `@stxt-lang/core` and `stxt` (Python) ship the same scope as 0.9.1.

- **Error codes renamed** to the normative annex (STXT-SPEC 11.1, STXT-SCHEMA-SPEC 13.1,
  STXT-TEMPLATE-SPEC 14.1), frozen from 1.0 on. Codes are part of the conformance surface, so
  this is a behavior change for anyone matching on them. Old -> new:
  `MIXED_INDENTATION` -> `INDENTATION_MIXED`, `INVALID_NUMBER_SPACES` -> `INDENTATION_SPACES_NOT_VALID`,
  `INLINE_VALUE_NOT_VALID` -> `BLOCK_VALUE_NOT_ALLOWED`, `NOT_ALLOWED_TEXT` -> `BLOCK_FORM_NOT_ALLOWED`,
  `NOT_ALLOWED_CHILDREN_TEXT` -> `CHILDREN_NOT_ALLOWED`, `NODE_NOT_EXIST_IN_SCHEMA` -> `NODE_NOT_DEFINED_IN_SCHEMA`,
  `TYPE_NOT_SUPPORTED` -> `TYPE_NOT_VALID`, `VALIDATION_ERROR` -> `UNEXPECTED_ERROR` (the single wrapper
  of an unforeseen exception, in the parser and in `SchemaValidator`; the exception subtype is kept),
  `NOT_STXT_SCHEMA` -> `SCHEMA_ROOT_NOT_VALID`, `NODE_DEF_ALREADY_DEFINED` -> `NODE_DUPLICATED`,
  `CHILD_DEF_ALREADY_DEFINED` -> `CHILD_DUPLICATED`, `VALUES_ONLY_SUPPORTED_BY_ENUM` -> `VALUES_NOT_ALLOWED_FOR_TYPE`,
  `VALUES_EMPTY_FOR_ENUM` -> `VALUES_REQUIRED`, `INVALID_INTEGER` and `INVALID_CHILD_COUNT` -> `CARDINALITY_NOT_VALID`,
  `INVALID_CHILD_LINE` -> `STRUCTURE_LINE_NOT_VALID`, `NODE_DEFINED_MULTIPLE_TIMES` -> `REFERENCE_REQUIRED`,
  `NODE_REFERENCE_NOT_VALID` -> `REFERENCE_NAME_NOT_VALID`, `NODE_NOT_FOUND` -> `DESCRIPTION_NODE_NOT_FOUND`,
  `CHILDREN_DESCRIPTION_NOT_ALLOWED` -> `DESCRIPTION_CHILDREN_NOT_ALLOWED`,
  `EXTERNAL_DESCRIPTION_NOT_ALLOWED` -> `DESCRIPTION_NOT_ALLOWED_IN_EXTERNAL_NAMESPACE`,
  `DESCRIPTION_ALREADY_DEFINED` -> `DESCRIPTION_DUPLICATED`,
  `TYPE_DEFINITION_NOT_ALLOWED` -> `TYPE_NOT_ALLOWED_IN_EXTERNAL_NAMESPACE`, `DUPLICATED_TYPE` -> `TYPE_DUPLICATED`.
  Three codes were split by condition: `INVALID_NUMBER` -> `TOO_FEW_CHILDREN` (count below `Min`) /
  `TOO_MANY_CHILDREN` (count above `Max`); `INVALID_SCHEMA` -> `SCHEMA_NODE_NOT_INLINE` (a schema node
  written with `>>`) / `SCHEMA_MULTIPLE_ROOTS` (a schema document with other than one root) /
  `SCHEMA_NAMESPACE_EMPTY` (root without target namespace), with `TEMPLATE_MULTIPLE_ROOTS` and
  `TEMPLATE_NAMESPACE_EMPTY` as the template counterparts; and the `INVALID_VALUE` of a `GROUP`
  carrying a value is now `VALUE_NOT_ALLOWED` (`INVALID_VALUE` stays for every other type).
  `INVALID_SIZE_VALUES` (a `RuntimeException`) is now `VALUES_DUPLICATED`, a `ValidationException`
  located at the second `Values` node. New `TEMPLATE_ROOT_NOT_VALID`: a template whose root is not
  `Template (@stxt.template): ns`, or whose target namespace is malformed or not the requested one,
  was not checked before; `SCHEMA_ROOT_NOT_VALID` covers the same three cases for a schema. The
  schema and template load errors (`SCHEMA_*`, `TEMPLATE_*`, `VALUES_DUPLICATED`) are
  `ValidationException` with a line, where before some were `SchemaException` without one; the
  Java-only facade codes `NAMESPACE_REQUIRED` and `RESOURCE_DIRECTORY_NOT_VALID` are unchanged.
  `ErrorCodesTest` covers every new or split code.
- **`URL` follows its own grammar** (STXT-SCHEMA-SPEC 9.4) instead of `java.net.URI`:
  `scheme "://" [userinfo "@"] host [":" port] ["/" path] ["?" query] ["#" fragment]`, with a
  mandatory scheme and non-empty host, so every port accepts exactly the same values; `mailto:`,
  `urn:`, `file:///`, a missing scheme, inner blanks or a non-numeric port are rejected, and
  nothing is resolved or normalised. `URLTest`.
- **`DATE`, `TIME` and `TIMESTAMP` validate the calendar and the clock**, not only the shape: the
  three extend the new `RangeValue` (regular expression plus an `inRange` check through
  `DateTime.isValidDate`/`isValidTime`, never `java.time`), so `2026-02-30`, `24:00:00` or an
  offset of `+25:00` are `INVALID_VALUE`. The fraction of seconds of a `TIMESTAMP` admits one or
  more digits (before, exactly three). `GrammarTypesTest`.
- **`NUMBER` is documented as its own grammar, not JSON's**: optional sign, digits with an optional
  decimal part, and an optional exponent; nothing else (no `Infinity`, `NaN`, hexadecimal or
  thousands separators), in the javadoc of the type.

## [0.9.0] - 2026-08-21

One language-level change decided on 2026-08-21 while preparing 1.0, made in the specification
first (`Last modif: 2026-08-21`), then in `stxt-impl` and in the three ports at once.
`@stxt-lang/core` and `stxt` (Python) ship the same scope as 0.9.0.

- **Comment indentation is validated like a node's** (STXT-SPEC 9, 11). A comment line (first
  non-blank character `#`, outside an open `>>` block) must have homogeneous indentation
  (`MIXED_INDENTATION` otherwise), a multiple of 4 when it uses spaces (`INVALID_NUMBER_SPACES`)
  and a level of at most the last node's level + 1 (`INDENTATION_LEVEL_NOT_VALID`); before, a
  comment accepted any indentation. Same error codes as nodes, no new code. A comment still
  produces no node and never becomes the reference level: the parser's last level is only updated
  by nodes, so a node after a deeper comment is checked against the last node, not the comment.
  Blank lines stay exempt; a `#` deeper than an open block is still block text, and a comment at
  the block's level or shallower still closes it. Conformance pair `conformance/tree/comment-indent`;
  `CommentIndentTest`.

## [0.8.1]

One language-level change decided on 2026-08-21 while preparing 1.0, made in the specification
first (`Last modif: 2026-08-21`), then in `stxt-impl` and in the three ports at once.
`@stxt-lang/core` and `stxt` (Python) ship the same scope as 0.8.1.

- **A blank is only U+0020 or U+0009** (STXT-SPEC 4). Every trim of the core —
  inline values, node names, the "is this line empty?" test and the right trim of `>>` block
  lines — works on space and tab only, through the new `StringUtils.trim`/`isBlank` and a
  narrowed `rightTrim`/`compactSpaces`/`normalize`. `String.trim()` (which also removed every
  control character below U+0020) and `Character.isWhitespace` are no longer used in the core.
  Any other Unicode space (NBSP, U+3000, U+2028...) is content: it stays in the value, a line
  holding only an NBSP is not empty (`INVALID_LINE`), `>>` followed by an NBSP is
  `INLINE_VALUE_NOT_VALID`, and an NBSP in a name makes it `INVALID_NODE_NAME`. Conformance
  pair `conformance/tree/nbsp`; `BlanksTest`.

## [0.8.0]

Three language-level changes decided on 2026-08-20 while preparing 1.0, each made in the
specification first (`Last modif: 2026-08-20`), then in `stxt-impl` and in the three ports at
once. `@stxt-lang/core` and `stxt` (Python) ship the same scope as 0.8.0.

- **A comment closes a `>>` block** (STXT-SPEC 6.1, 9.1). Any non-empty line with indentation
  less than or equal to the block node ends the block, comments included; before, a comment was
  transparent and the block stayed open across it. A block is a literal and cannot be commented
  from inside; its content is now always contiguous. Blank lines after such a comment are no
  longer block content, and a text line after it is a parse error instead of a silently lost
  comment. Conformance pair `conformance/tree/comment-closes-block`; `CommentClosesBlockTest`.
  The fixtures `docs_json/{client,recetas,types}.json`, `docs_txt/{client,recetas}.txt`,
  `docs_raw_*/client_raw.*` and `docs/types.stxt` were updated accordingly.
- **The empty namespace is never validated** (STXT-SCHEMA-SPEC 5). `SchemaValidator` no longer
  looks up a schema nor reports `SCHEMA_NOT_FOUND` for a node whose effective namespace is `""`;
  a namespaced node inside such a document is still validated. `ConditionalValidator` is
  therefore redundant and **deprecated** (to be removed in 1.0); `STXT.parser(loader)` still uses
  it, harmlessly. `SchemaProviderContractTest` covers the rule; `ConditionalValidatorTest` now
  checks that a bare `SchemaValidator` lets free nodes through too.
- **Combining marks in node names** (STXT-SPEC 4.2). Names accept the Unicode categories `Mn`
  and `Mc` besides `L` and `Nd` (Devanagari vowel signs, accents with no precomposed form...);
  enclosing marks (`Me`) are not allowed, and a name still needs at least one letter or digit,
  now checked explicitly. Conformance pair `conformance/tree/marks`; `NodeNameValidationTest`.

## [0.7.2]

- New `dev.stxt.runtime.ConditionalValidator`, the counterpart of the TypeScript and Python
  classes of the same name: a `Validator` that hands the namespaced nodes over to a
  `SchemaValidator` and lets the nodes without a namespace through. `STXT.parser(loader)` now
  registers its schema validator through it, so a document (or a root node) without a namespace
  is no longer reported — before it produced a `VALIDATION_ERROR` per node ("Namespace is
  required to load schema"), unlike the CLI, the extension and the other ports. Code that
  registers a `SchemaValidator` by hand keeps validating every node; wrap it in a
  `ConditionalValidator` to get the same behaviour. `ConditionalValidatorTest` covers both.
- The `INVALID_NUMBER` message closes the quote around the qualified child name
  (`0 nodes of 'ns:child' and min is 1`), as the other ports do.

## [0.7.1]

- `EMAIL` accepts, besides the bare address, a display name followed by the address between angle
  brackets (`Joan Costa <joan@example.com>`), as STXT-SCHEMA-SPEC 9.4 defines since 2026-08-17.
  The name is any non-empty text without `<`/`>`; the address is checked the same way in both
  forms, and unbalanced or trailing brackets are rejected. `EMAILTest` covers both forms.

## [0.7.0]

**Breaking: a new node model.** `Node` is now a sealed abstract class with two forms,
`InlineNode` (`Name: value`) and `TextNode` (`Name >>`), designed from real use of the API. Only
the in-memory model changes: the language, STXT-TREE-SPEC and every error code are the same.

- **Two forms, each owning only what is its own.** `InlineNode` has `getValue()`/`setValue()`,
  the children, the child lookups (`getChildren()`, `getChild(name[, ns])`,
  `getChildren(name[, ns])`) and the factories; `TextNode` has `getTextLines()`, `setText()`,
  `setTextLines()`, `addTextLine()`, `clearText()` and nothing else. `Node` keeps what is common:
  name, canonical name, namespaces, line, parent (typed `InlineNode`), `isTextNode()` and
  `getText()`. Walking a tree asks for the form (`instanceof InlineNode inline`), as the canonical
  tree of STXT-TREE-SPEC has `children` only for inline nodes. Both concrete classes are `final`.
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
  `getValue()`/`getTextLines()`/`addTextLine()`/`getChildren()`/`getChild(...)` on the base class
  (use `getText()`, or the concrete form). `getTextLines()` and `getChildren()` are now read-only
  views.
- New `NodeTest` (20 cases); the historical `docs_json/` fixtures are compared through a
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
