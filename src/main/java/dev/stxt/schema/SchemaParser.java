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

/** Transforma el árbol de un documento {@code @stxt.schema} ya parseado en un {@link Schema}. */
public class SchemaParser {

	/**
	 * @param node raíz del documento {@code @stxt.schema} ya parseado.
	 * @return el {@link Schema} resultante.
	 */
	public static Schema transformNodeToSchema(Node node) {
		// Node name
		String nodeName = node.getNormalizedName();
		String namespaceSchema = node.getNamespace();

		// Obtenemos name y namespace
		if (!nodeName.equals("schema") || !namespaceSchema.equals(Schema.SCHEMA_NAMESPACE)) {
			throw new SchemaException("NOT_STXT_SCHEMA",
					"Se espera schema(" + Schema.SCHEMA_NAMESPACE + ") y es " + nodeName + "(" + namespaceSchema + ")");
		}
		Schema schema = new Schema(node.getValue(), node.getLine());

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
				if (schChild.getNamespace().equals(schema.getNamespace())) // Sólo validamos del mismo namespace
				{
					if (!allNames.contains(schChild.getNormalizedName()))
						throw new ValidationException(0, "CHILD_NOT_DEFINED",
								"Child " + schChild.getNormalizedName() + " not defined in " + schema.getNamespace());
				}
			}
		}

		return schema;
	}

	private static NodeDefinition createFrom(Node n, String namespace) {
		String name = n.getValue();
		String type = "INLINE";
		Node typeNode = n.getChild("type");
		if (typeNode != null) type = typeNode.getValue();

		NodeDefinition result = new NodeDefinition(name, type, n.getLine());
		
		// STXT-SCHEMA-SPEC 7.1: descripción opcional del nodo
		Node descriptionNode = n.getChild("description");
		if (descriptionNode != null)
			result.setDescription(descriptionNode.getText());
		
		Node children = n.getChild("children");
		if (children != null) {
			// STXT-SCHEMA-SPEC 7.1/9.1/13.5: sólo INLINE y GROUP admiten hijos
			if (!TypeRegistry.admitsChildren(type))
				throw new ValidationException(children.getLine(), "CHILDREN_NOT_ALLOWED_FOR_TYPE",
						"Type " + type + " does not allow children (node " + name + ")");
			for (Node child: children.getChildren("child"))
				putChildToSchemaNode(result, child, namespace);
		}
		
		// Miramos values
		List<Node> values = n.getChildren("values");
		if (values != null && values.size()>0) {
		    if (!type.equals("ENUM")) 
		        throw new ParseException(n.getLine(), "VALUES_ONLY_SUPPORTED_BY_ENUM", "Values only supported for type ENUM, not for type " + type);
		    
		    if (values.size()>1)
		        throw new STXTException("INVALID_SIZE_VALUES", "Unexpected number of values: " + values.size());
		    
		    Node valuesNode = values.get(0);
		    values = valuesNode.getChildren("value");
		    for (Node value: values)
		        result.addValue(value.getValue(), value.getLine());
		}
		
		// Miramos enum
		if (type.equals("ENUM") && (values == null || values.size()==0))
		    throw new ParseException(n.getLine(), "VALUES_EMPTY_FOR_ENUM", "ENUM Type must include values");
		
		return result;
	}

	private static void putChildToSchemaNode(NodeDefinition schemaNode, Node child, String defNamespace) {
		// Obtenemos name y namespace
		NameNamespace ns = NameNamespaceParser.parse(child.getValue(), defNamespace, child.getLine(), child.getValue());
		String name = ns.getName();
		String namespace = ns.getNamespace();
		
		Integer min = getInteger(child, "min");
		Integer max = getInteger(child, "max");
		// STXT-SCHEMA-SPEC 10/13.7: la cardinalidad es inválida si Min > Max
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
			return Integer.parseInt(n.getValue());
		} catch (Exception e) {
			throw new ParseException(node.getLine(), "INVALID_INTEGER", "Integer not valid: " + n.getValue());
		}
	}
}