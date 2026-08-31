package dev.stxt.template;

import java.util.List;

import dev.stxt.InlineNode;
import dev.stxt.Node;
import dev.stxt.NamespaceValidator;
import dev.stxt.Parser;
import dev.stxt.exceptions.ParseException;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.schema.ChildDefinition;
import dev.stxt.schema.NodeDefinition;
import dev.stxt.schema.Schema;
import dev.stxt.schema.TypeRegistry;
import dev.stxt.utils.StringUtils;

/** Turns the tree of an already parsed {@code @stxt.template} document into an equivalent {@link Schema}. */
public class TemplateParser {

	/** Namespace of the template language; the canonical constant is {@link Schema#TEMPLATE_NAMESPACE}. */
	public static final String TEMPLATE_NAMESPACE = Schema.TEMPLATE_NAMESPACE;

	private TemplateParser() {
	}

	/**
	 * Transforms the document tree into the equivalent {@link Schema}.
	 *
	 * @param node root of the already parsed {@code @stxt.template} document.
	 * @return the resulting {@link Schema}.
	 */
	public static Schema transformNodeToSchema(Node node) {
		// STXT-TEMPLATE-SPEC 14.1: the root must be 'Template (@stxt.template): ns'
		if (!node.getCanonicalName().equals("template") || !TEMPLATE_NAMESPACE.equals(node.getNamespace())
				|| !(node instanceof InlineNode root))
			throw new ValidationException(node.getLine(), "TEMPLATE_ROOT_NOT_VALID",
					"Expected template(" + TEMPLATE_NAMESPACE + ") but got " + node.getCanonicalName() + "(" + node.getNamespace() + ")");
		String targetNamespace = root.getValue();
		if (targetNamespace.isEmpty())
			throw new ValidationException(root.getLine(), "TEMPLATE_NAMESPACE_EMPTY", "Template root must declare the target namespace");
		try {
			NamespaceValidator.validateNamespaceFormat(StringUtils.lowerCase(targetNamespace), root.getLine());
		} catch (ParseException e) {
			throw new ValidationException(root.getLine(), "TEMPLATE_ROOT_NOT_VALID", "Template namespace not valid: " + targetNamespace);
		}

		// Set the namespace
		Schema result = new Schema(targetNamespace, root.getLine());
		
		// Look for the structure node
		Node structure = root.getChild("structure");
		if (structure == null) {
		    throw new ValidationException(node.getLine(), "TEMPLATE_STRUCTURE_REQUIRED",
		        "Template must define 'Structure >>'");
		}
		
		String text = structure.getText();
		int offset = structure.getLine();
		
		// Parse it to get the nodes; a parse error of the block is reported at the line of the
		// original document (stxt-impl template_parser: the line is shifted by the block offset)
		List<Node> nodes = parseBlock(text, offset);
		
		// Walk every node adding it in
		for (Node n: nodes)
			addToSchema(result, n, offset);
		
		// STXT-TEMPLATE-SPEC 12: per-node descriptions, in a separate block
		Node description = root.getChild("description");
		if (description != null) {
			String descriptionText = description.getText();
			int descriptionOffset = description.getLine();
			List<Node> descriptionNodes = parseBlock(descriptionText, descriptionOffset);
			addDescriptions(result, descriptionNodes, descriptionOffset);
		}
		
		// Return the result
		return result;
	}

	/**
	 * Parses the text of a {@code >>} block of the template as an STXT document. Its parse
	 * errors are re-thrown with the line shifted by the block offset, so they point at the
	 * real line of the template; a {@link ValidationException} keeps its subtype.
	 */
	private static List<Node> parseBlock(String text, int offset) {
		try {
			return new Parser().parse(text);
		}
		catch (ValidationException e) {
			throw new ValidationException(e.getLine() + offset, e.getCode(), e.getMessage());
		}
		catch (ParseException e) {
			throw new ParseException(e.getLine() + offset, e.getCode(), e.getMessage());
		}
	}

