package dev.stxt;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dev.stxt.exceptions.ParseException;
import dev.stxt.exceptions.STXTException;
import dev.stxt.utils.StringUtils;

/**
 * Node of the STXT tree: what INLINE nodes ({@link InlineNode}) and BLOCK text nodes
 * ({@link TextNode}) have in common. The hierarchy is sealed: those two are the only forms.
 *
 * <p>Nodes are mutable, and the tree keeps its own integrity: a node knows its
 * {@link #getParent() parent}, {@link InlineNode#addChild(Node)} links both ends and refuses a
 * node that already has a parent, and {@link InlineNode#removeChild(Node)} / {@link #detach()}
 * undo it. The {@link #getLevel() level} is derived from the chain of parents, never stored.
 *
 * <p>The namespace a node <em>declares</em> ({@link #getDeclaredNamespace()}) and the one that
 * <em>applies</em> to it ({@link #getNamespace()}) are different things: the effective namespace
 * is the declared one or, failing that, the parent's effective namespace (STXT-SPEC: namespaces
 * are inherited vertically). Changing the declared namespace of a node therefore changes the
 * effective namespace of the whole subtree that inherited it, and so does moving a subtree.
 *
 * <p>The source line ({@link #getLine()}) is optional: the parser sets it, code that builds trees
 * usually does not ({@link #NO_LINE}).
 */
public sealed abstract class Node permits InlineNode, TextNode {
	/** Value of {@link #getLine()} when the node has no known position in a document. */
	public static final int NO_LINE = -1;

	private String name;
	private String canonicalName;
	private String declaredNamespace;
	private int line;
	private Node parent;

	/**
	 * Common initialisation, for the two concrete forms.
	 *
	 * @param name name of the node.
	 * @param namespace namespace the node declares, or {@code null}/empty if it declares none.
	 * @param line source line, or {@link #NO_LINE}.
	 * @throws ParseException with code {@code INVALID_NODE_NAME} if the name is not a valid STXT
	 *         node name, or the namespace does not have a valid format.
	 */
	protected Node(String name, String namespace, int line) {
		this.line = line;
		setName(name);
		setNamespace(namespace);
	}

	// ----------------------------------------------------------------
	// Name
	// ----------------------------------------------------------------

	/** {@return the original name of the node as it appears in the document (with spaces compacted)} */
	public String getName() {
		return name;
	}

	/**
	 * Renames the node. The canonical name is recomputed.
	 *
	 * @param name new name of the node.
	 * @throws ParseException with code {@code INVALID_NODE_NAME} if it is not a valid STXT node name.
	 */
	public void setName(String name) {
		String compacted = StringUtils.compactSpaces(name);
		if (!StringUtils.isValidNodeName(compacted))
			throw new ParseException(line, "INVALID_NODE_NAME", "Node name not valid: " + name);

		this.name = compacted;
		this.canonicalName = StringUtils.normalize(name);
	}

	/** {@return the canonical name of the node (STXT-SPEC §4.3), used to compare/look up by structural identity} */
	public String getCanonicalName() {
		return canonicalName;
	}

	/**
	 * @return the canonical name of the node.
	 * @deprecated since 0.7.0, use {@link #getCanonicalName()}; "canonical name" is the term of the
	 *             specifications. To be removed in a later version.
	 */
	@Deprecated
	public String getNormalizedName() {
		return canonicalName;
	}

	/** {@return the canonical name prefixed by the effective namespace ({@code namespace:name}), or just the canonical name when there is no namespace} */
	public String getQualifiedName() {
		String namespace = getNamespace();
		return namespace.isEmpty() ? canonicalName : namespace + ":" + canonicalName;
	}

	// ----------------------------------------------------------------
	// Namespace
	// ----------------------------------------------------------------

	/** {@return the namespace this node declares itself, lower-cased, or the empty string if it declares none (and so inherits the parent's)} */
	public String getDeclaredNamespace() {
		return declaredNamespace;
	}

	/**
	 * Sets the namespace this node declares. The empty string (or {@code null}) means "none":
	 * the node then inherits the effective namespace of its parent.
	 *
	 * @param namespace namespace to declare, or {@code null}/empty for none.
	 * @throws ParseException if the namespace does not have a valid format (STXT-SPEC §7).
	 */
	public void setNamespace(String namespace) {
		String lower = StringUtils.lowerCase(namespace);
		NamespaceValidator.validateNamespaceFormat(lower, line);
		this.declaredNamespace = lower;
	}

