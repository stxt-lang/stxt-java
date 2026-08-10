package dev.stxt.template;

import java.util.List;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.schema.ChildDefinition;
import dev.stxt.schema.NodeDefinition;
import dev.stxt.schema.Schema;
import dev.stxt.schema.TypeRegistry;
import dev.stxt.utils.StringUtils;

/** Turns the tree of an already parsed {@code @stxt.template} document into an equivalent {@link Schema}. */
public class TemplateParser {

	private TemplateParser() {
	}

	/**
	 * Transforms the document tree into the equivalent {@link Schema}.
	 *
	 * @param node root of the already parsed {@code @stxt.template} document.
	 * @return the resulting {@link Schema}.
	 */
	public static Schema transformNodeToSchema(Node node) {
		
		// Set the namespace
		Schema result = new Schema(node.getValue(), node.getLine());
		
		// Look for the structure node
		Node structure = node.getChild("structure");
		if (structure == null) {
		    throw new ValidationException(node.getLine(), "TEMPLATE_STRUCTURE_REQUIRED",
		        "Template must define 'Structure >>'");
		}
		
		String text = structure.getText();
		int offset = structure.getLine();
		
		// Parse it to get the nodes
		List<Node> nodes = new Parser().parse(text);
		
		// Walk every node adding it in
		for (Node n: nodes)
			addToSchema(result, n, offset);
		
		// STXT-TEMPLATE-SPEC 12: per-node descriptions, in a separate block
		Node description = node.getChild("description");
		if (description != null) {
			String descriptionText = description.getText();
			int descriptionOffset = description.getLine();
			List<Node> descriptionNodes = new Parser().parse(descriptionText);
			addDescriptions(result, descriptionNodes, descriptionOffset);
		}
		
		// Return the result
		return result;
	}

