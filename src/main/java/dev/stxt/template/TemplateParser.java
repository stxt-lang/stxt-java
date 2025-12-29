package dev.stxt.template;

import java.util.List;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.exceptions.ParseException;
import dev.stxt.schema.Schema;
import dev.stxt.schema.ChildDefinition;
import dev.stxt.schema.NodeDefinition;
import dev.stxt.utils.StringUtils;

public class TemplateParser {
	
	public static Schema transformNodeToSchema(Node node) {
		Schema result = new Schema();
		
		// Insertamos namespace
		String namespace = node.getValue();
		result.setNamespace(namespace);
		
		// Buscamos nodo structure
		String text = node.getChild("structure").getText();
		int offset = node.getLine();
		
		// Parseamos para los nodos
		Parser parser = new Parser();
		List<Node> nodes = parser.parse(text);
		
		// Vamos iterando todos los nodos insertando
		for (Node n: nodes)
			addToSchema(result, n, offset);
		
		// Retornamos resultado
		return result;
	}

	private static void addToSchema(Schema schema, Node node, int offset) {
		// Obtenemos nombre qualificado
		String namespace = node.getNamespace();
		String name = node.getName();
		
		// Miramos datos
		ChildLine cl = ChildLineParser.parse(node.getValue(), node.getLine());
		
		if (namespace.isEmpty()) namespace = schema.getNamespace(); // Es del template
		else {
			// Validamos type vacío
			String type = cl.getType();
			if (type != null && !type.trim().isEmpty()) 
				throw new ParseException(node.getLine() + offset, "TYPE_DEFINITION_NOT_ALLOWED", "Not allowed type definition in external namespaces");
			
			return; // No hacemos nada con creación de nodos que no son de @stxt.template!!
		}
		
		// Miramos si es nuevo y añadimos en listado
		NodeDefinition schemaNode = schema.getNodeDefinition(name);
		if (schemaNode == null) {	// Nuevo
			String type = cl.getType() == null? "VALUE_NODE": cl.getType();
			schemaNode = new NodeDefinition(node.getName(), type);
			schema.addNodeDefinition(schemaNode);
		} else {
			String type = cl.getType();
			if (!type.startsWith("@"))
				throw new ParseException(node.getLine() + offset, "NODE_DEFINED_MULTIPLE_TIMES", "Multiple node reference must start with @: " + node.getName());				
				
			type = type.substring(1);
			type = StringUtils.normalizeFull(type);
			
			if (type.equals(node.getNormalizedName())) return; // OK Definition
			throw new ParseException(node.getLine() + offset, "NODE_REFERENCE_NOT_VALID", "Reference must be '" + "@" + node.getName() + "', not '" + type + "'");
		}
		
		// Una vez ya existe, si tiene hijos los intentamos crear.
		List<Node> childrenNode = node.getChildren();
		
		// Insertamos childs
		for (Node child: childrenNode) {
			cl = ChildLineParser.parse(child.getValue(), child.getLine());
			
			String childName = child.getName();
			String childNamespace = child.getNamespace();
			if (childNamespace.isEmpty()) childNamespace = schema.getNamespace();
			
			ChildDefinition schChild = new ChildDefinition(childName, childNamespace, cl.getMin(), cl.getMax());
			schemaNode.addChildDefinition(schChild);
			
			addToSchema(schema, child, offset);
		}
	}
}
