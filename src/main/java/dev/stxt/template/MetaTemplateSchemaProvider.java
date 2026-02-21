package dev.stxt.template;

import java.util.List;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.exceptions.ResourceNotFoundException;
import dev.stxt.exceptions.SchemaException;
import dev.stxt.schema.Schema;
import dev.stxt.schema.SchemaProvider;

public class MetaTemplateSchemaProvider implements SchemaProvider {

	private static final String META_TEXT = """
Template (@stxt.template): @stxt.template
	Structure >>
		Template (@stxt.template):
			Structure: (1) BLOCK
""";
			
	private final Schema meta;
	
	public MetaTemplateSchemaProvider() {
		Schema metaSchema = null;
		Parser parser = new Parser();
		List<Node> nodes = parser.parse(META_TEXT);
		if (nodes.size() != 1)
		    throw new SchemaException("META_SCHEMA_INVALID", "Meta schema must produce exactly 1 document, got " + nodes.size());
		
		metaSchema = TemplateParser.transformNodeToSchema(nodes.get(0));
		meta = metaSchema;
	}
	
	@Override
	public Schema getSchema(String namespace) {
		if (!"@stxt.template".equals(namespace))
			throw new ResourceNotFoundException("@stxt.template", namespace);

	    if (meta == null)
	        throw new SchemaException("META_SCHEMA_NOT_AVAILABLE", "Meta schema not available");

	    return meta;
	}
}