	private static void addToSchema(Schema schema, Node node, int offset) {
		// Structure has its own grammar: every non-empty line must use ':'. The core
		// parser also accepts BLOCK nodes here, so reject that form explicitly.
		if (node.isTextNode())
			throw new ValidationException(node.getLine() + offset, "INVALID_CHILD_LINE", "Template Structure lines must use ':'");

		// Get the qualified name
		String namespace = node.getNamespace();
		String name = node.getName();

		// When empty it will be the schema's one
        if (namespace.isEmpty()) {
            namespace = schema.getNamespace();
        }           
        
		// Look at the data
		ChildLine cl = ChildLineParser.parse(node.getValue(), node.getLine() + offset);
		
		if (!namespace.equals(schema.getNamespace())) { 
			// STXT-TEMPLATE-SPEC 14.15: a cross-namespace node may only declare cardinality;
			// type, ENUM values and children are not allowed and must be rejected, not ignored
			String type = cl.getType();
			if (type != null && !type.trim().isEmpty()) 
				throw new ValidationException(node.getLine() + offset, "TYPE_DEFINITION_NOT_ALLOWED", "Not allowed type definition in external namespaces");
			
			if (cl.getValues() != null)
				throw new ValidationException(node.getLine() + offset, "VALUES_NOT_ALLOWED_IN_EXTERNAL_NAMESPACE", "Not allowed values in external namespaces (node " + node.getName() + ")");
			
			if (!node.getChildren().isEmpty())
				throw new ValidationException(node.getLine() + offset, "CHILDREN_NOT_ALLOWED_IN_EXTERNAL_NAMESPACE", "Not allowed children in external namespaces");
			
			return; // We do not create nodes that do not belong to @stxt.template!!
		}
		
		// Check whether it is new and add it to the list
		NodeDefinition schemaNode = schema.getNodeDefinition(name);
		if (schemaNode == null) {	// New
			String type = cl.getType() == null? "INLINE": cl.getType();

			// At this point the schema already holds both the previous definitions, already
			// closed, and the open ancestors, so a reference that does not resolve here does
			// not resolve at all (STXT-TEMPLATE-SPEC 6.4 and 14.11)
			if (type.startsWith("@"))
				throw new ValidationException(node.getLine() + offset, "REFERENCE_NOT_FOUND", "Reference '" + type + "' does not point to a previous definition or an open ancestor");

			// STXT-TEMPLATE-SPEC 14.6: the type must be one of the supported ones
			if (TypeRegistry.get(type) == null)
				throw new ValidationException(node.getLine() + offset, "TYPE_NOT_VALID", "Type not valid: " + type);

			schemaNode = new NodeDefinition(node.getName(), type, node.getLine() + offset);
			schema.addNodeDefinition(schemaNode);
            String[] values = cl.getValues();
            
            // STXT-TEMPLATE-SPEC 9/14.7/14.8: [values] only for ENUM, and ENUM requires non-empty values
            if (values != null && !type.equals("ENUM"))
                throw new ValidationException(node.getLine() + offset, "VALUES_ONLY_SUPPORTED_BY_ENUM", "Values only supported for type ENUM, not for type " + type);
            
            if (values != null)
                for (String value: values)
                    schemaNode.addValue(value, node.getLine() + offset);
            
            if (type.equals("ENUM") && (values == null || values.length == 0))
                throw new ValidationException(node.getLine() + offset, "VALUES_EMPTY_FOR_ENUM", "ENUM Type must include values");
		} else {
			String type = cl.getType();
			// STXT-TEMPLATE-SPEC 6.4: a local reappearance MUST be a '@Name' reference;
			// if it carries no type (type == null), it is not a valid reference (avoids an NPE)
			if (type == null || !type.startsWith("@"))
				throw new ValidationException(node.getLine() + offset, "NODE_DEFINED_MULTIPLE_TIMES", "Multiple node reference must start with @: " + node.getName());				
				
			String reference = type.substring(1).trim();

			// STXT-TEMPLATE-SPEC 14.13: a reference and an explicit type cannot be declared at once
			String explicitType = referenceType(reference, node.getNormalizedName());
			if (explicitType != null)
				throw new ValidationException(node.getLine() + offset, "REFERENCE_WITH_TYPE_NOT_ALLOWED", "Reference '@" + node.getName() + "' can not declare a type: " + explicitType);

			if (!StringUtils.normalize(reference).equals(node.getNormalizedName()))
				throw new ValidationException(node.getLine() + offset, "NODE_REFERENCE_NOT_VALID", "Reference must be '" + "@" + node.getName() + "', not '" + reference + "'");
			
			// STXT-TEMPLATE-SPEC 6.4: a @Node Name reference MUST NOT redefine ENUM values nor children
			if (cl.getValues() != null)
				throw new ValidationException(node.getLine() + offset, "VALUES_NOT_ALLOWED_IN_REFERENCE", "Reference '@" + node.getName() + "' can not redefine ENUM values");

			if (!node.getChildren().isEmpty())
				throw new ValidationException(node.getLine() + offset, "CHILDREN_NOT_ALLOWED_IN_REFERENCE", "Reference '@" + node.getName() + "' can not redefine children");
			
			return; // OK Definition (reference): it only overrides the cardinality of the parent's Child
		}
		
		// Once it exists, if it has children we try to create them.
		List<Node> childrenNode = node.getChildren();
		
		// STXT-TEMPLATE-SPEC 8.2/14.9: only INLINE and GROUP accept children
		if (!childrenNode.isEmpty() && !TypeRegistry.admitsChildren(schemaNode.getType()))
			throw new ValidationException(node.getLine() + offset, "CHILDREN_NOT_ALLOWED_FOR_TYPE", "Type " + schemaNode.getType() + " does not allow children (node " + node.getName() + ")");
		
		// Add the children
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

	/**
	 * Tells `@Node Name TYPE` (reference + type, error 14.13) apart from `@Other Name`
	 * (reference with a different name, error 14.12). Since node names may contain spaces,
	 * the only reliable reading is: if the last token is a known type and what comes before
	 * it is the name of the node itself, then the line declares both things.
	 * Returns the declared type, or null if the reference carries no type.
	 */
	private static String referenceType(String reference, String normalizedName) {
		int cut = reference.lastIndexOf(' ');
		if (cut < 0)
			return null;

		String candidate = reference.substring(cut + 1).trim();
		String rest = reference.substring(0, cut);

		if (TypeRegistry.get(candidate) != null && StringUtils.normalize(rest).equals(normalizedName))
			return candidate;

		return null;
	}

	private static void addDescriptions(Schema schema, List<Node> nodes, int offset) {
		for (Node node: nodes) {
			// Get the effective namespace
			String namespace = node.getNamespace();
			if (namespace.isEmpty())
				namespace = schema.getNamespace();
			
			// STXT-TEMPLATE-SPEC 14.19: a Description entry cannot declare another namespace
			if (!namespace.equals(schema.getNamespace()))
				throw new ValidationException(node.getLine() + offset, "EXTERNAL_DESCRIPTION_NOT_ALLOWED", "Not allowed description in external namespaces");
			
			// STXT-TEMPLATE-SPEC 14.18: a Description entry accepts no structured children
			if (!node.getChildren().isEmpty())
				throw new ValidationException(node.getLine() + offset, "CHILDREN_DESCRIPTION_NOT_ALLOWED", "Not allowed children in description");
			
			// STXT-TEMPLATE-SPEC 14.17: the entry must match a node defined in Structure
			NodeDefinition nodeDef = schema.getNodeDefinition(node.getName());
			if (nodeDef == null)
				throw new ValidationException(node.getLine() + offset, "NODE_NOT_FOUND", "Not found node with name: " + node.getName());
			
			// STXT-TEMPLATE-SPEC 14.20: there cannot be more than one entry per node
			if (nodeDef.getDescription() != null)
				throw new ValidationException(node.getLine() + offset, "DESCRIPTION_ALREADY_DEFINED", "Exists a previous description for node: " + node.getName());
			
			nodeDef.setDescription(node.getText());
		}
	}
}
