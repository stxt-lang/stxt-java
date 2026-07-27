package dev.stxt.template;

import java.util.List;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.exceptions.ParseException;
import dev.stxt.schema.ChildDefinition;
import dev.stxt.schema.NodeDefinition;
import dev.stxt.schema.Schema;
import dev.stxt.schema.TypeRegistry;
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
		
		// STXT-TEMPLATE-SPEC 12: descripciones por nodo, en bloque aparte
		Node description = node.getChild("description");
		if (description != null) {
			String descriptionText = description.getText();
			int descriptionOffset = description.getLine();
			List<Node> descriptionNodes = new Parser().parse(descriptionText);
			addDescriptions(result, descriptionNodes, descriptionOffset);
		}
		
		// Retornamos resultado
		return result;
	}

	private static void addToSchema(Schema schema, Node node, int offset) {
		// Obtenemos nombre qualificado
		String namespace = node.getNamespace();
		String name = node.getName();

		// Si está vacío será el del schema
        if (namespace.isEmpty()) {
            namespace = schema.getNamespace();
        }           
        
		// Miramos datos
		ChildLine cl = ChildLineParser.parse(node.getValue(), node.getLine() + offset);
		
		if (!namespace.equals(schema.getNamespace())) { 
			// STXT-TEMPLATE-SPEC 14.15: un nodo cross-namespace sólo puede declarar cardinalidad;
			// tipo, valores ENUM e hijos no están permitidos y deben rechazarse, no ignorarse
			String type = cl.getType();
			if (type != null && !type.trim().isEmpty()) 
				throw new ParseException(node.getLine() + offset, "TYPE_DEFINITION_NOT_ALLOWED", "Not allowed type definition in external namespaces");
			
			if (cl.getValues() != null)
				throw new ParseException(node.getLine() + offset, "VALUES_DEFINITION_NOT_ALLOWED", "Not allowed ENUM values in external namespaces");
			
			if (!node.getChildren().isEmpty())
				throw new ParseException(node.getLine() + offset, "CHILDREN_NOT_ALLOWED_IN_EXTERNAL_NAMESPACE", "Not allowed children in external namespaces");
			
			return; // No hacemos nada con creación de nodos que no son de @stxt.template!!
		}
		
		// Miramos si es nuevo y añadimos en listado
		NodeDefinition schemaNode = schema.getNodeDefinition(name);
		if (schemaNode == null) {	// Nuevo
			String type = cl.getType() == null? "INLINE": cl.getType();
			schemaNode = new NodeDefinition(node.getName(), type, node.getLine() + offset);
			schema.addNodeDefinition(schemaNode);
            String[] values = cl.getValues();
            
            // STXT-TEMPLATE-SPEC 9/14.7/14.8: [values] sólo para ENUM, y ENUM exige valores no vacíos
            if (values != null && !type.equals("ENUM"))
                throw new ParseException(node.getLine() + offset, "VALUES_ONLY_SUPPORTED_BY_ENUM", "Values only supported for type ENUM, not for type " + type);
            
            if (values != null)
                for (String value: values)
                    schemaNode.addValue(value);
            
            if (type.equals("ENUM") && (values == null || values.length == 0))
                throw new ParseException(node.getLine() + offset, "VALUES_EMPTY_FOR_ENUM", "ENUM Type must include values");
		} else {
			String type = cl.getType();
			// STXT-TEMPLATE-SPEC 6.4: una reaparición local DEBE ser una referencia '@Nombre';
			// si no lleva tipo (type == null), no es una referencia válida (evita NPE)
			if (type == null || !type.startsWith("@"))
				throw new ParseException(node.getLine() + offset, "NODE_DEFINED_MULTIPLE_TIMES", "Multiple node reference must start with @: " + node.getName());				
				
			String refName = StringUtils.normalize(type.substring(1));
			
			if (!refName.equals(node.getNormalizedName()))
				throw new ParseException(node.getLine() + offset, "NODE_REFERENCE_NOT_VALID", "Reference must be '" + "@" + node.getName() + "', not '" + type.substring(1) + "'");
			
			// STXT-TEMPLATE-SPEC 6.4: una referencia @Nombre Nodo NO DEBE redefinir valores ENUM ni hijos
			if (cl.getValues() != null)
				throw new ParseException(node.getLine() + offset, "VALUES_DEFINITION_NOT_ALLOWED", "A node reference must not redefine ENUM values: " + node.getName());
			
			if (!node.getChildren().isEmpty())
				throw new ParseException(node.getLine() + offset, "CHILDREN_NOT_ALLOWED_IN_REFERENCE", "A node reference must not redefine children: " + node.getName());
			
			return; // OK Definition (referencia): sólo sobrescribe la cardinalidad del Child del padre
		}
		
		// Una vez ya existe, si tiene hijos los intentamos crear.
		List<Node> childrenNode = node.getChildren();
		
		// STXT-TEMPLATE-SPEC 8.2/14.9: sólo INLINE y GROUP admiten hijos
		if (!childrenNode.isEmpty() && !TypeRegistry.admitsChildren(schemaNode.getType()))
			throw new ParseException(node.getLine() + offset, "CHILDREN_NOT_ALLOWED_FOR_TYPE", "Type " + schemaNode.getType() + " does not allow children (node " + node.getName() + ")");
		
		// Insertamos childs
		for (Node child: childrenNode) {
			cl = ChildLineParser.parse(child.getValue(), child.getLine() + offset);
			
			String childName = child.getName();
			String childNamespace = child.getNamespace();
	        if (childNamespace.isEmpty()) {
	            childNamespace = schema.getNamespace();
	        }           
			
			ChildDefinition schChild = new ChildDefinition(childName, childNamespace, cl.getMin(), cl.getMax(), child.getLine() + offset);
			schemaNode.addChildDefinition(schChild);
			
			addToSchema(schema, child, offset);
		}
	}

	private static void addDescriptions(Schema schema, List<Node> nodes, int offset) {
		for (Node node: nodes) {
			// Obtenemos namespace efectivo
			String namespace = node.getNamespace();
			if (namespace.isEmpty())
				namespace = schema.getNamespace();
			
			// STXT-TEMPLATE-SPEC 14.19: una entrada de Description no puede declarar otro namespace
			if (!namespace.equals(schema.getNamespace()))
				throw new ParseException(node.getLine() + offset, "EXTERNAL_DESCRIPTION_NOT_ALLOWED", "Not allowed description in external namespaces");
			
			// STXT-TEMPLATE-SPEC 14.18: una entrada de Description no admite hijos estructurados
			if (!node.getChildren().isEmpty())
				throw new ParseException(node.getLine() + offset, "CHILDREN_DESCRIPTION_NOT_ALLOWED", "Not allowed children in description");
			
			// STXT-TEMPLATE-SPEC 14.17: la entrada debe corresponder a un nodo definido en Structure
			NodeDefinition nodeDef = schema.getNodeDefinition(node.getName());
			if (nodeDef == null)
				throw new ParseException(node.getLine() + offset, "NODE_NOT_FOUND", "Not found node with name: " + node.getName());
			
			// STXT-TEMPLATE-SPEC 14.20: no puede haber más de una entrada por nodo
			if (nodeDef.getDescription() != null)
				throw new ParseException(node.getLine() + offset, "DESCRIPTION_ALREADY_DEFINED", "Exists a previous description for node: " + node.getName());
			
			nodeDef.setDescription(node.getText());
		}
	}
}
