package dev.stxt.runtime;

import java.util.List;

import dev.stxt.Node;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.processors.Validator;
import dev.stxt.schema.SchemaValidator;

/**
 * Wrapper around a {@link SchemaValidator} that only validates namespaced nodes, so that a
 * document mixing schema-bound and free nodes does not report the free ones as unknown.
 *
 * <p>This is the rule of the language: a document without a namespace is not wrong, it just
 * cannot be validated. {@link STXT#parser(dev.stxt.resources.ResourcesLoader)} registers its
 * schema validator through this wrapper, and so do the command line and the editor extension.
 */
public final class ConditionalValidator implements Validator {
	private final SchemaValidator schemaValidator;

	/**
	 * Creates a validator that delegates to a schema validator.
	 *
	 * @param schemaValidator validator the namespaced nodes are handed over to.
	 */
	public ConditionalValidator(SchemaValidator schemaValidator) {
		this.schemaValidator = schemaValidator;
	}

	/**
	 * Validates a node when it has a namespace, and lets it through otherwise.
	 *
	 * @param node already closed node to validate.
	 * @return the validation errors found, or an empty list if the node is valid or has no namespace.
	 */
	@Override
	public List<ValidationException> validate(Node node) {
		// Only validate the node when it has a namespace
		if (!node.getNamespace().isEmpty())
			return schemaValidator.validate(node);
		return List.of();
	}
}
