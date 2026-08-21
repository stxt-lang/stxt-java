package dev.stxt.template;

import java.util.List;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.schema.Schema;
import dev.stxt.schema.SchemaProvider;
import dev.stxt.schema.SchemaProviderMemory;
import dev.stxt.schema.SchemaValidator;

/**
 * In-memory {@link SchemaProvider} fed with {@code @stxt.template} documents: each template is
 * turned into its equivalent {@link Schema} and registered under its own namespace.
 */
public class TemplateSchemaProviderMemory extends SchemaProviderMemory {

	/** Creates an empty provider that falls back to the template meta-schema provider. */
	public TemplateSchemaProviderMemory() {
		this(null);
	}

	/**
	 * Creates an empty provider.
	 *
	 * @param parent provider to fall back to when a namespace is not registered here; the
	 *        template meta-schema provider when {@code null}.
	 */
	public TemplateSchemaProviderMemory(SchemaProvider parent) {
		super(parent != null ? parent : new MetaTemplateSchemaProvider());
	}

	/**
	 * Parses a template document, validates it against the template meta-schema and registers the
	 * schema it produces.
	 *
	 * @param template text of the {@code @stxt.template} document.
	 * @throws ValidationException with {@code TEMPLATE_MULTIPLE_ROOTS} if the document does not hold
	 *         exactly one root node, {@code TEMPLATE_ROOT_NOT_VALID} or {@code TEMPLATE_NAMESPACE_EMPTY}
	 *         if that root is not {@code Template (@stxt.template): ns}, or the first validation error
	 *         if the template does not validate against the template meta-schema.
	 */
	public void addTemplate(String template) {
		List<Node> nodes = new Parser().parse(template);
		if (nodes.size() != 1)
			throw new ValidationException(0, "TEMPLATE_MULTIPLE_ROOTS", "A template document must hold exactly 1 root node, got " + nodes.size());

		// A template that does not validate against the template meta-schema must not be registered
		List<ValidationException> errors = new SchemaValidator(new MetaTemplateSchemaProvider(), true).validate(nodes.get(0));
		if (!errors.isEmpty())
			throw errors.get(0);

		Schema schema = TemplateParser.transformNodeToSchema(nodes.get(0));
		schemas.put(schema.getNamespace(), schema);
	}
}
