package dev.stxt.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.utils.StringUtils;

/**
 * In-memory {@link SchemaProvider}: it keeps the schemas added with {@link #addSchema(String)}
 * indexed by namespace, and falls back to a parent provider (the meta-schema by default) for the
 * namespaces it does not know. Same contract as {@code SchemaProviderMemory} of the other ports.
 */
public class SchemaProviderMemory implements SchemaProvider {
	private final SchemaProvider parent;

	/** The schemas registered here, by namespace and in registration order. */
	protected final Map<String, Schema> schemas = new LinkedHashMap<>();

	/** Creates an empty provider that falls back to the meta-schema provider. */
	public SchemaProviderMemory() {
		this(null);
	}

	/**
	 * Creates an empty provider.
	 *
	 * @param parent provider to fall back to when a namespace is not registered here; the
	 *        meta-schema provider when {@code null}.
	 */
	public SchemaProviderMemory(SchemaProvider parent) {
		this.parent = parent != null ? parent : new SchemaProviderMeta();
	}

	/**
	 * Resolves the schema that applies to a namespace, delegating to the parent provider when it
	 * is not registered here.
	 *
	 * @return the schema of the namespace, or {@code null} if neither this provider nor its parent has one.
	 */
	@Override
	public Schema getSchema(String namespace) {
		Schema result = schemas.get(StringUtils.lowerCase(namespace));
		return result != null ? result : parent.getSchema(namespace);
	}

	/**
	 * Parses a schema document, validates it against the meta-schema and registers it under its
	 * own namespace.
	 *
	 * @param text text of the {@code @stxt.schema} document.
	 * @throws dev.stxt.exceptions.ParseException if the document cannot be parsed.
	 * @throws ValidationException with {@code SCHEMA_MULTIPLE_ROOTS} if it does not hold exactly one
	 *         root node, or the first error if it does not validate against the meta-schema.
	 */
	public void addSchema(String text) {
		List<Node> nodes = new Parser().parse(text);
		if (nodes.size() != 1)
			throw new ValidationException(0, "SCHEMA_MULTIPLE_ROOTS", "A schema document must hold exactly 1 root node, got " + nodes.size());
		Node node = nodes.get(0);

		// A schema that does not validate against its meta-schema must not be registered
		List<ValidationException> errors = new SchemaValidator(new SchemaProviderMeta(), true).validate(node);
		if (!errors.isEmpty())
			throw errors.get(0);

		Schema schema = SchemaParser.transformNodeToSchema(node);
		schemas.put(schema.getNamespace(), schema);
	}

	/** Removes every schema registered in this provider (the parent one is left untouched). */
	public void clear() {
		schemas.clear();
	}

	/** {@return every schema registered in this provider, in registration order} */
	public List<Schema> getAllSchemas() {
		return Collections.unmodifiableList(new ArrayList<>(schemas.values()));
	}
}
