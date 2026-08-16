package dev.stxt.schema;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.stxt.NameNamespace;
import dev.stxt.NameNamespaceParser;
import dev.stxt.Node;
import dev.stxt.exceptions.ParseException;
import dev.stxt.exceptions.STXTException;
import dev.stxt.exceptions.SchemaException;
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
			throw new SchemaException("NOT_STXT_SCHEMA",
					"Expected schema(" + Schema.SCHEMA_NAMESPACE + ") but got " + nodeName + "(" + namespaceSchema + ")");
		}
		Schema schema = new Schema(node.getText(), node.getLine());

		// For validation
		Set<String> allNames = new HashSet<String>(); // To check that the children exist

		// Get the nodes
		for (Node n : node.getChildren("node")) {
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

	private static NodeDefinition createFrom(Node n, String namespace) {
		String name = n.getText();
		String type = "INLINE";
		Node typeNode = n.getChild("type");
		if (typeNode != null) type = typeNode.getText();

		NodeDefinition result = new NodeDefinition(name, type, n.getLine());
		
		// STXT-SCHEMA-SPEC 7.1: optional description of the node
		Node descriptionNode = n.getChild("description");
		if (descriptionNode != null)
			result.setDescription(descriptionNode.getText());
		
		Node children = n.getChild("children");
		if (children != null) {
			// STXT-SCHEMA-SPEC 7.1/9.1/13.5: only INLINE and GROUP accept children
			if (!TypeRegistry.admitsChildren(type))
				throw new ValidationException(children.getLine(), "CHILDREN_NOT_ALLOWED_FOR_TYPE",
						"Type " + type + " does not allow children (node " + name + ")");
			for (Node child: children.getChildren("child"))
				putChildToSchemaNode(result, child, namespace);
		}
		
		// Look at the values
		List<Node> values = n.getChildren("values");
		if (values != null && values.size()>0) {
		    if (!type.equals("ENUM")) 
		        throw new ParseException(n.getLine(), "VALUES_ONLY_SUPPORTED_BY_ENUM", "Values only supported for type ENUM, not for type " + type);
		    
		    if (values.size()>1)
		        throw new STXTException("INVALID_SIZE_VALUES", "Unexpected number of values: " + values.size());
		    
		    Node valuesNode = values.get(0);
		    values = valuesNode.getChildren("value");
		    for (Node value: values)
		        result.addValue(value.getText(), value.getLine());
		}
		
		// Look at the enum
		if (type.equals("ENUM") && (values == null || values.size()==0))
		    throw new ParseException(n.getLine(), "VALUES_EMPTY_FOR_ENUM", "ENUM Type must include values");
		
		return result;
	}

	private static void putChildToSchemaNode(NodeDefinition schemaNode, Node child, String defNamespace) {
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

	private static Integer getInteger(Node node, String name) {
		Node n = node.getChild(name);
		if (n == null) return null;
		
		try	{
			return Integer.parseInt(n.getText());
		} catch (Exception e) {
			throw new ParseException(node.getLine(), "INVALID_INTEGER", "Integer not valid: " + n.getText());
		}
	}
}