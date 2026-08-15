package dev.stxt.schema;

import java.util.List;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.exceptions.SchemaException;

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
	
	private final Schema meta;

	/** Builds the meta-schema by parsing and validating {@code META_TEXT} against itself. */
	public SchemaProviderMeta() {
		Schema metaSchema = null;
		Parser parser = new Parser();
		List<Node> nodes = parser.parse(META_TEXT);
		if (nodes.size() != 1)
		    throw new SchemaException("META_SCHEMA_INVALID", "Meta schema must produce exactly 1 document, got " + nodes.size());
		metaSchema = SchemaParser.transformNodeToSchema(nodes.get(0));
		meta = metaSchema;
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

	    if (meta == null)
	        throw new SchemaException("META_SCHEMA_NOT_AVAILABLE", "Meta schema not available");

	    return meta;
	}
}
