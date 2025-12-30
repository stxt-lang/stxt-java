package dev.stxt.template;

import java.util.List;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.exceptions.SchemaException;
import dev.stxt.resources.ResourcesLoader;
import dev.stxt.schema.Schema;
import dev.stxt.schema.SchemaProvider;

public class TemplateSchemaProvider implements SchemaProvider {
	private static final Parser PARSER = new Parser();
	private final ResourcesLoader loader;
	
	public TemplateSchemaProvider(ResourcesLoader loader) {
		this.loader = loader;
	}	
	
	@Override
	public Schema getSchema(String namespace) {
		String template = loader.retrieve("@stxt.template", namespace);
		
		List<Node> nodes = PARSER.parse(template);
		if (nodes.size() != 1)
			throw new SchemaException("INVALID_SCHEMA", "There are " + nodes.size() + ", and expected is 1");
		
		return TemplateParser.transformNodeToSchema(nodes.get(0));
	}
}
