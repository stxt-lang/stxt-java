package dev.stxt.schema;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.stxt.Node;
import dev.stxt.processors.ValidationException;
import dev.stxt.utils.StringUtils;

class SchemaParser {
	private static final Pattern CHILD_LINE_PATTERN = Pattern.compile(
		    "^\\s*" +
		    "(?:\\(\\s*(?<count>[^()\\s][^)]*?)\\s*\\)\\s*)?" + // (count)
		    "(?<name>[^()]+?)" +                               // name
		    "\\s*(?:\\(\\s*(?<ns>[^()]+?)\\s*\\))?" +          // (namespace)
		    "\\s*$"
		);
	
	private static final Pattern NAME_TYPE_PATTERN = Pattern
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
		String namespace = node.getInlineText().trim().toLowerCase(Locale.ROOT);
		schema.setNamespace(namespace);

		// Obtenemos los nodos
		List<Node> nodes = node.getChildren("node");

		Set<String> allNames = new HashSet<String>(); // Para validar que
														// existan los childs
		for (Node n : nodes) {
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

		String name = n.getInlineText();

		Matcher m = NAME_TYPE_PATTERN.matcher(name);
		String type = "INLINE TEXT";

		if (m.matches()) {
			name = m.group("name").trim();
			type = m.group("type") != null ? m.group("type").trim() : type;
		} else {
			throw new ValidationException(n.getLine(), "NODE_NAME_INVALID", "Line not valid: " + n.getInlineText());
		}

		result.setName(StringUtils.normalizeName(name));
		result.setType(type);

		Node children = n.getChild("children");
		if (children != null) {
			int realLine = children.getLine();
			for (String line : children.getMultilineText()) {
				realLine++;
				line = line.trim();
				if (line.isEmpty())
					continue;

				SchemaChild schemaChild = parseFromLine(line, namespace, realLine);
				updateCount(schemaChild);
				String qname = schemaChild.getQualifiedName();
				if (result.getChildren().containsKey(qname))
					throw new ValidationException(realLine, "DUPLICATED_CHILD", qname);

				result.getChildren().put(qname, schemaChild);
			}
		}

		return result;
	}

	private static SchemaChild parseFromLine(String line, String namespace, int lineNum) {
		Matcher m = CHILD_LINE_PATTERN.matcher(line);
		if (!m.matches()) {
			throw new ValidationException(lineNum, "SCHEMA_CHILD_NOT_VALID", "line format not valid: " + line);
		}

		SchemaChild child = new SchemaChild();

		String name = m.group("name");
		if (name == null || name.trim().isEmpty()) {
			throw new ValidationException(lineNum, "SCHEMA_CHILD_NAME_EMPTY", "Name cannot be empty: " + line);
		}
		child.setName(StringUtils.normalizeName(name));

		String ns = m.group("ns");
		if (ns != null && !ns.trim().isEmpty())
			child.setNamespace(ns.trim().toLowerCase(Locale.ROOT));
		else
			child.setNamespace(namespace);

		String count = m.group("count");
		child.setCount(count);
		return child;
	}

	private static void updateCount(SchemaChild child) {
		String count = child.getCount();

		if (count == null || count.isEmpty() || count.equals("*")) {
			// No min, no max para empty y "*"
		} else if (count.equals("?")) {
			child.setMax(1);
		} else if (count.equals("+")) {
			child.setMin(1);
		} else if (count.endsWith("+")) {
			int expectedNum = Integer.parseInt(count.substring(0, count.length() - 1));
			child.setMin(expectedNum);
		} else if (count.endsWith("-")) {
			int expectedNum = Integer.parseInt(count.substring(0, count.length() - 1));
			child.setMax(expectedNum);
		} else {
			int expectedNum = Integer.parseInt(count);
			child.setMin(expectedNum);
			child.setMax(expectedNum);
		}
	}
}