package dev.stxt.template;

import java.util.List;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.exceptions.SchemaException;
import dev.stxt.resources.ResourcesLoader;
import dev.stxt.schema.Schema;
import dev.stxt.schema.SchemaProvider;
import dev.stxt.schema.SchemaValidator;

/** {@link SchemaProvider} that loads an {@code @stxt.template} document from a {@link ResourcesLoader} and turns it into a {@link Schema}. */
public class TemplateSchemaProvider implements SchemaProvider {
	private final ResourcesLoader loader;
	
	/** @param loader where to load the {@code @stxt.template} document of each namespace from. */
	public TemplateSchemaProvider(ResourcesLoader loader) {
		this.loader = loader;
	}	
	
	@Override
	public Schema getSchema(String namespace) {
		String template = loader.retrieve("@stxt.template", namespace);

		// Create the parser
		Parser parser = new Parser();
		parser.registerValidator(new SchemaValidator(new MetaTemplateSchemaProvider()));
		
		List<Node> nodes = parser.parse(template);
		if (nodes.size() != 1)
			throw new SchemaException("INVALID_SCHEMA", "There are " + nodes.size() + ", and expected is 1");

		// Get the schema
		Schema sch = TemplateParser.transformNodeToSchema(nodes.get(0)); 
		
		// Check the expected namespace
		if (!sch.getNamespace().equalsIgnoreCase(namespace))
			throw new SchemaException("INVALID_SCHEMA", "Schema namespace is " + sch.getNamespace() + ", and expected is " + namespace);
		
		return sch;
	}
}
