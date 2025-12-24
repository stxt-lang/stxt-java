package dev.stxt.schema;

import java.util.HashMap;
import java.util.Map;

import dev.stxt.Node;
import dev.stxt.processors.ValidationException;
import dev.stxt.processors.Validator;

class SchemaValidator implements Validator {
	// ------------------
	// Variables públicas
	// ------------------

	private final SchemaProvider schemaProvider;
	private boolean recursive = false;

	public SchemaValidator(SchemaProvider schemaProvider) {
		this.schemaProvider = schemaProvider;
	}
	public SchemaValidator(SchemaProvider schemaProvider, boolean recursive) {
		this.schemaProvider = schemaProvider;
		this.recursive = recursive;
	}

	@Override
	public void validate(Node node) {
		// Obtenemos namespace
		String namespace = node.getNamespace();
		Schema sch = schemaProvider.getSchema(namespace);
		if (sch == null)
			throw new ValidationException(node.getLine(), "SCHEMA_NOT_FOUND", "Not found schema: " + namespace);

		// Validamos nodo
		validateAgainstSchema(node, sch);
		
		// Validamos children
		if (recursive)
			for (Node n: node.getChildren())
				validate(n);
	}
	
	public void validateAgainstSchema(Node node, Schema sch) {
		// Obtenemos node
		SchemaNode schemaNode = sch.getNodes().get(node.getName());
		if (schemaNode == null) {
			String error = "NOT EXIST NODE " + node.getName() + " for namespace " + sch.getNamespace();
			throw new ValidationException(node.getLine(), "NODE_NOT_EXIST_IN_SCHEMA", error);
		}

		// Validamos nodo
		validateValue(schemaNode, node);
		validateCount(schemaNode, node);
	}

	private static void validateValue(SchemaNode nsNode, Node n) {
		String nodeType = nsNode.getType();

		TypeValidator validator = TypeRegistry.get(nodeType);
		if (validator == null)
			throw new ValidationException(n.getLine(), "TYPE_NOT_SUPPORTED", "Node type not supported: " + nodeType);

		validator.validate(n);
	}

	private static void validateCount(SchemaNode nsNode, Node node) {
		Map<String, Integer> count = new HashMap<>();

		for (Node child : node.getChildren()) {
			// Count childs
			String childName = child.getQualifiedName();
			count.put(childName, count.getOrDefault(childName, 0) + 1);
		}

		for (SchemaChild chNode : nsNode.getChildren().values()) {
			validateCount(chNode, count.getOrDefault(chNode.getQualifiedName(), 0), node);
		}
	}

	private static void validateCount(SchemaChild chNode, int num, Node node) {
		Integer min = chNode.getMin();
		Integer max = chNode.getMax();

		if (min != null && num < min)
			throw new ValidationException(node.getLine(), "INVALID_NUMBER",
					num + " nodes of '" + chNode.getQualifiedName() + " and min is " + min);

		if (max != null && num > max)
			throw new ValidationException(node.getLine(), "INVALID_NUMBER",
					num + " nodes of '" + chNode.getQualifiedName() + " and max is " + max);
	}


}
