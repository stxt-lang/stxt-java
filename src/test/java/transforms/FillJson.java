package transforms;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;

import dev.stxt.InlineNode;
import dev.stxt.Node;
import dev.stxt.TextNode;

/**
 * Prototype of the fill of Slots of STXT-TRANSFORM-SPEC (Genesis) with JSON data. It is not part
 * of the library: it lives in the test tree to try the rules on real documents while the
 * specification is written, and every rule here may change.
 *
 * <p>The rules are the ones of the example in §4 of the specification:
 *
 * <ul>
 * <li>A Slot is {@code <var>} (puts a value), {@code {object}} (enters an object) or
 *     {@code [list]} (repeats the node once for every element), with a name of ASCII letters,
 *     digits, {@code -} and {@code _}. Everything with that form is a Slot.
 * <li>The value of a node starts with its navigation, {@code [list]} and {@code {object}} only,
 *     applied from left to right; the rest is text, where only {@code <var>} may appear. In a
 *     block, the first line is the navigation when it has nothing else, and it is not copied.
 * <li>A Slot looks its data up in the current element and then upwards to the root of the data.
 *     A {@code null} counts as absent.
 * <li>Every Slot is required unless it has a leading {@code ?}. With no data, {@code ?[list]} and
 *     {@code ?{object}} remove the node, with its children, and {@code ?<var>} puts nothing: only
 *     the navigation decides whether a node appears and how many times.
 * <li>When the elements of a list are values, each one is the value of its node, which must have
 *     nothing but that navigation: no more navigation, no text, no children, no more lines.
 * <li>The filled node keeps the form it has: a value with line breaks is an error inline, and
 *     several lines in a block.
 * </ul>
 *
 * <p>Neither a definition nor a namespace is needed, and the document with Slots is not
 * validated. Blanks between the navigation Slots are allowed here; the specification does not
 * say it yet.
 */
public final class FillJson {
	// A Slot: an optional leading ?, and a name between <>, [] or {}
	private static final Pattern SLOT = Pattern.compile("(\\?)?(?:<([A-Za-z0-9_-]+)>|\\[([A-Za-z0-9_-]+)\\]|\\{([A-Za-z0-9_-]+)\\})");

	private record Slot(char kind, String name, boolean optional, String source) {
		boolean isNavigation() {
			return kind != '<';
		}
	}

	// Where Slots are looked up: the current element and, above it, what contains it
	private record Context(JsonNode data, Context parent) {
		JsonNode lookup(String name) {
			for (Context c = this; c != null; c = c.parent) {
				JsonNode value = c.data.get(name);
				if (value != null && !value.isNull())
					return value;
			}
			return null;
		}
	}

	/**
	 * Fills the Slots of a document with data.
	 *
	 * @param document the root nodes of a document with Slots, as the parser returns them.
	 * @param json the data, as a JSON text.
	 * @return the root nodes of the filled document; it is not validated here.
	 */
	public List<Node> fill(List<Node> document, String json) {
		Context root = new Context(Json.read(json), null);
		List<Node> filled = new ArrayList<>();
		for (Node node : document)
			filled.addAll(fillNode(node, root));
		return filled;
	}

	private static List<Node> fillNode(Node node, Context context) {
		List<Slot> navigation = new ArrayList<>();
		List<String> text = new ArrayList<>();
		if (node instanceof TextNode block) {
			List<String> lines = block.getTextLines();
			if (!lines.isEmpty() && lines.get(0).substring(readNavigation(lines.get(0), navigation)).isBlank()) {
				text.addAll(lines.subList(1, lines.size()));
			} else {
				navigation.clear();
				text.addAll(lines);
			}
		} else {
			String value = ((InlineNode) node).getValue();
			text.add(value.substring(readNavigation(value, navigation)));
		}
		for (String line : text)
			for (Slot slot : slotsOf(line))
				if (slot.isNavigation())
					throw error(node, "Navigation only goes at the start of the value: " + slot.source());

		List<Node> filled = new ArrayList<>();
		for (Context element : navigate(node, navigation, context))
			filled.add(element.data().isValueNode() ? fillWithElement(node, text, element) : fillWithText(node, text, element));
		return filled;
	}

	// Reads the [list] and {object} at the start of a value and returns where its text begins
	private static int readNavigation(String value, List<Slot> navigation) {
		Matcher m = SLOT.matcher(value);
		int end = 0;
		while (true) {
			int start = end;
			while (start < value.length() && (value.charAt(start) == ' ' || value.charAt(start) == '\t'))
				start++;
			m.region(start, value.length());
			if (!m.lookingAt())
				return end;
			Slot slot = slotOf(m);
			if (!slot.isNavigation())
				return end;
			navigation.add(slot);
			end = m.end();
		}
	}

