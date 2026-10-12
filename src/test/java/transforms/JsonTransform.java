package transforms;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import dev.stxt.InlineNode;
import dev.stxt.Node;
import dev.stxt.TextNode;
import dev.stxt.schema.ChildDefinition;
import dev.stxt.schema.NodeDefinition;
import dev.stxt.schema.Schema;
import dev.stxt.schema.SchemaProvider;
import dev.stxt.utils.StringUtils;

/**
 * Prototype of the STXT to JSON export and the JSON to STXT import of STXT-TRANSFORM-SPEC
 * (Genesis). It is not part of the library: it lives in the test tree to try the rules on real
 * documents while the specification is written, and every rule here may change.
 *
 * <p>Both directions are driven by the schema of the document, which says what the text of the
 * document does not: which values are numbers or booleans, which nodes are lists and, on
 * import, how the elements of a list are named.
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
 * <p>The root node is not wrapped: the import is told its name and namespace. Not decided yet,
 * and rejected here: a node with a value and children.
 */
public final class JsonTransform {
	private static final JsonNodeFactory JSON = JsonNodeFactory.instance;
	// Decimals as BigDecimal, so that importing 1.10 writes 1.10 and not 1.1
	private static final ObjectMapper MAPPER = new ObjectMapper().enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);

	private final SchemaProvider schemas;

	/**
	 * @param schemas provider of the schemas of the namespaces to transform.
	 */
	public JsonTransform(SchemaProvider schemas) {
		this.schemas = schemas;
	}

	// ----------------------------------------------------------------
	// Export
	// ----------------------------------------------------------------

	/**
	 * Exports a node, and everything under it, as JSON.
	 *
	 * @param root node to export; it must be valid against its schema.
	 * @return the JSON text, indented.
	 */
	public String exportJson(Node root) {
		try {
			return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(exportNode(root));
		} catch (JsonProcessingException e) {
			throw new IllegalStateException(e);
		}
	}

	private JsonNode exportNode(Node node) {
		NodeDefinition def = definitionOf(node.getName(), node.getNamespace());

		if (node instanceof TextNode text)
			return JSON.textNode(String.join("\n", text.getTextLines()));

		InlineNode inline = (InlineNode) node;
		if (def.getChildren().isEmpty())
			return exportValue(def.getType(), inline.getValue());

		if (!inline.getValue().isEmpty())
			throw new IllegalArgumentException("Not decided yet: a node with a value and children: " + node.getName());

		if (itemOf(def) != null) {
			ArrayNode array = JSON.arrayNode();
			for (Node child : inline.getChildren())
				array.add(exportNode(child));
			return array;
		}

		ObjectNode object = JSON.objectNode();
		for (ChildDefinition child : def.getChildren().values()) {
			List<Node> nodes = inline.getChildren(child.getName(), child.getNamespace());
			if (nodes.isEmpty())
				continue;
			if (isRepeatable(child)) {
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
				return JSON.booleanNode("true".equals(value));
			case "INTEGER":
			case "NATURAL":
				return JSON.numberNode(new BigInteger(value));
			case "NUMBER":
				// NUMBER is not the JSON number (+1, 1., .5, 007): the value is written, not the spelling
				return JSON.numberNode(new BigDecimal(value));
			default:
				return JSON.textNode(value);
		}
	}

	// ----------------------------------------------------------------
	// Import
	// ----------------------------------------------------------------

	/**
	 * Imports a JSON text as a node of a namespace.
	 *
	 * @param json JSON text, with the shape {@link #exportJson(Node)} writes.
	 * @param name name of the root node to build.
	 * @param namespace namespace of the root node; its schema drives the import.
	 * @return the root node; it is not validated here.
	 */
	public Node importJson(String json, String name, String namespace) {
		try {
			return importNode(MAPPER.readTree(json), name, namespace);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("Not a JSON text: " + e.getOriginalMessage(), e);
		}
	}

	private Node importNode(JsonNode json, String name, String namespace) {
		NodeDefinition def = definitionOf(name, namespace);

		if (def.getChildren().isEmpty())
			return importValue(json, def, namespace);

		InlineNode node = new InlineNode(def.getName(), namespace, null);

		ChildDefinition item = itemOf(def);
		if (item != null) {
			expect(json.isArray(), "an array", def);
			for (JsonNode element : json)
				node.addChild(importNode(element, item.getName(), item.getNamespace()));
			return node;
		}

		expect(json.isObject(), "an object", def);
		Iterator<Map.Entry<String, JsonNode>> fields = json.fields();
		while (fields.hasNext()) {
			Map.Entry<String, JsonNode> field = fields.next();
			ChildDefinition child = childOf(def, field.getKey());
			if (isRepeatable(child)) {
				expect(field.getValue().isArray(), "an array", definitionOf(child.getName(), child.getNamespace()));
				for (JsonNode element : field.getValue())
					node.addChild(importNode(element, child.getName(), child.getNamespace()));
			} else {
				node.addChild(importNode(field.getValue(), child.getName(), child.getNamespace()));
			}
		}
		return node;
	}

	private static Node importValue(JsonNode json, NodeDefinition def, String namespace) {
		expect(json.isValueNode() && !json.isNull(), "a string, a number or a boolean", def);

		String value;
		if (json.isIntegralNumber())
			value = json.bigIntegerValue().toString();
		else if (json.isNumber())
			value = json.decimalValue().toPlainString();
		else
			value = json.asText();

		// The schema decides the form where it leaves no choice; where it allows both (TEXT,
		// MARKDOWN), a text with more than one line can only be a block
		if ("BLOCK".equals(def.getType()) || value.indexOf('\n') >= 0)
			return new TextNode(def.getName(), namespace, value);
		return new InlineNode(def.getName(), namespace, value);
	}

	// ----------------------------------------------------------------
	// What the schema says
	// ----------------------------------------------------------------

	private NodeDefinition definitionOf(String name, String namespace) {
		Schema schema = schemas.getSchema(namespace);
		if (schema == null)
			throw new IllegalArgumentException("A transform needs the schema of the namespace: " + namespace);
		NodeDefinition def = schema.getNodeDefinition(name);
		if (def == null)
			throw new IllegalArgumentException("Node '" + name + "' is not defined in " + namespace);
		return def;
	}

	// The child that makes a node a list: the only child a GROUP declares, when it is repeatable
	private static ChildDefinition itemOf(NodeDefinition def) {
		if (!"GROUP".equals(def.getType()) || def.getChildren().size() != 1)
			return null;
		ChildDefinition only = def.getChildren().values().iterator().next();
		return isRepeatable(only) ? only : null;
	}

	private static boolean isRepeatable(ChildDefinition child) {
		return child.getMax() == null || child.getMax() > 1;
	}

	// Keys are matched by canonical name, as node names are everywhere else
	private static ChildDefinition childOf(NodeDefinition def, String key) {
		String canonical = StringUtils.normalize(key);
		for (ChildDefinition child : def.getChildren().values())
			if (child.getCanonicalName().equals(canonical))
				return child;
		throw new IllegalArgumentException("Key '" + key + "' is not a child of '" + def.getName() + "'");
	}

	private static void expect(boolean condition, String expected, NodeDefinition def) {
		if (!condition)
			throw new IllegalArgumentException("Node '" + def.getName() + "' expects " + expected);
	}
}
