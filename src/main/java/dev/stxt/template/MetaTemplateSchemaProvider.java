package dev.stxt.template;

import java.util.List;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.exceptions.ResourceNotFoundException;
import dev.stxt.exceptions.SchemaException;
import dev.stxt.schema.Schema;
import dev.stxt.schema.SchemaProvider;

/**
 * {@link SchemaProvider} que define en código el meta-schema del propio lenguaje de templates
 * ({@code @stxt.template}), análogo a {@link dev.stxt.schema.SchemaProviderMeta} para schemas.
 */
public class MetaTemplateSchemaProvider implements SchemaProvider {

	private static final String META_TEXT = """
Template (@stxt.template): @stxt.template
	Structure >>
		Template (@stxt.template):
			Description: (?) TEXT
			Structure: (1) BLOCK
""";
			
	private final Schema meta;
	
	/** Construye el meta-schema de templates parseando y validando {@code META_TEXT}. */
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
