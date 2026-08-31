package dev.stxt.template;

import java.util.List;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.exceptions.ParseException;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.schema.Schema;
import dev.stxt.schema.SchemaProvider;

/**
 * {@link SchemaProvider} that defines in code the meta-schema of the template language itself
 * ({@code @stxt.template}), the counterpart of {@link dev.stxt.schema.SchemaProviderMeta} for schemas.
 */
public class MetaTemplateSchemaProvider implements SchemaProvider {

	private static final String META_TEXT = """
Template (@stxt.template): @stxt.template
	Structure >>
		Template (@stxt.template):
			Description: (?) TEXT
			Structure: (1) BLOCK
""";
			
	/**
	 * Compiled once per process and shared between instances, exactly like the meta field of
	 * {@link dev.stxt.schema.SchemaProviderMeta}.
	 */
	private static Schema compiledMeta;

	private final Schema meta;

	/** Compiles the meta-template the first time and keeps the schema it produces ready to be served. */
	public MetaTemplateSchemaProvider() {
		meta = compiledMeta();
	}

	// Lazy, thread-safe compilation of META_TEXT, shared by every instance.
	private static synchronized Schema compiledMeta() {
		if (compiledMeta == null) {
			Parser parser = new Parser();
			List<Node> nodes = parser.parse(META_TEXT);
			if (nodes.size() != 1)
				throw new ValidationException(ParseException.NO_LINE, "META_SCHEMA_INVALID",
						"Meta schema must produce exactly 1 document, got " + nodes.size());

			// The meta-template itself is compiled with the TemplateParser
			compiledMeta = TemplateParser.transformNodeToSchema(nodes.get(0));
		}
		return compiledMeta;
	}

	/**
	 * Serves the meta-template. Follows the {@link SchemaProvider} contract: providers never throw
	 * "not found", so any namespace other than {@code @stxt.template} yields {@code null}.
	 *
	 * @return the meta-schema of the template language, or {@code null} for any other namespace.
	 */
	@Override
	public Schema getSchema(String namespace) {
		if (!Schema.TEMPLATE_NAMESPACE.equals(namespace))
			return null;

	    return meta;
	}
}
