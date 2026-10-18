package transforms;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import dev.stxt.InlineNode;
import dev.stxt.Node;
import dev.stxt.TextNode;
import dev.stxt.schema.ChildDefinition;
import dev.stxt.schema.NodeDefinition;
import dev.stxt.schema.SchemaProvider;

/**
 * Prototype of the STXT to JSON export of STXT-TRANSFORM-SPEC (Genesis). It is not part of the
 * library: it lives in the test tree to try the rules on real documents while the specification
 * is written, and every rule here may change. {@link ImportJson} is the inverse.
 *
 * <p>The export is driven by the schema of the document, which says what the text of the
 * document does not: which values are numbers or booleans and which nodes are lists.
 *
 * <ul>
 * <li>A leaf is a string, except {@code BOOLEAN} (a boolean) and {@code NUMBER},
 *     {@code INTEGER} and {@code NATURAL} (a number). A block is a string with its lines
 *     joined by a line feed.
 * <li>A {@code GROUP} whose only declared child is repeatable is an array: the name of the
 *     child is not written.
 * <li>Any other node with declared children is an object, with one key per child present,
 *     named as the schema names it. A repeatable child is an array under its name.
 * </ul>
 *
 * <p>The root node is not wrapped: the JSON is its content. Not decided yet, and rejected here:
 * a node with a value and children.
 */
public final class ExportJson {
	private final Definitions definitions;

	/**
	 * @param schemas provider of the schemas of the namespaces to export.
	 */
	public ExportJson(SchemaProvider schemas) {
		this.definitions = new Definitions(schemas);
	}

	/**
	 * Exports a node, and everything under it, as JSON.
	 *
	 * @param root node to export; it must be valid against its schema.
	 * @return the JSON text, indented.
	 */
	public String exportJson(Node root) {
		return Json.write(exportNode(root));
	}

	private JsonNode exportNode(Node node) {
		NodeDefinition def = definitions.definitionOf(node.getName(), node.getNamespace());

		if (node instanceof TextNode text)
			return Json.FACTORY.textNode(String.join("\n", text.getTextLines()));

		InlineNode inline = (InlineNode) node;
		if (def.getChildren().isEmpty())
			return exportValue(def.getType(), inline.getValue());

		if (!inline.getValue().isEmpty())
			throw new IllegalArgumentException("Not decided yet: a node with a value and children: " + node.getName());

		if (Definitions.itemOf(def) != null) {
			ArrayNode array = Json.FACTORY.arrayNode();
			for (Node child : inline.getChildren())
				array.add(exportNode(child));
			return array;
		}

		ObjectNode object = Json.FACTORY.objectNode();
		for (ChildDefinition child : def.getChildren().values()) {
			List<Node> nodes = inline.getChildren(child.getName(), child.getNamespace());
			if (nodes.isEmpty())
				continue;
			if (Definitions.isRepeatable(child)) {
				ArrayNode array = object.putArray(child.getName());
				for (Node n : nodes)
					array.add(exportNode(n));
			} else {
				object.set(child.getName(), exportNode(nodes.get(0)));
			}
		}
		return object;
	}

	private static JsonNode exportValue(String type, String value) {
		switch (type) {
			case "BOOLEAN":
				return Json.FACTORY.booleanNode("true".equals(value));
			case "INTEGER":
			case "NATURAL":
				return Json.FACTORY.numberNode(new BigInteger(value));
			case "NUMBER":
				// NUMBER is not the JSON number (+1, 1., .5, 007): the value is written, not the spelling
				return Json.FACTORY.numberNode(new BigDecimal(value));
			default:
				return Json.FACTORY.textNode(value);
		}
	}
}
