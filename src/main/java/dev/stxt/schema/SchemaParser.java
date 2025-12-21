package dev.stxt.schema;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.stxt.Node;
import dev.stxt.processors.ValidationException;
import dev.stxt.utils.StringUtils;

class SchemaParser {
	private static final Pattern CHILD_LINE_PATTERN = Pattern
			.compile("^\\s*(?:(?<ns>[^:()]+):)?(?<name>[^()]+?)\\s*(?:\\(\\s*(?<count>[^()\\s][^)]*?)\\s*\\))?\\s*$");
	private static final Pattern P = Pattern
			.compile("^\\s*(?<name>[^()]+?)\\s*(?:\\(\\s*(?<type>[^()]+?)\\s*\\))?\\s*$");

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
		String namespace = node.getInlineText();
		schema.namespace = namespace;

		// Obtenemos los nodos
		List<Node> nodes = node.getChilds("node");

		Set<String> allNames = new HashSet<String>(); // Para validar que
														// existan los childs
		for (Node n : nodes) {
			SchemaNode schNode = createFrom(n, schema.namespace);
			schema.nodes.put(schNode.name, schNode);

			allNames.add(schNode.name);
		}

		// Validamos que todos los nombres estén definidos
		for (SchemaNode schNode : schema.nodes.values()) {
			for (SchemaChild schChild : schNode.children.values()) {
				if (schChild.namespace.equals(namespace)) // Sólo validamos del mismo namespace
				{
					if (!allNames.contains(schChild.name))
						throw new ValidationException(0, "CHILD_NOT_DEFINED",
								"Child " + schChild.name + " not defined in " + namespace);
				}
			}
		}

		return schema;
	}

	private static SchemaNode createFrom(Node n, String namespace) {
		SchemaNode result = new SchemaNode();

		String name = n.getInlineText();

		Matcher m = P.matcher(name);
		String type = "INLINE TEXT";

		if (m.matches()) {
			name = m.group("name").trim();
			type = m.group("type") != null ? m.group("type").trim() : type;
		} else {
			throw new ValidationException(n.getLine(), "NODE_NAME_INVALID", "Line not valid: " + n.getInlineText());
		}

		result.name = StringUtils.compactString(name.toLowerCase());
		result.type = type;

		Node children = n.getChild("children");
		if (children != null) {
			for (String line : children.getMultilineText()) {
				if (line.isEmpty())
					continue;

				SchemaChild schemaChild = parseFromLine(line, namespace);
				updateCount(schemaChild);
				String qname = schemaChild.getQualifiedName();
				if (result.children.containsKey(qname))
					throw new ValidationException(children.getLine(), "DUPLICATED_CHILD", qname);

				result.children.put(qname, schemaChild);
			}
		}

		return result;
	}

	private static SchemaChild parseFromLine(String line, String namespace) {
		if (line == null) {
			throw new IllegalArgumentException("line cannot be null");
		}

		String trimmed = line.trim();
		if (trimmed.isEmpty()) {
			throw new IllegalArgumentException("line cannot be empty");
		}

		Matcher m = CHILD_LINE_PATTERN.matcher(trimmed);
		if (!m.matches()) {
			throw new IllegalArgumentException("Line format not valid: " + line);
		}

		SchemaChild child = new SchemaChild();

		String name = m.group("name");
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("Name cannot be empty: " + line);
		}
		child.name = StringUtils.compactString(name.trim().toLowerCase());

		String ns = m.group("ns");
		if (ns != null && !ns.trim().isEmpty())
			child.namespace = ns.trim();
		else
			child.namespace = namespace;

		String count = m.group("count");
		child.count = count;
		return child;
	}

	private static void updateCount(SchemaChild child) {
		String count = child.count;

		if (count == null || count.isEmpty() || count.equals("*")) {
			// No min, no max para empty y "*"
		} else if (count.equals("?")) {
			child.max = 1;
		} else if (count.equals("+")) {
			child.min = 1;
		} else if (count.endsWith("+")) {
			int expectedNum = Integer.parseInt(count.substring(0, count.length() - 1));
			child.min = expectedNum;
		} else if (count.endsWith("-")) {
			int expectedNum = Integer.parseInt(count.substring(0, count.length() - 1));
			child.max = expectedNum;
		} else {
			int expectedNum = Integer.parseInt(count);
			child.min = expectedNum;
			child.max = expectedNum;
		}
	}
}