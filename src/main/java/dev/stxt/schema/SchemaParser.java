package dev.stxt.schema;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import dev.stxt.NameNamespace;
import dev.stxt.NameNamespaceParser;
import dev.stxt.Node;
import dev.stxt.exceptions.ParseException;
import dev.stxt.exceptions.SchemaException;
import dev.stxt.exceptions.ValidationException;

class SchemaParser {

	public static Schema transformNodeToSchema(Node node) {
		Schema schema = new Schema();

		// Node name
		String nodeName = node.getNormalizedName();
		String namespaceSchema = node.getNamespace();

		// Obtenemos name y namespace
		if (!nodeName.equals("schema") || !namespaceSchema.equals(Schema.SCHEMA_NAMESPACE)) {
			throw new SchemaException("NOT_STXT_SCHEMA",
					"Se espera schema(" + Schema.SCHEMA_NAMESPACE + ") y es " + nodeName + "(" + namespaceSchema + ")");
		}
		String namespace = node.getValue().trim().toLowerCase(Locale.ROOT);
		schema.setNamespace(namespace);

		// Para validar
		Set<String> allNames = new HashSet<String>(); // Para validar que existan los childs
		
		// Obtenemos los nodos
		for (Node n : node.getChildren("node")) {
			NodeDefinition schNode = createFrom(n, schema.getNamespace());
			schema.addNodeDefinition(schNode);
			allNames.add(schNode.getNormalizedName());
		}

		// Validamos que todos los nombres estén definidos
		for (NodeDefinition schNode : schema.getNodes().values()) {
			for (ChildDefinition schChild : schNode.getChildren().values()) {
				if (schChild.getNamespace().equals(namespace)) // Sólo validamos del mismo namespace
				{
					if (!allNames.contains(schChild.getNormalizedName()))
						throw new ValidationException(0, "CHILD_NOT_DEFINED",
								"Child " + schChild.getNormalizedName() + " not defined in " + namespace);
				}
			}
		}

		return schema;
	}

	private static NodeDefinition createFrom(Node n, String namespace) {
		NodeDefinition result = new NodeDefinition();

		String name = n.getValue();
		String type = "VALUE_NODE";
		Node typeNode = n.getChild("type");
		if (typeNode != null) type = typeNode.getValue();

		result.setName(name);
		result.setType(type);

		Node children = n.getChild("children");
		if (children != null) {
			for (Node child: children.getChildren("child"))
				putChildToSchemaNode(result, child, namespace);
		}
		
		return result;
	}

	private static void putChildToSchemaNode(NodeDefinition schemaNode, Node child, String defNamespace) {
		// Obtenemos name y namespace
		NameNamespace ns = NameNamespaceParser.parse(child.getValue(), defNamespace, child.getLine(), child.getValue());
		String name = ns.getName();
		String namespace = ns.getNamespace() != null ? ns.getNamespace(): defNamespace;
		
		ChildDefinition schemaChild = new ChildDefinition(name, namespace, getInteger(child, "min"), getInteger(child, "max"));
		schemaNode.addChildDefinition(schemaChild);
	}

	private static Integer getInteger(Node node, String name) {
		Node n = node.getChild(name);
		if (n == null) return null;
		
		try
		{
			return Integer.parseInt(n.getValue());
		}
		catch (Exception e)
		{
			throw new ParseException(node.getLine(), "INVALID_INTEGER", "Integer not valid: " + n.getValue());
		}
	}


}