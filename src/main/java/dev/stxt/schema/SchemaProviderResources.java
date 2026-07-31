package dev.stxt.schema;

import java.util.List;
import java.util.Locale;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.exceptions.SchemaException;
import dev.stxt.resources.ResourcesLoader;

/** {@link SchemaProvider} that loads an {@code @stxt.schema} document from a {@link ResourcesLoader} and turns it into a {@link Schema}. */
public final class SchemaProviderResources implements SchemaProvider {
	private final ResourcesLoader resourcesLoader;
	private final SchemaValidator schemaValidator;

	/** @param pathResolver where to load the {@code @stxt.schema} document of each namespace from. */
	public SchemaProviderResources(ResourcesLoader pathResolver) {
		this.resourcesLoader = pathResolver;
		this.schemaValidator = new SchemaValidator(new SchemaProviderMeta());
	}

	/** @return the schema loaded and validated against the meta-schema, or {@code null} if the resource does not exist. */
	public Schema getSchema(String namespace) {
		// Return from the cache
		if (namespace == null || namespace.isEmpty())
		    throw new SchemaException("NAMESPACE_REQUIRED", "Namespace is required to load schema");

		namespace = namespace.toLowerCase(Locale.ROOT);

		// Load the schema
		String textSchema = resourcesLoader.retrieve(Schema.SCHEMA_NAMESPACE, namespace);
		Parser parser = new Parser();
		parser.registerValidator(schemaValidator);
		List<Node> nodes = parser.parse(textSchema);

		if (nodes.size() != 1)
			throw new SchemaException("INVALID_SCHEMA", "There are " + nodes.size() + ", and expected is 1");

		// Turn it into a schema
		Node root = nodes.get(0);
		Schema sch = SchemaParser.transformNodeToSchema(root);

		// Check the expected namespace
		if (!sch.getNamespace().equalsIgnoreCase(namespace))
			throw new SchemaException("INVALID_SCHEMA", "Schema namespace is " + sch.getNamespace() + ", and expected is " + namespace);

		// Put it in the cache
		return sch;
	}
}
