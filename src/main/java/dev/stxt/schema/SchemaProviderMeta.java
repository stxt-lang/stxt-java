package dev.stxt.schema;

import java.util.List;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.resources.ResourceNotFoundException;

final class SchemaProviderMeta implements SchemaProvider {
	private static final String META_TEXT = "Schema (@stxt.schema): @stxt.schema\n"
			+ "    Node: Schema (INLINE TEXT)\n"
			+ "        Children>>\n"
			+ "            Description (?)\n"
			+ "            Node (+)\n"
			+ "    Node: Node (INLINE TEXT)\n"
			+ "        Children>>\n"
			+ "            Children (?)\n"
			+ "            Description (?)\n"
			+ "    Node: Children (MULTILINE TEXT)\n"
			+ "    Node: Description (TEXT)\n"
			+ "";
	
	private final Schema meta;

	public SchemaProviderMeta() {
		Schema metaSchema = null;
		Parser parser = new Parser();
		List<Node> nodes = parser.parse(META_TEXT);
		metaSchema = SchemaParser.transformNodeToSchema(nodes.get(0));
		meta = metaSchema;
	}

	public Schema getSchema(String namespace) {
	    if (!Schema.SCHEMA_NAMESPACE.equals(namespace))
	        throw new ResourceNotFoundException(Schema.SCHEMA_NAMESPACE, namespace);

	    if (meta == null)
	        throw new SchemaException("META_SCHEMA_NOT_AVAILABLE", "Meta schema not available");

	    return meta;
	}
}
