package dev.stxt.template;

import java.util.List;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.exceptions.ParseException;
import dev.stxt.schema.ChildDefinition;
import dev.stxt.schema.NodeDefinition;
import dev.stxt.schema.Schema;
import dev.stxt.utils.StringUtils;

public class TemplateParser {
	
	public static Schema transformNodeToSchema(Node node) {
		
		// Insertamos namespace
		Schema result = new Schema(node.getValue(), node.getLine());
		
		// Buscamos nodo structure
		Node structure = node.getChild("structure");
		if (structure == null) {
		    throw new ParseException(node.getLine(), "TEMPLATE_STRUCTURE_REQUIRED",
		        "Template must define 'Structure >>'");
		}
		
		String text = structure.getText();
		int offset = structure.getLine();
		
		// Parseamos para los nodos
		List<Node> nodes = new Parser().parse(text);
		
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
		ChildLine cl = ChildLineParser.parse(node.getValue(), node.getLine() + offset);
		
		if (namespace.isEmpty())
			throw new ParseException(node.getLine() + offset, "EMPTY_NAMESPACE", "Not allowed empty namespaces");
		
		if (!namespace.equals(schema.getNamespace())) { 
			// Validamos type vacío
			String type = cl.getType();
			if (type != null && !type.trim().isEmpty()) 
				throw new ParseException(node.getLine() + offset, "TYPE_DEFINITION_NOT_ALLOWED", "Not allowed type definition in external namespaces");
			
			return; // No hacemos nada con creación de nodos que no son de @stxt.template!!
		}
		
		// Miramos si es nuevo y añadimos en listado
		NodeDefinition schemaNode = schema.getNodeDefinition(name);
		if (schemaNode == null) {	// Nuevo
			String type = cl.getType() == null? "INLINE": cl.getType();
			schemaNode = new NodeDefinition(node.getName(), type, node.getLine() + offset);
			schema.addNodeDefinition(schemaNode);
            String[] values = cl.getValues();
            if (values != null)
                for (String value: values)
                    schemaNode.addValue(value);
		} else {
			String type = cl.getType();
			if (!type.startsWith("@"))
				throw new ParseException(node.getLine() + offset, "NODE_DEFINED_MULTIPLE_TIMES", "Multiple node reference must start with @: " + node.getName());				
				
			type = type.substring(1);
			type = StringUtils.normalize(type);
			
			if (type.equals(node.getNormalizedName())) return; // OK Definition
			throw new ParseException(node.getLine() + offset, "NODE_REFERENCE_NOT_VALID", "Reference must be '" + "@" + node.getName() + "', not '" + type + "'");
		}
		
		// Una vez ya existe, si tiene hijos los intentamos crear.
		List<Node> childrenNode = node.getChildren();
		
		// Insertamos childs
		for (Node child: childrenNode) {
			cl = ChildLineParser.parse(child.getValue(), child.getLine() + offset);
			
			String childName = child.getName();
			String childNamespace = child.getNamespace();
			
			ChildDefinition schChild = new ChildDefinition(childName, childNamespace, cl.getMin(), cl.getMax(), child.getLine() + offset);
			schemaNode.addChildDefinition(schChild);
			
			addToSchema(schema, child, offset);
		}
	}
}
