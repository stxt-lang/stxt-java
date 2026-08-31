package dev.stxt.schema;

import java.util.Locale;

import dev.stxt.exceptions.ParseException;
import dev.stxt.exceptions.ResourceNotFoundException;
import dev.stxt.exceptions.SchemaException;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.resources.ResourcesLoader;

/** {@link SchemaProvider} that loads an {@code @stxt.schema} document from a {@link ResourcesLoader} and turns it into a {@link Schema}. */
public final class SchemaProviderResources implements SchemaProvider {
	private final ResourcesLoader resourcesLoader;

	/**
	 * Creates a provider that loads schemas through a {@link ResourcesLoader}.
	 *
	 * @param pathResolver where to load the {@code @stxt.schema} document of each namespace from.
	 */
	public SchemaProviderResources(ResourcesLoader pathResolver) {
		this.resourcesLoader = pathResolver;
	}

	/** {@return the schema loaded and validated against the meta-schema, or {@code null} if the resource does not exist} */
	public Schema getSchema(String namespace) {
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

		// The whole load pipeline is the shared one of DefinitionCompiler
		Schema sch = DefinitionCompiler.compileDocument(textSchema, new SchemaProviderMeta(),
				SchemaParser::transformNodeToSchema, "SCHEMA_MULTIPLE_ROOTS", "schema");

		// Facade check, outside the normative scope: the document must define the requested namespace
		if (!sch.getNamespace().equalsIgnoreCase(namespace))
			throw new ValidationException(ParseException.NO_LINE, "SCHEMA_ROOT_NOT_VALID",
					"Schema namespace is " + sch.getNamespace() + ", and expected is " + namespace);

		return sch;
	}
}
