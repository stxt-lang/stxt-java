package dev.stxt.schema;

import dev.stxt.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dev.stxt.Node;
import dev.stxt.ParseException;
import dev.stxt.Parser;
import dev.stxt.resources.NotFoundException;
import dev.stxt.resources.ResourcesLoader;

final class SchemaProviderCache implements SchemaProvider {
	private final ResourcesLoader resourcesLoader;
	private final Map<String, Schema> cache = new ConcurrentHashMap<>();
	private final SchemaValidator schemaValidator;

	public SchemaProviderCache(ResourcesLoader pathResolver) {
		this.resourcesLoader = pathResolver;
		this.schemaValidator = new SchemaValidator(new SchemaProviderMeta());
	}

	public Schema getSchema(String namespace) throws IOException, ParseException, NotFoundException {
		// Retorno de cache
		namespace = namespace.toLowerCase();
		Schema cached = cache.get(namespace);
		if (cached != null)
			return cached;

		// Cargamos schema
		String textSchema = resourcesLoader.retrieve(Schema.SCHEMA_NAMESPACE, namespace);
		Parser parser = new Parser();
		List<Node> nodes = parser.parse(textSchema);

		if (nodes.size() != 1)
			throw new ParseException(0, "INVALID_SCHEMA", "There are " + nodes.size() + ", and expected is 1");

		Node root = nodes.get(0);
		schemaValidator.validateNode(root);

		// Convertimos a schema
		Schema sch = SchemaParser.transformNodeToSchema(root);

		// Comprobar namespace esperado
		if (!sch.namespace.equalsIgnoreCase(namespace))
			throw new ParseException(0, "INVALID_SCHEMA",
					"Schema namespace is " + sch.namespace + ", and expected is " + namespace);

		// Insertamos en cache
		cache.put(namespace, sch);
		return sch;
	}
}
