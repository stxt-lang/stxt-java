package dev.stxt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import dev.stxt.exceptions.STXTException;

/**
 * INLINE node of the STXT tree ({@code Name: value}): an optional inline value and an ordered
 * list of children. It is the only form that can have children, and the only one that can
 * create them ({@link #addInlineNode(String)}, {@link #addTextNode(String)}).
 *
 * <p>Overloads with two strings always take the second one as the <em>content</em> (the value);
 * the namespace only exists in the three-argument forms.
 */
public final class InlineNode extends Node {
	private String value;
	private final List<Node> children = new ArrayList<>();

	/**
	 * Creates an inline node with no value, no declared namespace and no known source line.
	 *
	 * @param name name of the node.
	 */
	public InlineNode(String name) {
		this(name, null, null, NO_LINE);
	}

	/**
	 * Creates an inline node with a value, no declared namespace and no known source line.
	 *
	 * @param name name of the node.
	 * @param value inline value, or {@code null} for none.
	 */
	public InlineNode(String name, String value) {
		this(name, null, value, NO_LINE);
	}

	/**
	 * Creates an inline node with a declared namespace and a value, and no known source line.
	 *
	 * @param name name of the node.
	 * @param namespace namespace the node declares, or {@code null}/empty for none.
	 * @param value inline value, or {@code null} for none.
	 */
	public InlineNode(String name, String namespace, String value) {
		this(name, namespace, value, NO_LINE);
	}

	/**
	 * Creates an inline node with its full description. This is the constructor the
	 * {@link Parser} uses.
	 *
	 * @param name name of the node.
	 * @param namespace namespace the node declares, or {@code null}/empty for none.
	 * @param value inline value, or {@code null} for none.
	 * @param line source line, or {@link #NO_LINE}.
	 * @throws dev.stxt.exceptions.ParseException if the name or the namespace are not valid.
	 */
	public InlineNode(String name, String namespace, String value, int line) {
		super(name, namespace, line);
		setValue(value);
	}

	// ----------------------------------------------------------------
	// Value
	// ----------------------------------------------------------------

	/** {@return the inline value of the node, trimmed; the empty string if it has none} */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the inline value of the node.
	 *
	 * @param value new value, or {@code null} for none. It is trimmed.
	 */
	public void setValue(String value) {
		this.value = value == null ? "" : value.trim();
	}

	@Override
	public String getText() {
		return value;
	}

	@Override
	public boolean isTextNode() {
		return false;
	}

	// ----------------------------------------------------------------
	// Children
	// ----------------------------------------------------------------

	@Override
	public List<Node> getChildren() {
		return Collections.unmodifiableList(children);
	}

	/**
	 * Appends a child at the end of the list of children, linking both ends: afterwards
	 * {@code child.getParent()} is this node.
	 *
	 * @param child node to append; it must not have a parent yet.
	 * @throws STXTException with code {@code NODE_ALREADY_ATTACHED} if the child already has a
	 *         parent (detach it first), or {@code NODE_CYCLE} if it is this node or one of its
	 *         ancestors.
	 */
	public void addChild(Node child) {
		addChild(children.size(), child);
	}

	/**
	 * Inserts a child at the given position of the list of children, linking both ends.
	 *
	 * @param index position where to insert it (0 = first).
	 * @param child node to insert; it must not have a parent yet.
	 * @throws STXTException with code {@code NODE_ALREADY_ATTACHED} if the child already has a
	 *         parent (detach it first), or {@code NODE_CYCLE} if it is this node or one of its
	 *         ancestors.
	 * @throws IndexOutOfBoundsException if the index is out of range.
	 */
	public void addChild(int index, Node child) {
		if (child.getParent() != null)
			throw new STXTException("NODE_ALREADY_ATTACHED", "Node '" + child.getName() + "' already has a parent: detach it first");

		for (Node p = this; p != null; p = p.getParent())
			if (p == child)
				throw new STXTException("NODE_CYCLE", "Node '" + child.getName() + "' cannot be a child of itself or of one of its descendants");

		children.add(index, child);
		child.setParent(this);
	}

	/**
	 * Removes a direct child, unlinking both ends: afterwards {@code child.getParent()} is
	 * {@code null} and the child is a root on its own.
	 *
	 * @param child the child to remove.
	 * @return {@code true} if it was a direct child of this node and has been removed; {@code false} otherwise.
	 */
	public boolean removeChild(Node child) {
		if (child.getParent() != this)
			return false;

		// Identity, not equals(): two children may look alike
		for (int i = 0; i < children.size(); i++) {
			if (children.get(i) == child) {
				children.remove(i);
				child.setParent(null);
				return true;
			}
		}

		return false;
	}

	// ----------------------------------------------------------------
	// Factories: create a child and append it
	// ----------------------------------------------------------------

	/**
	 * Creates an inline child with no value and appends it.
	 *
	 * @param name name of the child.
	 * @return the child created, already attached to this node.
	 */
	public InlineNode addInlineNode(String name) {
		return addInlineNode(name, null, null);
	}

	/**
	 * Creates an inline child with a value and appends it.
	 *
	 * @param name name of the child.
	 * @param value inline value, or {@code null} for none.
	 * @return the child created, already attached to this node.
	 */
	public InlineNode addInlineNode(String name, String value) {
		return addInlineNode(name, null, value);
	}

	/**
	 * Creates an inline child with a declared namespace and a value, and appends it.
	 *
	 * @param name name of the child.
	 * @param namespace namespace the child declares, or {@code null}/empty to inherit this node's.
	 * @param value inline value, or {@code null} for none.
	 * @return the child created, already attached to this node.
	 */
	public InlineNode addInlineNode(String name, String namespace, String value) {
		InlineNode child = new InlineNode(name, namespace, value);
		addChild(child);
		return child;
	}

	/**
	 * Creates an empty text child and appends it.
	 *
	 * @param name name of the child.
	 * @return the child created, already attached to this node.
	 */
	public TextNode addTextNode(String name) {
		return addTextNode(name, null, (List<String>) null);
	}

	/**
	 * Creates a text child with its text and appends it.
	 *
	 * @param name name of the child.
	 * @param text text of the child; it is split into lines at every line break.
	 * @return the child created, already attached to this node.
	 */
	public TextNode addTextNode(String name, String text) {
		return addTextNode(name, null, text);
	}

	/**
	 * Creates a text child with a declared namespace and its text, and appends it.
	 *
	 * @param name name of the child.
	 * @param namespace namespace the child declares, or {@code null}/empty to inherit this node's.
	 * @param text text of the child; it is split into lines at every line break.
	 * @return the child created, already attached to this node.
	 */
	public TextNode addTextNode(String name, String namespace, String text) {
		return addTextNode(name, namespace, text == null ? null : Arrays.asList(TextNode.splitLines(text)));
	}

	/**
	 * Creates a text child with a declared namespace and its lines, and appends it.
	 *
	 * @param name name of the child.
	 * @param namespace namespace the child declares, or {@code null}/empty to inherit this node's.
	 * @param lines text lines of the child, or {@code null} for none.
	 * @return the child created, already attached to this node.
	 */
	public TextNode addTextNode(String name, String namespace, List<String> lines) {
		TextNode child = new TextNode(name, namespace, lines);
		addChild(child);
		return child;
	}

	@Override
	void describe(StringBuilder sb) {
		if (!value.isEmpty()) sb.append(", value='").append(value).append('\'');
		sb.append(", children=").append(children.size());
	}
}