	/** {@return the effective namespace of the node: the one it declares or, failing that, the effective namespace of its parent; the empty string if there is none} */
	public String getNamespace() {
		if (!declaredNamespace.isEmpty())
			return declaredNamespace;
		return parent != null ? parent.getNamespace() : "";
	}

	// ----------------------------------------------------------------
	// Position in the source
	// ----------------------------------------------------------------

	/** {@return the line number of the document where this node was opened, or {@link #NO_LINE} if unknown} */
	public int getLine() {
		return line;
	}

	/**
	 * Sets the source line of the node.
	 *
	 * @param line line number, or {@link #NO_LINE} if unknown.
	 */
	public void setLine(int line) {
		this.line = line;
	}

	/** {@return the depth of the node in its tree: 0 for a root node, 1 for its children, and so on} */
	public int getLevel() {
		int level = 0;
		for (Node p = parent; p != null; p = p.parent)
			level++;
		return level;
	}

	// ----------------------------------------------------------------
	// Tree
	// ----------------------------------------------------------------

	/** {@return the parent of this node, or {@code null} if it is a root node} */
	public Node getParent() {
		return parent;
	}

	/**
	 * Removes this node from its parent, if it has one. Afterwards the node is a root, and its
	 * effective namespace is the one it declares.
	 *
	 * @return {@code true} if the node had a parent and was detached; {@code false} if it was already a root.
	 */
	public boolean detach() {
		if (parent == null)
			return false;
		return ((InlineNode) parent).removeChild(this);
	}

	// Both ends of the link are kept in sync by InlineNode; nobody else touches this.
	void setParent(Node parent) {
		this.parent = parent;
	}

	/**
	 * Children of the node in order of appearance, as a read-only view. A {@link TextNode} has
	 * none: this lets code that walks the tree treat both forms alike.
	 *
	 * @return the children of the node, or an empty list.
	 */
	public abstract List<Node> getChildren();

	/**
	 * Looks up the single direct child with that name in this node's effective namespace.
	 *
	 * @param cname name of the child to look for.
	 * @return the child found, or {@code null} if there is none.
	 * @throws STXTException with code {@code AMBIGUOUS_CHILD} if more than one child matches;
	 *         use {@link #getChildren(String)} in that case.
	 */
	public Node getChild(String cname) {
		return getChild(cname, getNamespace());
	}

	/**
	 * Looks up the single direct child with that name in the given namespace.
	 *
	 * @param cname name of the child to look for.
	 * @param namespace effective namespace to search in.
	 * @return the child found, or {@code null} if there is none.
	 * @throws STXTException with code {@code AMBIGUOUS_CHILD} if more than one child matches;
	 *         use {@link #getChildren(String, String)} in that case.
	 */
	public Node getChild(String cname, String namespace) {
		List<Node> result = getChildren(cname, namespace);
		if (result.size() > 1)
			throw new STXTException("AMBIGUOUS_CHILD", "More than 1 child. Use getChildren");
		if (result.isEmpty())
			return null;
		return result.get(0);
	}

	/**
	 * Looks up every direct child with that name, in this node's effective namespace.
	 *
	 * @param cname name of the child to look for.
	 * @return every direct child with that name in this node's effective namespace.
	 */
	public List<Node> getChildren(String cname) {
		return getChildren(cname, getNamespace());
	}

	/**
	 * Looks up every direct child with that name, in the given namespace.
	 *
	 * @param cname name of the child to look for.
	 * @param namespace effective namespace to search in.
	 * @return every direct child with that name in the given namespace.
	 */
	public List<Node> getChildren(String cname, String namespace) {
		String key = StringUtils.normalize(cname);
		List<Node> result = new ArrayList<>();

		for (Node child : getChildren()) {
			if (child.getCanonicalName().equals(key) && Objects.equals(child.getNamespace(), namespace))
				result.add(child);
		}

		return result;
	}

	// ----------------------------------------------------------------
	// Content
	// ----------------------------------------------------------------

	/** {@return {@code true} if the node is a text block (BLOCK, {@code >>}); {@code false} if it is INLINE} */
	public abstract boolean isTextNode();

	/** {@return the textual content of the node: the text lines joined with '\n' if it is BLOCK, or the inline value otherwise} */
	public abstract String getText();

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()).append('{');
		if (line != NO_LINE) sb.append("line=").append(line).append(", ");
		sb.append("name='").append(name).append('\'');
		String namespace = getNamespace();
		if (!namespace.isEmpty()) sb.append(", ns='").append(namespace).append('\'');
		describe(sb);
		sb.append('}');
		return sb.toString();
	}

	// Form-specific part of toString()
	abstract void describe(StringBuilder sb);
}