	/**
	 * Adds to the schema the definition a Structure node declares, along with its children.
	 *
	 * Only the orchestration lives here; each of the three shapes a Structure line can take has
	 * its own helper below: a node of an external namespace ({@link #validateExternalNode}, nothing
	 * is created), a name seen for the first time ({@link #createDefinition}) or a reappearance
	 * ({@link #validateReference}, nothing is created). The children of a definition are declared
	 * and recursed by {@link #addChildren}.
	 */
	private static void addToSchema(Schema schema, Node node, int offset) {
		// A Structure line belongs to the template grammar, not merely to the core one: every
		// non-empty line must use ':'. The core parser also accepts BLOCK nodes here, so reject
		// that form explicitly (STXT-TEMPLATE-SPEC 6.3).
		if (!(node instanceof InlineNode inline))
			throw new ValidationException(node.getLine() + offset, "STRUCTURE_LINE_NOT_VALID", "Template Structure lines must use ':'");

		// Parse the RuleSpec (cardinality / type / values) from the inline value
		ChildLine cl = ChildLineParser.parse(node.getText(), node.getLine() + offset);

		// No explicit namespace => the target namespace of the template
		String namespace = node.getNamespace();
        if (namespace.isEmpty()) {
            namespace = schema.getNamespace();
        }

		if (!namespace.equals(schema.getNamespace())) {
			validateExternalNode(inline, cl, offset);
			return; // no definitions are created for nodes of other namespaces
		}

		// New definition or a reappearance (reference)?
		NodeDefinition schemaNode = schema.getNodeDefinition(node.getName());

		if (schemaNode != null) {
			validateReference(inline, cl, offset);
			return; // valid reference: nothing is redefined, no children are processed
		}

		schemaNode = createDefinition(schema, inline, cl, offset);
		addChildren(schema, schemaNode, inline, offset);
	}

	/**
	 * Cross-namespace node (STXT-TEMPLATE-SPEC 6.4, 10 and 14.15): not defined locally; it may
	 * only declare cardinality — no type, no ENUM values and no children.
	 */
	private static void validateExternalNode(InlineNode node, ChildLine cl, int offset) {
		String type = cl.getType();
		if (type != null && !StringUtils.trim(type).isEmpty())
			throw new ValidationException(node.getLine() + offset, "TYPE_NOT_ALLOWED_IN_EXTERNAL_NAMESPACE", "Not allowed type definition in external namespaces");

		if (cl.getValues() != null)
			throw new ValidationException(node.getLine() + offset, "VALUES_NOT_ALLOWED_IN_EXTERNAL_NAMESPACE", "Not allowed values in external namespaces (node " + node.getName() + ")");

		if (!node.getChildren().isEmpty())
			throw new ValidationException(node.getLine() + offset, "CHILDREN_NOT_ALLOWED_IN_EXTERNAL_NAMESPACE", "Not allowed children in external namespaces");
	}

	/**
	 * First appearance of a name: creates its {@link NodeDefinition} (with its type and its ENUM
	 * values, when it has them) and registers it in the schema.
	 */
	private static NodeDefinition createDefinition(Schema schema, InlineNode node, ChildLine cl, int offset) {
		String type = cl.getType() == null? "INLINE": cl.getType();

		// At this point the schema already holds both the previous definitions, already
		// closed, and the open ancestors, so a reference that does not resolve here does
		// not resolve at all (STXT-TEMPLATE-SPEC 6.4 and 14.11)
		if (type.startsWith("@"))
			throw new ValidationException(node.getLine() + offset, "REFERENCE_NOT_FOUND", "Reference '" + type + "' does not point to a previous definition or an open ancestor");

		NodeDefinition schemaNode = new NodeDefinition(node.getName(), type, node.getLine() + offset);
		schema.addNodeDefinition(schemaNode);

		// STXT-TEMPLATE-SPEC 14.6: the type must be one of the supported ones
		if (TypeRegistry.get(type) == null)
			throw new ValidationException(node.getLine() + offset, "TYPE_NOT_VALID", "Type not valid: " + type);

		String[] values = cl.getValues();

		// STXT-TEMPLATE-SPEC 9/14.7/14.8: [values] only for ENUM, and ENUM requires non-empty values
		if (values != null && !type.equals("ENUM"))
			// Same code as SchemaParser: a template is sugar equivalent to a schema
			// (STXT-TEMPLATE-SPEC 13), so the same condition must not change its code
			// depending on the entry point
			throw new ValidationException(node.getLine() + offset, "VALUES_NOT_ALLOWED_FOR_TYPE", "Values only supported for type ENUM, not for type " + type);

		if (values != null)
			for (String value: values)
				schemaNode.addValue(value, node.getLine() + offset);

		// An ENUM with no list of values is an invalid template (STXT-TEMPLATE-SPEC 9 and 13.7)
		if (type.equals("ENUM") && (values == null || values.length == 0))
			throw new ValidationException(node.getLine() + offset, "VALUES_REQUIRED", "ENUM Type must include values");

		return schemaNode;
	}

