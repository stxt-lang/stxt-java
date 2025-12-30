package dev.stxt.schema;

import java.util.List;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.exceptions.ResourceNotFoundException;
import dev.stxt.exceptions.SchemaException;

public final class SchemaProviderMeta implements SchemaProvider {
	private static final String META_TEXT = "Schema (@stxt.schema): @stxt.schema\n"
			+ "    Node: Schema\n"
			+ "        Children:\n"
			+ "            Child: Description\n"
			+ "                Max: 1\n"
			+ "            Child: Node\n"
			+ "                Min: 1\n"
			+ "    Node: Node\n"
			+ "        Children:\n"
			+ "            Child: Type\n"
			+ "                Max: 1\n"
			+ "            Child: Children\n"
			+ "                Max: 1\n"
			+ "            Child: Description\n"
			+ "                Max: 1\n"
			+ "    Node: Children\n"
			+ "        Children:\n"
			+ "            Child: Child\n"
			+ "                Min: 1\n"
			+ "    Node: Description\n"
			+ "        Type: TEXT\n"
			+ "    Node: Child\n"
			+ "        Children:\n"
			+ "            Child: Min\n"
			+ "                Max: 1\n"
			+ "            Child: Max\n"
			+ "                Max: 1\n"
			+ "    Node: Min\n"
			+ "        Type: NATURAL\n"
			+ "    Node: Max\n"
			+ "        Type: NATURAL\n"
			+ "    Node: Type";
	
	private final Schema meta;

	public SchemaProviderMeta() {
		Schema metaSchema = null;
		Parser parser = new Parser();
		List<Node> nodes = parser.parse(META_TEXT);
		if (nodes.size() != 1)
		    throw new SchemaException("META_SCHEMA_INVALID", "Meta schema must produce exactly 1 document, got " + nodes.size());
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
