package dev.stxt.schema;

import dev.stxt.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.stxt.Node;
import dev.stxt.ParseException;
import dev.stxt.resources.NotFoundException;

class SchemaValidator {
	// ------------------
	// Variables públicas
	// ------------------

	private final SchemaProvider schemaProvider;

	public SchemaValidator(SchemaProvider schemaProvider) {
		this.schemaProvider = schemaProvider;
	}

	public Node validateNode(Node node) throws ParseException, IOException, NotFoundException {
		// Obtenemos namespace
		String namespace = node.getNamespace();
		Schema sch = schemaProvider.getSchema(namespace);
		if (sch == null)
			throw new ParseException(node.getLine(), "SCHEMA_NOT_FOUND", "Not found schema: " + sch);

		// Validamos nodo y childs
		validateAgainstSchema(node, sch);
		validateNodes(node.getChildren());

		return node;
	}

	public List<Node> validateNodes(List<Node> nodes) throws ParseException, IOException, NotFoundException {
		if (nodes == null)
			return null;

		for (Node n : nodes)
			validateNode(n);

		return nodes;
	}

	public void validateAgainstSchema(Node node, Schema sch) throws ParseException {
		// Obtenemos namespace
		String namespace = node.getNamespace();
		if (!namespace.equals(sch.namespace))
			throw new IllegalArgumentException(
					"Node namespace " + namespace + " not match with schema namespace: " + sch.namespace);

		// Obtenemos node
		SchemaNode schemaNode = sch.nodes.get(node.getName());
		if (schemaNode == null) {
			String error = "NOT EXIST NODE " + node.getName() + " for namespace " + namespace;
			throw new ParseException(node.getLine(), "NODE_NOT_EXIST_IN_SCHEMA", error);
		}

		// Validamos nodo
		validateValue(schemaNode, node);
		validateCount(schemaNode, node);
	}

	private static void validateValue(SchemaNode nsNode, Node n) throws ParseException {
		String nodeType = nsNode.type;

		TypeValidator validator = TypeRegistry.get(nodeType);
		if (validator == null)
			throw new ParseException(n.getLine(), "TYPE_NOT_SUPPORTED", "Node type not supported: " + nodeType);

		validator.validate(n);
	}

	private static void validateCount(SchemaNode nsNode, Node node) throws ParseException {
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

	private static void validateCount(SchemaChild chNode, int num, Node node) throws ParseException {
		Integer min = chNode.min;
		Integer max = chNode.max;

		if (min != null && num < min)
			throw new ParseException(node.getLine(), "INVALID_NUMBER",
					num + " nodes of '" + chNode.getQualifiedName() + " and min is " + min);

		if (max != null && num > max)
			throw new ParseException(node.getLine(), "INVALID_NUMBER",
					num + " nodes of '" + chNode.getQualifiedName() + " and max is " + max);
	}

}