	/**
	 * Reappearance of an already defined name: it must be a {@code @Node Name} reference, and a
	 * reference may override the cardinality but may redefine neither the ENUM values nor the
	 * children (STXT-TEMPLATE-SPEC 6.4, 14.12 and 14.13).
	 */
	private static void validateReference(InlineNode node, ChildLine cl, int offset) {
		String type = cl.getType();

		// A reappearance without "@" would redefine an existing node: error
		// (if it carries no type at all, it is not a valid reference either — avoids an NPE)
		if (type == null || !type.startsWith("@"))
			throw new ValidationException(node.getLine() + offset, "REFERENCE_REQUIRED", "Multiple node reference must start with @: " + node.getName());

		String reference = StringUtils.trim(type.substring(1));

		// STXT-TEMPLATE-SPEC 14.13: a reference and an explicit type cannot be declared at once
		String explicitType = referenceType(reference, node.getCanonicalName());
		if (explicitType != null)
			throw new ValidationException(node.getLine() + offset, "REFERENCE_WITH_TYPE_NOT_ALLOWED", "Reference '@" + node.getName() + "' can not declare a type: " + explicitType);

		// The name of the reference must match (canonically) the one of the line (14.12)
		if (!StringUtils.normalize(reference).equals(node.getCanonicalName()))
			throw new ValidationException(node.getLine() + offset, "REFERENCE_NAME_NOT_VALID", "Reference must be '" + "@" + node.getName() + "', not '" + reference + "'");

		// STXT-TEMPLATE-SPEC 6.4: a @Node Name reference MUST NOT redefine ENUM values nor children
		if (cl.getValues() != null)
			throw new ValidationException(node.getLine() + offset, "VALUES_NOT_ALLOWED_IN_REFERENCE", "Reference '@" + node.getName() + "' can not redefine ENUM values");

		if (!node.getChildren().isEmpty())
			throw new ValidationException(node.getLine() + offset, "CHILDREN_NOT_ALLOWED_IN_REFERENCE", "Reference '@" + node.getName() + "' can not redefine children");
	}

	/**
	 * Declares every direct child of a definition as a {@link ChildDefinition} (with its
	 * cardinality) and recurses into each one as a definition/reference of its own.
	 */
	private static void addChildren(Schema schema, NodeDefinition schemaNode, InlineNode node, int offset) {
		List<Node> children = node.getChildren();

		// STXT-TEMPLATE-SPEC 8.2/14.9: only INLINE and GROUP accept children
		if (!children.isEmpty() && !TypeRegistry.admitsChildren(schemaNode.getType()))
			throw new ValidationException(node.getLine() + offset, "CHILDREN_NOT_ALLOWED_FOR_TYPE", "Type " + schemaNode.getType() + " does not allow children (node " + node.getName() + ")");

		for (Node child: children) {
			ChildLine childCl = ChildLineParser.parse(child.getText(), child.getLine() + offset);

			String childNamespace = child.getNamespace();
	        if (childNamespace.isEmpty()) {
	            childNamespace = schema.getNamespace();
	        }

			// The child is declared as a Child (with its cardinality) in the current definition
			ChildDefinition schChild = new ChildDefinition(child.getName(), childNamespace, childCl.getMin(), childCl.getMax(), child.getLine() + offset);
			schemaNode.addChildDefinition(schChild);

			// And processed recursively as a definition/reference
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

		String candidate = StringUtils.trim(reference.substring(cut + 1));
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
				throw new ValidationException(node.getLine() + offset, "DESCRIPTION_NOT_ALLOWED_IN_EXTERNAL_NAMESPACE", "Not allowed description in external namespaces");
			
			// STXT-TEMPLATE-SPEC 14.18: a Description entry accepts no structured children
			if (node instanceof InlineNode inline && !inline.getChildren().isEmpty())
				throw new ValidationException(node.getLine() + offset, "DESCRIPTION_CHILDREN_NOT_ALLOWED", "Not allowed children in description");
			
			// STXT-TEMPLATE-SPEC 14.17: the entry must match a node defined in Structure
			NodeDefinition nodeDef = schema.getNodeDefinition(node.getName());
			if (nodeDef == null)
				throw new ValidationException(node.getLine() + offset, "DESCRIPTION_NODE_NOT_FOUND", "Not found node with name: " + node.getName());
			
			// STXT-TEMPLATE-SPEC 14.20: there cannot be more than one entry per node
			if (nodeDef.getDescription() != null)
				throw new ValidationException(node.getLine() + offset, "DESCRIPTION_DUPLICATED", "Exists a previous description for node: " + node.getName());
			
			nodeDef.setDescription(node.getText());
		}
	}
}
