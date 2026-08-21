package dev.stxt.schema;

import java.util.List;
import java.util.Locale;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.exceptions.ResourceNotFoundException;
import dev.stxt.exceptions.SchemaException;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.resources.ResourcesLoader;

/** {@link SchemaProvider} that loads an {@code @stxt.schema} document from a {@link ResourcesLoader} and turns it into a {@link Schema}. */
public final class SchemaProviderResources implements SchemaProvider {
	private final ResourcesLoader resourcesLoader;
	private final SchemaValidator schemaValidator;

	/**
	 * Creates a provider that loads schemas through a {@link ResourcesLoader}.
	 *
	 * @param pathResolver where to load the {@code @stxt.schema} document of each namespace from.
	 */
	public SchemaProviderResources(ResourcesLoader pathResolver) {
		this.resourcesLoader = pathResolver;
		this.schemaValidator = new SchemaValidator(new SchemaProviderMeta());
	}

	/** {@return the schema loaded and validated against the meta-schema, or {@code null} if the resource does not exist} */
	public Schema getSchema(String namespace) {
		// Return from the cache
		if (namespace == null || namespace.isEmpty())
		    throw new SchemaException("NAMESPACE_REQUIRED", "Namespace is required to load schema");

		namespace = namespace.toLowerCase(Locale.ROOT);

		// Load the schema; a missing resource is "no schema for this namespace", not an error
		String textSchema;
		try {
			textSchema = resourcesLoader.retrieve(Schema.SCHEMA_NAMESPACE, namespace);
		}
		catch (ResourceNotFoundException e) {
			return null;
		}
		Parser parser = new Parser();
		parser.registerValidator(schemaValidator);
		List<Node> nodes = parser.parse(textSchema);

		if (nodes.size() != 1)
			throw new ValidationException(nodes.size() > 1 ? nodes.get(1).getLine() : 0, "SCHEMA_MULTIPLE_ROOTS",
					"A schema document must have exactly 1 root node, found " + nodes.size());

		// Turn it into a schema
		Node root = nodes.get(0);
		Schema sch = SchemaParser.transformNodeToSchema(root);

		// Check the expected namespace
		if (!sch.getNamespace().equalsIgnoreCase(namespace))
			throw new ValidationException(root.getLine(), "SCHEMA_ROOT_NOT_VALID", "Schema namespace is " + sch.getNamespace() + ", and expected is " + namespace);

		// Put it in the cache
		return sch;
	}
}
