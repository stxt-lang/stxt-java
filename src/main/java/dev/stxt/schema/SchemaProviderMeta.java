package dev.stxt.schema;

import java.util.List;

import dev.stxt.Node;
import dev.stxt.ParseException;
import dev.stxt.Parser;
import dev.stxt.STXTException;
import dev.stxt.resources.ResourceNotFoundException;
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
		} catch (java.io.IOException e) {
		    throw new STXTException("META_SCHEMA_NOT_FOUND", 
		        "Could not load meta-schema from resources", e);
		} catch (ParseException e) {
		    throw new STXTException("INVALID_META_SCHEMA", 
		        "Meta-schema is invalid", e);
		}		
		meta = metaSchema;
	}

	public Schema getSchema(String namespace) {
		if (!namespace.equals(Schema.SCHEMA_NAMESPACE) || meta == null)
			throw new ResourceNotFoundException(namespace, namespace);

		// Retorno de cache
		return meta;
	}
}
