package dev.stxt.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.stxt.InlineNode;
import dev.stxt.Node;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.processors.Validator;

/** {@link Validator} that, for each node, resolves its {@link Schema} through a {@link SchemaProvider} and validates type and cardinality. */
public class SchemaValidator implements Validator {
	private final SchemaProvider schemaProvider;
	private boolean recursiveValidation = false;

	/**
	 * Creates a validator that resolves schemas through the given provider.
	 *
	 * @param schemaProvider where to resolve the schema of each namespace from. Without recursive validation of the children.
	 */
	public SchemaValidator(SchemaProvider schemaProvider) {
		this.schemaProvider = schemaProvider;
	}
	/**
	 * Creates a validator, optionally validating the children of each node too.
	 *
	 * @param schemaProvider where to resolve the schema of each namespace from.
	 * @param recursive whether the children of each node are validated recursively too.
	 */
	public SchemaValidator(SchemaProvider schemaProvider, boolean recursive) {
		this.schemaProvider = schemaProvider;
		this.recursiveValidation = recursive;
	}

	@Override
	public List<ValidationException> validate(Node node) {
		List<ValidationException> errors = new ArrayList<>();

		// Get the namespace
		String namespace = node.getNamespace();

		// The empty namespace is never validated (STXT-SCHEMA-SPEC 5): a node that neither
		// declares nor inherits a namespace is valid by definition, no schema is looked up for
		// it and SCHEMA_NOT_FOUND is never reported for it. Its children are still walked when
		// recursive, because one of them may declare a namespace of its own.
		if (namespace.isEmpty()) {
			if (recursiveValidation && node instanceof InlineNode inline)
				for (Node n: inline.getChildren())
					errors.addAll(validate(n));
			return errors;
		}

		Schema sch = schemaProvider.getSchema(namespace);
		if (sch == null) {
			errors.add(new ValidationException(node.getLine(), "SCHEMA_NOT_FOUND", "Not found schema: " + namespace));
			return errors;
		}

		// Validate the node
		errors.addAll(validateAgainstSchema(node, sch));

		// Validate the children (only an inline node has any)
		if (recursiveValidation && node instanceof InlineNode inline)
			for (Node n: inline.getChildren())
				errors.addAll(validate(n));

		return errors;
	}
	
	/**
	 * Validates a node against an already resolved schema: existence, value type and cardinalities of its children.
	 *
	 * @param node node to validate.
	 * @param sch schema to validate against.
	 * @return the validation errors found, empty if the node is valid.
	 */
	public List<ValidationException> validateAgainstSchema(Node node, Schema sch) {
		List<ValidationException> errors = new ArrayList<>();

	    NodeDefinition schemaNode = sch.getNodeDefinition(node.getCanonicalName());
	    if (schemaNode == null) {
	        String error = "NOT EXIST NODE " + node.getCanonicalName() + " for namespace " + sch.getNamespace();
	        errors.add(new ValidationException(node.getLine(), "NODE_NOT_DEFINED_IN_SCHEMA", error));
	        return errors;
	    }

	    errors.addAll(validateValue(schemaNode, node));
	    errors.addAll(validateChildrenDeclared(schemaNode, node));
	    errors.addAll(validateCount(schemaNode, node));

	    return errors;
	}

	// Closed content model (STXT-SCHEMA-SPEC, section 6): only the direct children declared
	// in the parent definition are allowed; with no Children, nothing at all is allowed
	private static List<ValidationException> validateChildrenDeclared(NodeDefinition nsNode, Node node) {
		List<ValidationException> errors = new ArrayList<>();

		for (Node child : childrenOf(node)) {
			if (!nsNode.getChildren().containsKey(child.getQualifiedName()))
				errors.add(new ValidationException(child.getLine(), "CHILD_NOT_DECLARED",
						"Child '" + child.getQualifiedName() + "' not declared in node '" + node.getQualifiedName() + "'"));
		}

		return errors;
	}

	// The children of a node for the purposes of the content model: a text node has none
	private static List<Node> childrenOf(Node node) {
		return node instanceof InlineNode inline ? inline.getChildren() : List.of();
	}

	private static List<ValidationException> validateValue(NodeDefinition nsNode, Node n) {
		List<ValidationException> errors = new ArrayList<>();
		String nodeType = nsNode.getType();

		Type validator = TypeRegistry.get(nodeType);
		if (validator == null) {
			errors.add(new ValidationException(n.getLine(), "TYPE_NOT_VALID", "Node type not supported: " + nodeType));
			return errors;
		}

		try {
			validator.validate(nsNode, n);
		} catch (ValidationException e) {
			errors.add(e);
		} catch (RuntimeException e) {
			errors.add(new ValidationException(n.getLine(), "UNEXPECTED_ERROR", e.getMessage()));
		}

		return errors;
	}

	private static List<ValidationException> validateCount(NodeDefinition nsNode, Node node) {
		List<ValidationException> errors = new ArrayList<>();
		Map<String, Integer> count = new HashMap<>();

		for (Node child : childrenOf(node)) {
			// Count childs
			String childName = child.getQualifiedName();
			count.put(childName, count.getOrDefault(childName, 0) + 1);
		}

		for (ChildDefinition chNode : nsNode.getChildren().values()) {
			errors.addAll(validateCount(chNode, count.getOrDefault(chNode.getQualifiedName(), 0), node));
		}

		return errors;
	}

	private static List<ValidationException> validateCount(ChildDefinition chNode, int num, Node node) {
		List<ValidationException> errors = new ArrayList<>();
		Long min = chNode.getMin();
		Long max = chNode.getMax();

		if (min != null && num < min)
			errors.add(new ValidationException(node.getLine(), "TOO_FEW_CHILDREN",
					num + " nodes of '" + chNode.getQualifiedName() + "' and min is " + min));

		if (max != null && num > max)
			errors.add(new ValidationException(node.getLine(), "TOO_MANY_CHILDREN",
					num + " nodes of '" + chNode.getQualifiedName() + "' and max is " + max));

		return errors;
	}

}
