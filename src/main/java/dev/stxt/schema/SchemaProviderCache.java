package dev.stxt.schema;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dev.stxt.Node;
import dev.stxt.ParseException;
import dev.stxt.Parser;
import dev.stxt.resources.ResourcesLoader;

final class SchemaProviderCache implements SchemaProvider {
	private final ResourcesLoader resourcesLoader;
	private final Map<String, Schema> cache = new ConcurrentHashMap<>();
	private final SchemaValidator schemaValidator;

	public SchemaProviderCache(ResourcesLoader pathResolver) {
		this.resourcesLoader = pathResolver;
		this.schemaValidator = new SchemaValidator(new SchemaProviderMeta());
	}

	public Schema getSchema(String namespace) {
		// Retorno de cache
		namespace = namespace.toLowerCase();
		Schema cached = cache.get(namespace);
		if (cached != null)
			return cached;

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
		if (!sch.namespace.equalsIgnoreCase(namespace))
			throw new SchemaException("INVALID_SCHEMA", "Schema namespace is " + sch.namespace + ", and expected is " + namespace);

		// Insertamos en cache
		cache.put(namespace, sch);
		return sch;
	}
}
