# dev.stxt:stxt-core

Parser and schema validator for **STXT**, an indentation-based structured-text format.

STXT is a plain-text format for writing structured, semantic documents: no braces, no closing tags, just indentation. It is designed to be equally readable by humans and by machines, and it comes with an optional schema layer so documents can be validated.

- Website and language reference: <https://stxt.dev>
- JavaScript/TypeScript implementation: [@stxt-lang/core](https://www.npmjs.com/package/@stxt-lang/core)
- Python implementation: [stxt](https://pypi.org/project/stxt/) on PyPI
- VSCode extension: [STXT Language](https://marketplace.visualstudio.com/items?itemName=stxt-lang.stxt)

## What STXT looks like

```stxt
# A line starting with '#' is a comment

Article (blog.post):
    Title: Getting started with STXT
    Author: Joan
    Published: 2026-07-28
    Tags:
        Tag: parser
        Tag: text-format
    Body >>
        Everything indented under a '>>' node is kept verbatim
        as a block of text lines.
```

- `Name: value` declares an **inline node**.
- `Name >>` opens a **text block**; every deeper-indented line belongs to it.
- Indentation is **one level per tab or per 4 spaces**.
- `Name (a.b.c):` attaches a **namespace** to a node; children inherit it unless they declare their own.

## Install

Requires **Java 17** or later.

```xml
<dependency>
    <groupId>dev.stxt</groupId>
    <artifactId>stxt-core</artifactId>
    <version>0.15.0</version>
</dependency>
```

```groovy
implementation 'dev.stxt:stxt-core:0.15.0'
```

The library has **no runtime dependencies**. Under JPMS it is an automatic module named `dev.stxt`.

## Parsing

```java
import java.util.List;

import dev.stxt.InlineNode;
import dev.stxt.Node;
import dev.stxt.ParseResult;
import dev.stxt.Parser;
import dev.stxt.exceptions.ParseException;
import dev.stxt.runtime.STXT;

String text = """
        Article (blog.post):
        \tTitle: Getting started with STXT
        \tAuthor: Joan
        """;

// rawParser() parses syntax only, with no schema validation
Parser parser = STXT.rawParser();

// parseResult() collects every error instead of stopping at the first one
ParseResult result = parser.parseResult(text);

if (result.hasErrors()) {
    for (ParseException error : result.getErrors()) {
        System.err.printf("line %d [%s]: %s%n", error.getLine(), error.getCode(), error.getMessage());
    }
}

Node article = result.getNodes().get(0);

System.out.println(article.getName());                  // "Article"
System.out.println(article.getNamespace());             // "blog.post"
if (article instanceof InlineNode inline)
    System.out.println(inline.getChild("Title").getText()); // "Getting started with STXT"
```

Use `parser.parse(text)` instead if you prefer an exception (`ParseException`) on the first error. Both have a file-based counterpart: `parseFile(File)` and `parseResultFile(File)`.

A document may have **several root nodes**, which is why both entry points return a list.

`getChild(String)` returns `null` when there is no such child, and node lookup is by **canonical name**: `getChild("Título")` and `getChild("titulo")` find the same node.

## Working with the tree

`Node` is a sealed class with exactly two forms, and each one owns only what is really its own: `InlineNode` (`Name: value`) has the optional value, the children and the child lookups (`getChildren()`, `getChild(name)`, `getChildren(name)`); `TextNode` (`Name >>`) has the literal text lines and nothing else. What they share lives in `Node`: name and canonical name, declared and effective namespace, source line, parent (always an `InlineNode`) and `getText()` — the value of an inline node or the joined lines of a text node. Walking a tree therefore asks for the form (`instanceof InlineNode inline`), the same way the canonical tree of STXT-TREE-SPEC has `children` only for inline nodes.

Trees are mutable and keep their own integrity: every node knows its parent, `addChild` links both ends and refuses a node that already has one, and `removeChild` / `detach()` undo it. Levels are derived from the chain of parents; the source line is only set by the parser.

```java
import dev.stxt.InlineNode;
import dev.stxt.Node;
import dev.stxt.TextNode;

InlineNode email = new InlineNode("Email", "com.example.docs", "Weekly report");
email.addInlineNode("From", "ana@example.com");
InlineNode to = email.addInlineNode("To");
to.addInlineNode("Address", "bob@example.com");
TextNode body = email.addTextNode("Body", "Hi Bob,\n\nSee attached.");

body.getParent() == email;          // true
body.getLevel();                    // 1
to.getNamespace();                  // "com.example.docs", inherited
to.getDeclaredNamespace();          // "" — it declares none

// Reorganise: move "To" to the front
to.detach();
email.addChild(0, to);

// Edit in place
email.setNamespace("com.example.mail");   // the whole inheriting subtree follows
body.setText("Hi Bob,\n\nSee the new attachment.");

for (Node child : email.getChildren()) {
    if (child instanceof InlineNode inline) System.out.println(inline.getValue() + " " + inline.getChildren().size());
    if (child instanceof TextNode text)     System.out.println(text.getTextLines());
}
```

Overloads with two strings always take the second one as the *content* (value or text); the namespace only appears in the three-argument forms. Adding a node that already has a parent throws `NODE_ALREADY_ATTACHED`; adding an ancestor throws `NODE_CYCLE`.

## Validating against a schema

Schemas are themselves STXT documents, written in the reserved `@stxt.schema` namespace (or in the friendlier `@stxt.template` form, which is equivalent sugar). A `ResourcesLoader` says where they live; `STXT.parser(loader)` returns a parser that resolves both kinds, caches them, and validates every namespaced node as it is closed — nodes without a namespace are let through by the `SchemaValidator` itself (STXT-SCHEMA-SPEC §5), because a document without a namespace is not wrong, it just cannot be validated.

`ResourcesLoaderDirectory` expects this layout on disk:

```
<dir>/@stxt.schema/blog.post.stxt      # schema for namespace blog.post
<dir>/@stxt.template/blog.note.stxt    # template for namespace blog.note
```

```java
import java.io.File;

import dev.stxt.ParseResult;
import dev.stxt.Parser;
import dev.stxt.exceptions.ParseException;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.resources.ResourcesLoader;
import dev.stxt.resources.ResourcesLoaderDirectory;
import dev.stxt.runtime.STXT;

ResourcesLoader loader = new ResourcesLoaderDirectory(new File("schemas"));
Parser parser = STXT.parser(loader);

ParseResult result = parser.parseResult(documentText);

for (ParseException error : result.getErrors()) {
    // Schema problems are ValidationException; syntax problems are plain ParseException
    String severity = (error instanceof ValidationException) ? "warning" : "error";
    System.out.printf("%s at line %d [%s]: %s%n",
            severity, error.getLine(), error.getCode(), error.getMessage());
}
```

A schema for the document above looks like this:

```stxt
Schema (@stxt.schema): blog.post
    Node: Article
        Children:
            Child: Title
                Min: 1
                Max: 1
            Child: Author
                Min: 1
    Node: Title
    Node: Author
```

Available value types: `INLINE`, `BLOCK`, `TEXT`, `MARKDOWN`, `BOOLEAN`, `INTEGER`, `NATURAL`, `NUMBER`, `DATE`, `TIME`, `TIMESTAMP`, `UUID`, `EMAIL`, `URL`, `HEXADECIMAL`, `BINARY`, `BASE64`, `GROUP`, `ENUM`.

To add your own, implement `dev.stxt.schema.Type` and register it in `TypeRegistry`.

Schemas do not have to live on disk. `dev.stxt.runtime.UnifiedSchemaProvider` takes schema and template documents as text (`addFile`) and serves them by namespace, as do `SchemaProviderMemory` (`addSchema`) and `TemplateSchemaProviderMemory` (`addTemplate`); all three implement `SchemaProvider`, so they go straight into a `SchemaValidator`:

```java
import dev.stxt.runtime.UnifiedSchemaProvider;
import dev.stxt.schema.SchemaValidator;

UnifiedSchemaProvider provider = new UnifiedSchemaProvider();
provider.addFile(schemaText);

Parser parser = new Parser();
parser.registerValidator(new SchemaValidator(provider));
```

Discovery (STXT-DISCOVERY-SPEC) is in `dev.stxt.discovery`: `new DiscoveryResolver().resolve(documentDir)` builds the chain of `.stxt` directories and returns a `DiscoveryResult`, itself a `SchemaProvider`. The file system and the environment are injectable (`DiscoveryFileSystem`, `DiscoveryEnvironment`), so the resolver also works over an in-memory tree or a `java.nio.file.FileSystem` over a ZIP.

## Observing the parse

The parser itself knows nothing about schemas: validation is a decoupled layer plugged in through two hooks. `Observer` receives streaming callbacks while the document is parsed — useful for syntax highlighting, indexes or any per-node bookkeeping.

```java
import java.util.List;

import dev.stxt.LineIndent;
import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.TextNode;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.processors.Observer;
import dev.stxt.processors.Validator;

parser.registerObserver(new Observer() {
    @Override
    public void onCreate(Node node, String line) {
        System.out.println("open " + node.getQualifiedName());
    }

    @Override
    public void onFinish(Node node) {
        System.out.println("close " + node.getQualifiedName());
    }

    @Override
    public void onComment(int lineNumber, String line) { }

    @Override
    public void onTextLine(TextNode node, int lineNumber, String lineString, LineIndent line) { }
});

// A Validator runs when each node is closed, so documents can be validated
// while streaming instead of waiting for EOF
parser.registerValidator(node -> List.<ValidationException>of());
```

`StreamObserver` watches the results instead of the process: each completed root node and each
error, in every mode. With `parseStream` the parser retains nothing — no nodes, no errors — so a
file larger than memory can be processed one root tree at a time:

```java
import java.io.FileReader;

import dev.stxt.Constants;
import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.exceptions.ParseException;
import dev.stxt.processors.StreamObserver;

Parser parser = new Parser();
parser.registerStreamObserver(new StreamObserver() {
    @Override
    public void onRootNode(Node node) {
        System.out.println("root " + node.getQualifiedName());  // one complete root at a time
    }

    @Override
    public void onError(ParseException error) {
        System.out.println(error.toString());   // "[CODE] line N: message"
    }
});
try (FileReader reader = new FileReader("data.stxt", Constants.ENCODING)) {
    parser.parseStream(reader);
}
```

## Parser limits

The parser rejects hostile or runaway inputs by default (STXT-SPEC §11.2): documents nesting
more than 100 levels, lines longer than 10 000 characters, or inputs over 10 000 000
characters. A limit error is a `LimitException` (`LIMIT_NESTING_EXCEEDED`,
`LIMIT_LINE_LENGTH_EXCEEDED`, `LIMIT_INPUT_SIZE_EXCEEDED`) and aborts the parse: it is always
the last error reported. Each limit is configurable per parser; `-1` disables it:

```java
Parser parser = new Parser();
parser.setMaxNesting(500);
parser.setMaxInputSize(-1);
```

## Writing STXT back out

```java
import java.util.List;

import dev.stxt.Node;
import dev.stxt.runtime.NodeWriter;
import dev.stxt.runtime.NodeWriter.IndentStyle;

// A single node, or a whole document list
String text = NodeWriter.toSTXT(node, IndentStyle.TABS);
String docs = NodeWriter.toSTXT(result.getNodes(), IndentStyle.SPACES_4);
```

Writing a tree out and parsing it back yields the same tree, in both indentation styles.

`NodeWriter` re-serializes the tree, so comments and blank lines are gone. To reformat a document
**keeping everything the author wrote**, use `Formatter`: it rewrites the original text line by
line — node lines in canonical form, block lines re-indented to their block, comments and blank
lines kept with their indentation units converted — and reports the syntax errors it met, so the
caller decides what to do with a document that does not parse.

```java
import dev.stxt.runtime.Formatter;
import dev.stxt.runtime.FormatResult;

FormatResult formatted = Formatter.format(source, IndentStyle.TABS);
if (formatted.errors().isEmpty()) {
    Files.writeString(path, formatted.text());
}
```

An overload takes the parser limits, since formatting parses the document with them
(STXT-SPEC §11.2; `-1` disables one):
`Formatter.format(source, IndentStyle.TABS, 100, 10000, -1)`.

## Errors

Every failure is an unchecked `dev.stxt.exceptions.STXTException` carrying an uppercase error code (`getCode()`), such as `INVALID_LINE`, `NODE_NOT_DEFINED_IN_SCHEMA` or `SCHEMA_NOT_FOUND`:

| Exception | Raised when |
|---|---|
| `ParseException` | the syntax is wrong; adds `getLine()` |
| `ValidationException` | the document breaks its schema (type, cardinality, undeclared child) |
| `LimitException` | a parser limit was exceeded (`LIMIT_NESTING_EXCEEDED`, `LIMIT_LINE_LENGTH_EXCEEDED`, `LIMIT_INPUT_SIZE_EXCEEDED`); the parse aborts |
| `SchemaException` | a schema is built inconsistently through the API (`NODE_DUPLICATED`, `CHILD_DUPLICATED`) or a provider is asked for an empty namespace (`NAMESPACE_REQUIRED`); a malformed schema or template document raises a `ValidationException` with its line (`SCHEMA_ROOT_NOT_VALID`, `SCHEMA_MULTIPLE_ROOTS`, `VALUES_DUPLICATED`...) |
| `ResourceNotFoundException` | a `ResourcesLoader` has no such resource (schema providers turn it into a `SCHEMA_NOT_FOUND` finding) |
| `STXTException` (base) | tree integrity is broken (`NODE_ALREADY_ATTACHED`, `NODE_CYCLE`), an ambiguous lookup (`AMBIGUOUS_CHILD`), and other runtime failures |
| `STXTIOException` | reading a file failed |

## Conformance

`dev.stxt:stxt-core` implements the five STXT specifications at `SPEC_VERSION` (exposed by the package; the package version is independent) and passes every case of the official conformance kit, [`stxt-lang/conformance`](https://github.com/stxt-lang/stxt-lang/tree/master/conformance), across all its profiles: `core`, `schema`, `template`, `discovery` and `text`. The kit is the same one any other implementation can run, which is what makes the three ports interchangeable. What the 1.0 line freezes, and what it does not, is stated at <https://stxt.dev/lang-stability>.

## License

MIT — see [LICENSE](LICENSE).
