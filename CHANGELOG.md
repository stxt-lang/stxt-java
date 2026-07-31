# Change Log

All notable changes to `dev.stxt:stxt-core` are documented in this file.

Check [Keep a Changelog](http://keepachangelog.com/) for recommendations on how to structure this file.

## [Unreleased]

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
