package dev.stxt.schema;

import java.util.List;
import java.util.function.Function;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.exceptions.ParseException;
import dev.stxt.exceptions.ValidationException;

/**
 * The one pipeline every definition loader shares, whatever the store: the in-memory
 * providers (a document each), {@link dev.stxt.runtime.UnifiedSchemaProvider} (several
 * roots per file), the resource loaders and discovery. A definition node is validated
 * against the meta-schema of its kind and, only when valid, transformed into a
 * {@link Schema}; a definition that does not validate is never registered anywhere —
 * the first validation finding is thrown instead.
 * Mirrors {@code stxt-impl/schema/definition_compiler.txt}.
 */
public final class DefinitionCompiler {

	private DefinitionCompiler() {
	}

	/**
	 * Validates one root node against the meta provider of its kind and compiles it into a
	 * {@link Schema}.
	 *
	 * @param node root node of the definition ({@code Schema (@stxt.schema)} or
	 *        {@code Template (@stxt.template)}).
	 * @param meta provider of the meta-schema of the kind.
	 * @param transform function that turns the validated node into a {@link Schema}.
	 * @return the compiled schema.
	 * @throws ValidationException the first validation finding, if the node does not validate.
	 */
	public static Schema compileNode(Node node, SchemaProvider meta, Function<Node, Schema> transform) {
		List<ValidationException> errors = new SchemaValidator(meta, true).validate(node);

		if (!errors.isEmpty()) {
			throw errors.get(0);
		}

		return transform.apply(node);
	}

	/**
	 * Parses a whole document that must hold exactly one definition, and compiles it.
	 *
	 * @param text text of the definition document.
	 * @param meta provider of the meta-schema of the kind.
	 * @param transform function that turns the validated root into a {@link Schema}.
	 * @param multipleRootsCode error code when the document does not hold exactly one root
	 *        ({@code SCHEMA_MULTIPLE_ROOTS} for schemas, {@code TEMPLATE_MULTIPLE_ROOTS} for templates).
	 * @param kind word naming the kind in the error message ({@code schema} or {@code template}).
	 * @return the compiled schema.
	 * @throws ParseException or {@link ValidationException} if the document is not a valid definition.
	 */
	public static Schema compileDocument(String text, SchemaProvider meta, Function<Node, Schema> transform,
			String multipleRootsCode, String kind) {
		List<Node> nodes = new Parser().parse(text);

		if (nodes.size() != 1) {
			throw new ValidationException(ParseException.NO_LINE, multipleRootsCode,
					"A " + kind + " document must hold exactly 1 root node, got " + nodes.size());
		}

		return compileNode(nodes.get(0), meta, transform);
	}
}
