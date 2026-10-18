package transforms;

import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import dev.stxt.InlineNode;
import dev.stxt.Node;
import dev.stxt.TextNode;
import dev.stxt.schema.ChildDefinition;
import dev.stxt.schema.NodeDefinition;
import dev.stxt.schema.SchemaProvider;
import dev.stxt.utils.StringUtils;

/**
 * Prototype of the JSON to STXT import of STXT-TRANSFORM-SPEC (Genesis). It is not part of the
 * library: it lives in the test tree to try the rules on real documents while the specification
 * is written, and every rule here may change.
 *
 * <p>It reads the shape that {@link ExportJson} writes, with the same rules in the other
 * direction. The schema gives what the JSON does not carry: the name of the elements of a list,
 * the namespace of each node and, where the schema leaves the choice, the form of the node.
 * Keys are matched with the children of the schema by canonical name. The root node is not
 * wrapped in the JSON: the import is told its name and namespace.
 */
public final class ImportJson {
	private final Definitions definitions;

	/**
	 * @param schemas provider of the schemas of the namespaces to import.
	 */
	public ImportJson(SchemaProvider schemas) {
		this.definitions = new Definitions(schemas);
	}

	/**
	 * Imports a JSON text as a node of a namespace.
	 *
	 * @param json JSON text, with the shape {@link ExportJson#exportJson(Node)} writes.
	 * @param name name of the root node to build.
	 * @param namespace namespace of the root node; its schema drives the import.
	 * @return the root node; it is not validated here.
	 */
	public Node importJson(String json, String name, String namespace) {
		return importNode(Json.read(json), name, namespace);
	}

	private Node importNode(JsonNode json, String name, String namespace) {
		NodeDefinition def = definitions.definitionOf(name, namespace);

		if (def.getChildren().isEmpty())
			return importValue(json, def, namespace);

		InlineNode node = new InlineNode(def.getName(), namespace, null);

		ChildDefinition item = Definitions.itemOf(def);
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
			if (Definitions.isRepeatable(child)) {
				expect(field.getValue().isArray(), "an array", definitions.definitionOf(child.getName(), child.getNamespace()));
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

		String value = Json.text(json);

		// The schema decides the form where it leaves no choice. Where it allows both (TEXT,
		// MARKDOWN), the form that loses less: a block for a text with more than one line or with
		// leading blanks, which an inline value would trim; inline otherwise
		if ("BLOCK".equals(def.getType()) || value.indexOf('\n') >= 0 || startsWithBlank(value))
			return new TextNode(def.getName(), namespace, value);
		return new InlineNode(def.getName(), namespace, value);
	}

	private static boolean startsWithBlank(String value) {
		return !value.isEmpty() && (value.charAt(0) == ' ' || value.charAt(0) == '\t');
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
