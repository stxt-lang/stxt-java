package dev.stxt.schema;

import dev.stxt.IOException;
import java.util.List;

import dev.stxt.Node;
import dev.stxt.ParseException;
import dev.stxt.Parser;
import dev.stxt.resources.NotFoundException;
import dev.stxt.utils.ResourceReader;

final class SchemaProviderMeta implements SchemaProvider {
	private final Schema meta;

	public SchemaProviderMeta() {
		Schema metaSchema = null;
		try {
			String metaSchemaText = ResourceReader.readResource("dev/stxt/schema/@stxt.schema.stxt");
			Parser parser = new Parser();
			List<Node> nodes = parser.parse(metaSchemaText);
			metaSchema = SchemaParser.transformNodeToSchema(nodes.get(0));
		} catch (Exception e) {
			throw new RuntimeException();
		}
		meta = metaSchema;
	}

	public Schema getSchema(String namespace) throws IOException, ParseException, NotFoundException {
		if (!namespace.equals(Schema.SCHEMA_NAMESPACE))
			throw new NotFoundException(namespace, namespace);

		// Retorno de cache
		return meta;
	}
}
