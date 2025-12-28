package dev.stxt.schema;

import java.util.List;

import dev.stxt.Node;
import dev.stxt.ParseException;

public class TemplateSchemaParser {

	public static Schema transformNodeToSchema(Node node) {
		Schema result = new Schema();
		
		// Insertamos namespace
		String namespace = node.getInlineText();
		result.setNamespace(namespace);
		
		// Vamos iterando todos los nodos insertando
		for (Node n: node.getChildren())
			addToSchema(result, n);
		
		// Retornamos resultado
		return result;
	}

	private static void addToSchema(Schema schema, Node node) {
		// Obtenemos nombre qualificado
		String name = node.getName();
		String namespace = node.getNamespace();
		if (namespace.equals("@stxt.template")) namespace = schema.getNamespace(); // Es del template
		else return; // No hacemos nada con creación de nodos que no son de @stxt.template!!
		
		// Miramos datos
		ChildLine cl = TemplateChildLineParser.parse(node.getInlineText(), node.getLine());
		System.out.println(cl);
		
		// Miramos si es nuevo y añadimos en listado
		SchemaNode schemaNode = schema.getNodes().get(name);
		String type = cl.getType() == null? "INLINE TEXT": cl.getType();
		if (schemaNode == null) {	// Nuevo
			schemaNode = new SchemaNode();
			schemaNode.setName(name);
			schemaNode.setType(type);
			schema.getNodes().put(name, schemaNode);
		} else {
			if (cl.getType()!=null)
				throw new ParseException(node.getLine(), "TYPE_DEFINED_MULTIPLE_TIMES", "Type only can defined one time");
		}
		
		// Una vez ya existe, si tiene hijos los intentamos crear.
		List<Node> childrenNode = node.getChildren();
		if (childrenNode.size()>0) {
			// Si ya tenía error!!
			if(schemaNode.getChildren().size()>0)
				throw new ParseException(node.getLine(), "CHILDREN_DEFINED_MULTIPLE_TIMES", "children only can defined one time");
			
			// Insertamos childs
			for (Node child: childrenNode) {
				System.out.println("=>" + child.getName() + " -> " + child.getNamespace());
				cl = TemplateChildLineParser.parse(child.getInlineText(), child.getLine());
				System.out.println(" ==> " + cl);
				
				String childName = child.getName();
				String childNamespace = child.getNamespace();
				if (childNamespace.equals("@stxt.template")) childNamespace = schema.getNamespace();
				String childQualifiedName = childNamespace + ":" + childName;
				
				SchemaChild schChild = new SchemaChild();
				schChild.setName(childName);
				schChild.setNamespace(childNamespace);
				schChild.setMin(cl.getMin());
				schChild.setMax(cl.getMax());
				schemaNode.getChildren().put(childQualifiedName, schChild);
				
				addToSchema(schema, child);
			}
		}
	}

}
