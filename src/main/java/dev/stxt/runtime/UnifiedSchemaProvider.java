package dev.stxt.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.schema.Schema;
import dev.stxt.schema.SchemaParser;
import dev.stxt.schema.SchemaProvider;
import dev.stxt.schema.SchemaProviderMeta;
import dev.stxt.schema.SchemaValidator;
import dev.stxt.template.MetaTemplateSchemaProvider;
import dev.stxt.template.TemplateParser;
import dev.stxt.utils.StringUtils;

/**
 * Unified in-memory provider that handles both schemas and templates. It detects which one a
 * root node is from its namespace: {@code @stxt.template} is processed as a template,
 * {@code @stxt.schema} as a schema, anything else is ignored. It serves the meta-schemas of the
 * two reserved namespaces itself.
 */
public final class UnifiedSchemaProvider implements SchemaProvider {
	private final Map<String, Schema> schemas = new LinkedHashMap<>();
	private final SchemaProvider schemaMeta = new SchemaProviderMeta();
	private final SchemaProvider templateMeta = new MetaTemplateSchemaProvider();

	/** Creates an empty provider, with the two meta-schemas already loaded. */
	public UnifiedSchemaProvider() {
	}

	/**
	 * Resolves the schema that applies to a namespace, serving the meta-schemas of the two
	 * reserved namespaces itself.
	 *
	 * @return the schema of the namespace, or {@code null} if none has been registered for it.
	 */
	@Override
	public Schema getSchema(String namespace) {
		String key = StringUtils.lowerCase(namespace);
		if (TemplateParser.TEMPLATE_NAMESPACE.equals(namespace))
			return templateMeta.getSchema(key);
		if (Schema.SCHEMA_NAMESPACE.equals(namespace))
			return schemaMeta.getSchema(key);
		return schemas.get(key);
	}

	/**
	 * Parses a document and registers every schema or template it defines, each one under its own
	 * namespace. Root nodes of any other namespace are ignored.
	 *
	 * @param text text of the document to load.
	 * @throws dev.stxt.exceptions.ParseException if the document cannot be parsed.
	 * @throws ValidationException the first one, if a schema or a template does not validate
	 *         against its meta-schema.
	 */
	public void addFile(String text) {
		for (Node node : new Parser().parse(text)) {
			String namespace = node.getNamespace();
			if (TemplateParser.TEMPLATE_NAMESPACE.equals(namespace))
				addTemplateNode(node);
			else if (Schema.SCHEMA_NAMESPACE.equals(namespace))
				addSchemaNode(node);
		}
	}

	private void addTemplateNode(Node node) {
		throwIfInvalid(new SchemaValidator(templateMeta, true).validate(node));
		Schema schema = TemplateParser.transformNodeToSchema(node);
		schemas.put(StringUtils.lowerCase(schema.getNamespace()), schema);
	}

	private void addSchemaNode(Node node) {
		throwIfInvalid(new SchemaValidator(schemaMeta, true).validate(node));
		Schema schema = SchemaParser.transformNodeToSchema(node);
		schemas.put(StringUtils.lowerCase(schema.getNamespace()), schema);
	}

	// A schema/template that does not validate against its meta-schema must not be loaded
	private static void throwIfInvalid(List<ValidationException> errors) {
		if (!errors.isEmpty())
			throw errors.get(0);
	}

	/** Removes every schema and template registered in this provider. */
	public void clear() {
		schemas.clear();
	}

	/** {@return every schema registered in this provider, in registration order} */
	public List<Schema> getAllSchemas() {
		return Collections.unmodifiableList(new ArrayList<>(schemas.values()));
	}
}
