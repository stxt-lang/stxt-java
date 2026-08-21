package dev.stxt.schema;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.stxt.NameNamespace;
import dev.stxt.NameNamespaceParser;
import dev.stxt.NamespaceValidator;
import dev.stxt.InlineNode;
import dev.stxt.Node;
import dev.stxt.exceptions.ParseException;
import dev.stxt.utils.StringUtils;
import dev.stxt.exceptions.ValidationException;

/** Turns the tree of an already parsed {@code @stxt.schema} document into a {@link Schema}. */
public class SchemaParser {

	private SchemaParser() {
	}

	/**
	 * Transforms the document tree into the {@link Schema} it describes.
	 *
	 * @param node root of the already parsed {@code @stxt.schema} document.
	 * @return the resulting {@link Schema}.
	 */
	public static Schema transformNodeToSchema(Node node) {
		// Node name
		String nodeName = node.getCanonicalName();
		String namespaceSchema = node.getNamespace();

		// Get the name and the namespace
		if (!nodeName.equals("schema") || !namespaceSchema.equals(Schema.SCHEMA_NAMESPACE)) {
			throw new ValidationException(node.getLine(), "SCHEMA_ROOT_NOT_VALID",
					"Expected schema(" + Schema.SCHEMA_NAMESPACE + ") but got " + nodeName + "(" + namespaceSchema + ")");
		}
		InlineNode root = inline(node);

		// STXT-SCHEMA-SPEC 13.1: the value of the root is the target namespace, and it must be a valid one
		String targetNamespace = root.getValue();
		if (targetNamespace.isEmpty())
			throw new ValidationException(root.getLine(), "SCHEMA_NAMESPACE_EMPTY", "Schema root must declare the target namespace");
		try {
			NamespaceValidator.validateNamespaceFormat(StringUtils.lowerCase(targetNamespace), root.getLine());
		} catch (ParseException e) {
			throw new ValidationException(root.getLine(), "SCHEMA_ROOT_NOT_VALID", "Schema namespace not valid: " + targetNamespace);
		}
		// STXT-SCHEMA-SPEC 6.1: optional description of the schema
		Node descriptionNode = root.getChild("description");
		String description = descriptionNode == null ? null : descriptionNode.getText();
		Schema schema = new Schema(targetNamespace, root.getLine(), description);

		// For validation
		Set<String> allNames = new HashSet<String>(); // To check that the children exist

		// Get the nodes
		for (Node n : root.getChildren("node")) {
			NodeDefinition schNode = createFrom(n, schema.getNamespace());
			schema.addNodeDefinition(schNode);
			allNames.add(schNode.getCanonicalName());
		}

		// Check that every name is defined
		for (NodeDefinition schNode : schema.getNodes().values()) {
			for (ChildDefinition schChild : schNode.getChildren().values()) {
				if (schChild.getNamespace().equals(schema.getNamespace())) // We only check the ones in the same namespace
				{
					if (!allNames.contains(schChild.getCanonicalName()))
						throw new ValidationException(0, "CHILD_NOT_DEFINED",
								"Child " + schChild.getCanonicalName() + " not defined in " + schema.getNamespace());
				}
			}
		}

		return schema;
	}

	// The schema language is written with inline nodes; anything else is not a schema
	private static InlineNode inline(Node node) {
		if (node instanceof InlineNode inline)
			return inline;
		throw new ValidationException(node.getLine(), "SCHEMA_NODE_NOT_INLINE", "Node '" + node.getName() + "' must be inline in a schema");
	}

	private static NodeDefinition createFrom(Node node, String namespace) {
		InlineNode n = inline(node);
		String name = n.getText();
		String type = "INLINE";
		Node typeNode = n.getChild("type");
		if (typeNode != null) type = typeNode.getText();

		// STXT-SCHEMA-SPEC 7.1: optional description of the node
		Node descriptionNode = n.getChild("description");
		String description = descriptionNode == null ? null : descriptionNode.getText();

		NodeDefinition result = new NodeDefinition(name, type, n.getLine(), description);
		
		Node children = n.getChild("children");
		if (children != null) {
			// STXT-SCHEMA-SPEC 7.1/9.1/13.5: only INLINE and GROUP accept children
			if (!TypeRegistry.admitsChildren(type))
				throw new ValidationException(children.getLine(), "CHILDREN_NOT_ALLOWED_FOR_TYPE",
						"Type " + type + " does not allow children (node " + name + ")");
			for (Node child: inline(children).getChildren("child"))
				putChildToSchemaNode(result, child, namespace);
		}
		
		// Look at the values
		List<Node> values = n.getChildren("values");
		if (values != null && values.size()>0) {
		    if (!type.equals("ENUM")) 
		        throw new ParseException(n.getLine(), "VALUES_NOT_ALLOWED_FOR_TYPE", "Values only supported for type ENUM, not for type " + type);
		    
		    // STXT-SCHEMA-SPEC 13.1: a Node carries at most one Values; the error points at the second one
		    if (values.size()>1)
		        throw new ValidationException(values.get(1).getLine(), "VALUES_DUPLICATED", "Values defined " + values.size() + " times for node " + name);
		    
		    Node valuesNode = values.get(0);
		    values = inline(valuesNode).getChildren("value");
		    for (Node value: values) {
		        // STXT-SCHEMA-SPEC 7.2, condition 14: an empty Value: is a schema error
		        if (value.getText() == null || value.getText().isEmpty())
		            throw new ValidationException(value.getLine(), "VALUE_EMPTY", "Value of ENUM cannot be empty");
		        result.addValue(value.getText(), value.getLine());
		    }
		}
		
		// Look at the enum
		if (type.equals("ENUM") && (values == null || values.size()==0))
		    throw new ParseException(n.getLine(), "VALUES_REQUIRED", "ENUM Type must include values");
		
		return result;
	}

	private static void putChildToSchemaNode(NodeDefinition schemaNode, Node childNode, String defNamespace) {
		InlineNode child = inline(childNode);

		// Get the name and the namespace
		NameNamespace ns = NameNamespaceParser.parse(child.getText(), defNamespace, child.getLine(), child.getText());
		String name = ns.getName();
		String namespace = ns.getNamespace();
		
		Integer min = getInteger(child, "min");
		Integer max = getInteger(child, "max");
		// STXT-SCHEMA-SPEC 10/13.7: the cardinality is invalid when Min > Max
		if (min != null && max != null && min > max)
			throw new ValidationException(child.getLine(), "MIN_GREATER_THAN_MAX",
					"Min " + min + " greater than Max " + max);

		ChildDefinition schemaChild = new ChildDefinition(name, namespace, min, max, child.getLine());
		schemaNode.addChildDefinition(schemaChild);
	}

	private static Integer getInteger(InlineNode node, String name) {
		Node n = node.getChild(name);
		if (n == null) return null;
		
		try	{
			return Integer.parseInt(n.getText());
		} catch (Exception e) {
			throw new ParseException(node.getLine(), "CARDINALITY_NOT_VALID", "Integer not valid: " + n.getText());
		}
	}
}