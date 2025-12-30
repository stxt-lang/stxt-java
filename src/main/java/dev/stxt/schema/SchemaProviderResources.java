package dev.stxt.schema;

import java.util.List;
import java.util.Locale;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.exceptions.SchemaException;
import dev.stxt.resources.ResourcesLoader;

public final class SchemaProviderResources implements SchemaProvider {
	private final ResourcesLoader resourcesLoader;
	private final SchemaValidator schemaValidator;

	public SchemaProviderResources(ResourcesLoader pathResolver) {
		this.resourcesLoader = pathResolver;
		this.schemaValidator = new SchemaValidator(new SchemaProviderMeta());
	}

	public Schema getSchema(String namespace) {
		// Retorno de cache
		if (namespace == null || namespace.isEmpty())
		    throw new SchemaException("NAMESPACE_REQUIRED", "Namespace is required to load schema");
		
		namespace = namespace.toLowerCase(Locale.ROOT);

		// Cargamos schema
		String textSchema = resourcesLoader.retrieve(Schema.SCHEMA_NAMESPACE, namespace);
		Parser parser = new Parser();
		parser.registerValidator(schemaValidator);
		List<Node> nodes = parser.parse(textSchema);

		if (nodes.size() != 1)
			throw new SchemaException("INVALID_SCHEMA", "There are " + nodes.size() + ", and expected is 1");

		// Convertimos a schema
		Node root = nodes.get(0);
		Schema sch = SchemaParser.transformNodeToSchema(root);

		// Comprobar namespace esperado
		if (!sch.getNamespace().equalsIgnoreCase(namespace))
			throw new SchemaException("INVALID_SCHEMA", "Schema namespace is " + sch.getNamespace() + ", and expected is " + namespace);

		// Insertamos en cache
		return sch;
	}
}
