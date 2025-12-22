package dev.stxt.schema;

import java.util.List;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.resources.ResourceNotFoundException;
import dev.stxt.utils.ResourceReader;

final class SchemaProviderMeta implements SchemaProvider {
	private final Schema meta;

	public SchemaProviderMeta() {
		Schema metaSchema = null;
		String metaSchemaText = ResourceReader.readResource("dev/stxt/schema/@stxt.schema.stxt");
		Parser parser = new Parser();
		List<Node> nodes = parser.parse(metaSchemaText);
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