	// One context for every copy of the node: none if an optional Slot has no data
	private static List<Context> navigate(Node node, List<Slot> navigation, Context context) {
		List<Context> current = List.of(context);
		for (Slot slot : navigation) {
			List<Context> next = new ArrayList<>();
			for (Context c : current) {
				if (c.data().isValueNode())
					throw error(node, "A list of values gives the value of the node; nothing can follow it: " + slot.source());
				JsonNode data = c.lookup(slot.name());
				if (data == null) {
					if (slot.optional())
						continue;
					throw error(node, "No data for " + slot.source());
				}
				if (slot.kind() == '{') {
					if (!data.isObject())
						throw error(node, slot.source() + " expects an object");
					next.add(new Context(data, c));
				} else {
					checkList(node, slot, data);
					for (JsonNode e : data)
						if (!e.isNull())
							next.add(new Context(e, c));
				}
			}
			current = next;
		}
		return current;
	}

	private static void checkList(Node node, Slot slot, JsonNode data) {
		if (!data.isArray())
			throw error(node, slot.source() + " expects an array");
		boolean objects = false;
		boolean values = false;
		for (JsonNode e : data) {
			if (e.isArray())
				throw error(node, slot.source() + " has an array inside the array");
			objects |= e.isObject();
			values |= e.isValueNode() && !e.isNull();
		}
		if (objects && values)
			throw error(node, slot.source() + " mixes values and objects");
	}

	// The element of a list of values is the value of the node, which has nothing else
	private static Node fillWithElement(Node node, List<String> text, Context element) {
		boolean onlyNavigation = node instanceof TextNode
				? text.isEmpty()
				: text.get(0).isBlank() && ((InlineNode) node).getChildren().isEmpty();
		if (!onlyNavigation)
			throw error(node, "A node that takes its value from a list of values has nothing else");
		return newNode(node, List.of(Json.text(element.data())));
	}

	private static Node fillWithText(Node node, List<String> text, Context context) {
		List<String> lines = new ArrayList<>();
		for (String line : text)
			lines.add(substitute(node, line, context));
		Node filled = newNode(node, lines);
		if (filled instanceof InlineNode inline)
			for (Node child : ((InlineNode) node).getChildren())
				for (Node c : fillNode(child, context))
					inline.addChild(c);
		return filled;
	}

	// The node in the form it has in the document with Slots
	private static Node newNode(Node node, List<String> text) {
		if (node instanceof TextNode) {
			List<String> lines = new ArrayList<>();
			for (String t : text)
				lines.addAll(List.of(t.split("\n", -1)));
			return new TextNode(node.getName(), node.getDeclaredNamespace(), lines);
		}
		String value = text.get(0);
		if (value.indexOf('\n') >= 0)
			throw error(node, "An inline value cannot have line breaks");
		return new InlineNode(node.getName(), node.getDeclaredNamespace(), value);
	}

	// Puts the data of every <var>; the text around is copied as it is
	private static String substitute(Node node, String text, Context context) {
		Matcher m = SLOT.matcher(text);
		StringBuilder out = new StringBuilder();
		int last = 0;
		while (m.find()) {
			Slot slot = slotOf(m);
			out.append(text, last, m.start());
			JsonNode data = context.lookup(slot.name());
			if (data == null && !slot.optional())
				throw error(node, "No data for " + slot.source());
			if (data != null) {
				if (!data.isValueNode())
					throw error(node, slot.source() + " expects a string, a number or a boolean");
				out.append(Json.text(data));
			}
			last = m.end();
		}
		return out.append(text, last, text.length()).toString();
	}

	private static List<Slot> slotsOf(String text) {
		List<Slot> slots = new ArrayList<>();
		Matcher m = SLOT.matcher(text);
		while (m.find())
			slots.add(slotOf(m));
		return slots;
	}

	private static Slot slotOf(Matcher m) {
		boolean optional = m.group(1) != null;
		if (m.group(2) != null)
			return new Slot('<', m.group(2), optional, m.group());
		if (m.group(3) != null)
			return new Slot('[', m.group(3), optional, m.group());
		return new Slot('{', m.group(4), optional, m.group());
	}

	private static IllegalArgumentException error(Node node, String message) {
		return new IllegalArgumentException("Line " + node.getLine() + ", node '" + node.getName() + "': " + message);
	}
}
