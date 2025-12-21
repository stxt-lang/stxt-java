package dev.stxt.schema;

import java.util.HashMap;
import java.util.Map;

import dev.stxt.Node;
import dev.stxt.ValidationException;
import dev.stxt.Validator;

class SchemaValidator implements Validator {
	// ------------------
	// Variables públicas
	// ------------------

	private final SchemaProvider schemaProvider;

	public SchemaValidator(SchemaProvider schemaProvider) {
		this.schemaProvider = schemaProvider;
	}

	@Override
	public void validate(Node node) {
		// Obtenemos namespace
		String namespace = node.getNamespace();
		Schema sch = schemaProvider.getSchema(namespace);
		if (sch == null)
			throw new ValidationException(node.getLine(), "SCHEMA_NOT_FOUND", "Not found schema: " + namespace);

		// Validamos nodo y childs
		validateAgainstSchema(node, sch);
	}
	
	public void validateAgainstSchema(Node node, Schema sch) {
		// Obtenemos node
		SchemaNode schemaNode = sch.nodes.get(node.getName());
		if (schemaNode == null) {
			String error = "NOT EXIST NODE " + node.getName() + " for namespace " + sch.namespace;
			throw new ValidationException(node.getLine(), "NODE_NOT_EXIST_IN_SCHEMA", error);
		}

		// Validamos nodo
		validateValue(schemaNode, node);
		validateCount(schemaNode, node);
	}

	private static void validateValue(SchemaNode nsNode, Node n) {
		String nodeType = nsNode.type;

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

		for (SchemaChild chNode : nsNode.children.values()) {
			validateCount(chNode, count.getOrDefault(chNode.getQualifiedName(), 0), node);
		}
	}

	private static void validateCount(SchemaChild chNode, int num, Node node) {
		Integer min = chNode.min;
		Integer max = chNode.max;

		if (min != null && num < min)
			throw new ValidationException(node.getLine(), "INVALID_NUMBER",
					num + " nodes of '" + chNode.getQualifiedName() + " and min is " + min);

		if (max != null && num > max)
			throw new ValidationException(node.getLine(), "INVALID_NUMBER",
					num + " nodes of '" + chNode.getQualifiedName() + " and max is " + max);
	}


}
