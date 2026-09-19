# dev.stxt:stxt-core

Parser and schema validator for **STXT**, in Java.

STXT is a **Human-First** language, designed for documents and structured data: indentation is
the structure, free text is literal, and schemas are written in STXT itself.

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

- `Name: value` is an **inline node**.
- `Name >>` opens a **text block**. Every deeper-indented line belongs to it.
- Indentation is **one level per tab or per 4 spaces**.
- `Name (a.b.c):` attaches a **namespace** to a node. Children inherit it unless they declare their own.

Links:

- The language: <https://stxt.dev>
- The full guide of this library: <https://stxt.dev/tools-java>
- The other implementations: [`@stxt-lang/core`](https://www.npmjs.com/package/@stxt-lang/core) (TypeScript) and [`stxt`](https://pypi.org/project/stxt/) (Python)
- The tools: the [`stxt` command](https://www.npmjs.com/package/@stxt-lang/cli), the [VS Code extension](https://marketplace.visualstudio.com/items?itemName=stxt-lang.stxt) and the [playground](https://play.stxt.dev)

## Install

Requires **Java 17** or later.

```xml
<dependency>
    <groupId>dev.stxt</groupId>
    <artifactId>stxt-core</artifactId>
    <version>1.0.3</version>
</dependency>
```

```groovy
implementation 'dev.stxt:stxt-core:1.0.3'
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

| Entry point | Behaviour |
|---|---|
| `parseResult(text)`, `parseResultFile(File)` | Collects every error, and also returns the nodes it managed to build |
| `parse(text)`, `parseFile(File)` | Throws a `ParseException` on the first error |
| `parseStream(reader)` | Retains no nodes or errors (see *Observing the parse*) |

- A document may have **several root nodes**, so the entry points return a list.
- `getChild(String)` returns `null` when there is no such child.
- Children are looked up by **canonical name**: `getChild("Title")` and `getChild("title")` find the same node.

## Working with the tree

`Node` is a sealed class with two forms:

| Class | Syntax | What it has |
|---|---|---|
| `InlineNode` | `Name: value` | The optional value, the children and the child lookups: `getChildren()`, `getChild(name)`, `getChildren(name)` |
| `TextNode` | `Name >>` | The literal text lines |

Both share what is in `Node`: the name and the canonical name, the declared and the effective
namespace, the source line, the parent (always an `InlineNode`) and `getText()`.
The form of a node is told apart with `instanceof`.

Trees are mutable, and every node knows its parent:

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
to.getDeclaredNamespace();          // "": it declares none

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

- In the overloads with two strings, the second one is the *content* (value or text). The namespace only appears in the three-argument forms.
- Adding a node that already has a parent throws `NODE_ALREADY_ATTACHED`. Adding an ancestor throws `NODE_CYCLE`.
- The level is derived from the chain of parents. The source line is only set by the parser.

## Validating against a schema

Schemas are STXT documents, written in the `@stxt.schema` namespace, or in the shorter
`@stxt.template` form, which compiles to a schema.

A `ResourcesLoader` says where they live. `STXT.parser(loader)` returns a parser that resolves
both kinds, caches them, and validates every node with a namespace as it is closed. Nodes
without a namespace are not validated (STXT-SCHEMA-SPEC §5).

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

The value types are those of [STXT-SCHEMA-SPEC §9](https://stxt.dev/stxt-schema-ref#s9): `INLINE`, `BLOCK`, `TEXT`, `MARKDOWN`, `BOOLEAN`, `INTEGER`, `NATURAL`, `NUMBER`, `DATE`, `TIME`, `TIMESTAMP`, `UUID`, `EMAIL`, `URL`, `HEXADECIMAL`, `BINARY`, `BASE64`, `GROUP`, `ENUM`.

Schemas do not have to live on disk. Three providers take them as text, and all three implement
`SchemaProvider`, so they go straight into a `SchemaValidator`:

| Provider | Method | Takes |
|---|---|---|
| `dev.stxt.runtime.UnifiedSchemaProvider` | `addFile` | Schema and template documents |
| `SchemaProviderMemory` | `addSchema` | Schemas |
| `TemplateSchemaProviderMemory` | `addTemplate` | Templates |


```java
import dev.stxt.runtime.UnifiedSchemaProvider;
import dev.stxt.schema.SchemaValidator;

UnifiedSchemaProvider provider = new UnifiedSchemaProvider();
provider.addFile(schemaText);

Parser parser = new Parser();
parser.registerValidator(new SchemaValidator(provider));
```

## Finding the schemas: discovery

Discovery (STXT-DISCOVERY-SPEC) is in `dev.stxt.discovery`. Given the directory of a document,
it builds the chain of `.stxt` directories and returns a `DiscoveryResult`, which is also a
`SchemaProvider`:

```java
DiscoveryResult result = new DiscoveryResolver().resolve(documentDir);
```

The file system and the environment can be replaced (`DiscoveryFileSystem`,
`DiscoveryEnvironment`), so the resolver also works over an in-memory tree, or over a
`java.nio.file.FileSystem` on a ZIP.

## Observing the parse

The parser knows nothing about schemas. Validation is a separate layer, plugged in through two
extension points: `Observer` and `Validator`.

An `Observer` receives calls while the document is parsed. It is useful for syntax highlighting
or for indexes.

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

A `StreamObserver` receives the results: each completed root node and each error. With
`parseStream` the parser retains no nodes or errors, so a file larger than memory can be
processed one root tree at a time:

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

The parser applies three limits by default (STXT-SPEC §11.2):

| Limit | Default | Error code |
|---|---|---|
| Nesting depth | 100 levels | `LIMIT_NESTING_EXCEEDED` |
| Line length | 10 000 characters | `LIMIT_LINE_LENGTH_EXCEEDED` |
| Input size | 10 000 000 characters | `LIMIT_INPUT_SIZE_EXCEEDED` |

A limit error is a `LimitException`, and it aborts the parse: it is always the last error
reported. Each limit is configurable per parser, and `-1` disables it:

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

`NodeWriter` writes the tree, so comments and blank lines are lost.

`Formatter` reformats a document **keeping the comments and the blank lines**. It rewrites the
original text line by line, and returns the text together with the syntax errors it found.

```java
import dev.stxt.runtime.Formatter;
import dev.stxt.runtime.FormatResult;

FormatResult formatted = Formatter.format(source, IndentStyle.TABS);
if (formatted.errors().isEmpty()) {
    Files.writeString(path, formatted.text());
}
```

Formatting parses the document, so an overload takes the limits of the parser:
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

`dev.stxt:stxt-core` implements the five STXT specifications, and passes every case of the
[conformance kit](https://github.com/stxt-lang/stxt-lang/tree/master/conformance) in all its
profiles: `core`, `schema`, `template`, `discovery` and `text`. It is the same kit the other
implementations run.

`SPEC_VERSION` is the date of the STXT-SPEC text the library implements. The library version is
independent, and follows semver. The specifications carry a date and a status, not a version
number: see <https://stxt.dev/stability>.

## License

MIT, see [LICENSE](LICENSE).
