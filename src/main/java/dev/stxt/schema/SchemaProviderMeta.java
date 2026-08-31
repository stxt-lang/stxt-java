package dev.stxt.schema;

import java.util.List;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.exceptions.ParseException;
import dev.stxt.exceptions.ValidationException;

/**
 * {@link SchemaProvider} that defines in code the meta-schema of the schema language itself
 * ({@code @stxt.schema}), so that a loaded schema can validate itself.
 */
public final class SchemaProviderMeta implements SchemaProvider {
	private static final String META_TEXT = """
Schema (@stxt.schema): @stxt.schema
    Node: Schema
        Children:
            Child: Description
                Max: 1
            Child: Node
                Min: 1
    Node: Node
        Children:
            Child: Type
                Max: 1
            Child: Children
                Max: 1
            Child: Description
                Max: 1
            Child: Values
                Max: 1
    Node: Children
        Type: GROUP
        Children:
            Child: Child
                Min: 1
    Node: Description
        Type: TEXT
    Node: Child
        Children:
            Child: Min
                Max: 1
            Child: Max
                Max: 1
    Node: Min
        Type: NATURAL
    Node: Max
        Type: NATURAL
    Node: Type
        Type: ENUM
        Values:
            Value: INLINE
            Value: BLOCK
            Value: TEXT
            Value: GROUP
            Value: BOOLEAN
            Value: NUMBER
            Value: ENUM
            Value: INTEGER
            Value: NATURAL
            Value: DATE
            Value: TIME
            Value: TIMESTAMP
            Value: UUID
            Value: URL
            Value: EMAIL
            Value: HEXADECIMAL
            Value: BINARY
            Value: BASE64
            Value: MARKDOWN
    Node: Values
        Type: GROUP
        Children:
            Child: Value
                Min: 1
    Node: Value
""";
	
	/**
	 * The meta-schema is immutable, so it is compiled once per process, lazily, and every
	 * instance serves this same schema (constructing these providers is common: every
	 * {@code addSchema()} and every discovery compilation builds one).
	 */
	private static Schema compiledMeta;

	private final Schema meta;

	/** Compiles the meta-schema the first time and keeps it ready to be served. */
	public SchemaProviderMeta() {
		meta = compiledMeta();
	}

	// Lazy, thread-safe compilation of META_TEXT, shared by every instance.
	private static synchronized Schema compiledMeta() {
		if (compiledMeta == null) {
			Parser parser = new Parser();
			List<Node> nodes = parser.parse(META_TEXT);
			if (nodes.size() != 1)
				throw new ValidationException(ParseException.NO_LINE, "META_SCHEMA_INVALID",
						"Meta schema must produce exactly 1 document, got " + nodes.size());

			compiledMeta = SchemaParser.transformNodeToSchema(nodes.get(0));
		}
		return compiledMeta;
	}

	/**
	 * Serves the meta-schema. Follows the {@link SchemaProvider} contract: providers never throw
	 * "not found", so any namespace other than {@code @stxt.schema} yields {@code null} and only
	 * {@link SchemaValidator} reports {@code SCHEMA_NOT_FOUND}.
	 *
	 * @return the meta-schema for {@code @stxt.schema}, or {@code null} for any other namespace.
	 */
	public Schema getSchema(String namespace) {
	    if (!Schema.SCHEMA_NAMESPACE.equals(namespace))
	        return null;

	    return meta;
	}
}
