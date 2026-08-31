package dev.stxt.template;

import dev.stxt.exceptions.ParseException;
import dev.stxt.exceptions.ResourceNotFoundException;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.resources.ResourcesLoader;
import dev.stxt.schema.DefinitionCompiler;
import dev.stxt.schema.Schema;
import dev.stxt.schema.SchemaProvider;

/** {@link SchemaProvider} that loads an {@code @stxt.template} document from a {@link ResourcesLoader} and turns it into a {@link Schema}. */
public class TemplateSchemaProvider implements SchemaProvider {
	private final ResourcesLoader loader;
	
	/**
	 * Creates a provider that loads templates through a {@link ResourcesLoader}.
	 *
	 * @param loader where to load the {@code @stxt.template} document of each namespace from.
	 */
	public TemplateSchemaProvider(ResourcesLoader loader) {
		this.loader = loader;
	}	
	
	@Override
	public Schema getSchema(String namespace) {
		// A missing resource is "no template for this namespace", not an error (SchemaProvider contract)
		String template;
		try {
			template = loader.retrieve(Schema.TEMPLATE_NAMESPACE, namespace);
		}
		catch (ResourceNotFoundException e) {
			return null;
		}

		// The whole load pipeline is the shared one of DefinitionCompiler
		Schema sch = DefinitionCompiler.compileDocument(template, new MetaTemplateSchemaProvider(),
				TemplateParser::transformNodeToSchema, "TEMPLATE_MULTIPLE_ROOTS", "template");

		// Facade check, outside the normative scope: the document must define the requested namespace
		if (!sch.getNamespace().equalsIgnoreCase(namespace))
			throw new ValidationException(ParseException.NO_LINE, "TEMPLATE_ROOT_NOT_VALID",
					"Template namespace is " + sch.getNamespace() + ", and expected is " + namespace);

		return sch;
	}
}
