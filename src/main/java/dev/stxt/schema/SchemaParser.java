package dev.stxt.schema;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import dev.stxt.NameNamespace;
import dev.stxt.NameNamespaceParser;
import dev.stxt.Node;
import dev.stxt.ParseException;
import dev.stxt.processors.ValidationException;
import dev.stxt.utils.StringUtils;

class SchemaParser {

	public static Schema transformNodeToSchema(Node node) {
		Schema schema = new Schema();

		// Node name
		String nodeName = node.getName();
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
			SchemaNode schNode = createFrom(n, schema.getNamespace());
			schema.getNodes().put(schNode.getName(), schNode);
			allNames.add(schNode.getName());
		}

		// Validamos que todos los nombres estén definidos
		for (SchemaNode schNode : schema.getNodes().values()) {
			for (SchemaChild schChild : schNode.getChildren().values()) {
				if (schChild.getNamespace().equals(namespace)) // Sólo validamos del mismo namespace
				{
					if (!allNames.contains(schChild.getName()))
						throw new ValidationException(0, "CHILD_NOT_DEFINED",
								"Child " + schChild.getName() + " not defined in " + namespace);
				}
			}
		}

		return schema;
	}

	private static SchemaNode createFrom(Node n, String namespace) {
		SchemaNode result = new SchemaNode();

		String name = n.getValue();
		String type = "TEXT_VALUE";
		Node typeNode = n.getChild("type");
		if (typeNode != null) type = typeNode.getValue();

		result.setName(StringUtils.normalizeSimple(name));
		result.setType(type);

		Node children = n.getChild("children");
		if (children != null) {
			for (Node child: children.getChildren("child"))
				putChildToSchemaNode(result, child, namespace);
		}
		
		return result;
	}

	private static void putChildToSchemaNode(SchemaNode schemaNode, Node child, String defNamespace) {
		// Obtenemos name y namespace
		NameNamespace ns = NameNamespaceParser.parse(child.getValue(), defNamespace, child.getLine(), child.getValue());
		String name = ns.getName();
		String namespace = ns.getNamespace() != null ? ns.getNamespace(): defNamespace;
		
		SchemaChild schemaChild = new SchemaChild();
		schemaChild.setName(name);
		schemaChild.setNamespace(namespace);
		schemaChild.setMin(getInteger(child, "min"));
		schemaChild.setMax(getInteger(child, "max"));
		
		schemaNode.getChildren().put(schemaChild.getQualifiedName(), schemaChild);
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