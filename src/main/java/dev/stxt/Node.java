package dev.stxt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import dev.stxt.exceptions.ParseException;
import dev.stxt.exceptions.STXTException;
import dev.stxt.utils.StringUtils;

/**
 * Node of the STXT tree. Mutable while parsing ({@link #addChild(Node)}/
 * {@link #addTextLine(String)} are public); once the document is closed it must be treated as
 * read-only. It represents both INLINE nodes (with {@link #getValue()}) and BLOCK text nodes
 * (with {@link #getTextLines()}), as told apart by {@link #isTextNode()}.
 */
public class Node {
	// STXT-SPEC 4.2: Unicode letters and digits (categories L and Nd) plus '-', '_' and space
	private static final Pattern VALID_NAME = Pattern.compile("^[\\p{L}\\p{Nd}\\-_ ]+$");

	private final String name;
	private final String normalizedName;
	private final String namespace;
	private final boolean textNode;

	private final String value;
	private List<String> textLines = new ArrayList<>();
	private final int line;
	private final int level;
	private List<Node> children = new ArrayList<>();

    /**
     * Creates a node with neither namespace nor a known position in the document (line/level = -1).
     * Meant for building nodes outside normal parsing (e.g. in tests).
     *
     * @param name name of the node.
     * @param textNode {@code true} if it is a text block node (BLOCK); {@code false} if it is INLINE.
     * @param value inline value of the node (INLINE node), ignored when it is BLOCK.
     */
    public Node(String name, boolean textNode, String value) {
        this(-1,-1,name,null,textNode,value);
    }
    
	/**
	 * Creates a node with an explicit namespace but no known position in the document
	 * (line/level = -1). Meant for building nodes outside normal parsing (e.g. in tests).
	 *
	 * @param name name of the node.
	 * @param namespace namespace of the node, or {@code null} if it has none.
	 * @param textNode {@code true} if it is a text block node (BLOCK); {@code false} if it is INLINE.
	 * @param value inline value of the node (INLINE node), ignored when it is BLOCK.
	 */
	public Node(String name, String namespace, boolean textNode, String value) {
	    this(-1,-1,name,namespace,textNode,value);
	}
	
	/**
	 * Creates a node with its full position in the document. This is the constructor the
	 * {@link Parser} uses while parsing.
	 *
	 * @param line line number of the document where the node opens.
	 * @param level indentation level of the node.
	 * @param name name of the node.
	 * @param namespace namespace of the node, or {@code null} if it has none.
	 * @param textNode {@code true} if it is a text block node (BLOCK); {@code false} if it is INLINE.
	 * @param value inline value of the node (INLINE node), ignored when it is BLOCK.
	 * @throws ParseException if the name or the namespace are not valid.
	 */
	public Node(int line, int level, String name, String namespace, boolean textNode, String value) {
		this.level = level;
		this.line = line;
		this.name = StringUtils.compactSpaces(name);
		this.normalizedName = StringUtils.normalize(name);
		this.namespace = StringUtils.lowerCase(namespace);
		this.value = (value == null ? "" : value.trim());
		this.textNode = textNode;
		NamespaceValidator.validateNamespaceFormat(this.namespace, line);

		if (!this.value.isEmpty() && this.isTextNode())
			throw new IllegalArgumentException("Not empty value with textNode");

		if (name == null || !VALID_NAME.matcher(name).matches()) {
		    throw new ParseException(line, "INVALID_NODE_NAME", "Node name contains invalid characters: " + name);
		}

		if (this.normalizedName.isEmpty()) {
		    throw new ParseException(line, "INVALID_NODE_NAME", "Node name not valid: " + name);
		}
	}

	/** @param line text line to append to a BLOCK node ({@link #isTextNode()}). */
	public void addTextLine(String line) {
		this.textLines.add(line);
	}

	/** @return original name of the node as it appears in the document (with spaces compacted). */
	public String getName() {
		return name;
	}

	/** @return canonical name of the node, used to compare/look up by structural identity. */
	public String getNormalizedName() {
		return normalizedName;
	}

	/** @return canonical name prefixed by its namespace ({@code namespace:name}), or just the name when there is no namespace. */
	public String getQualifiedName() {
		return namespace.isEmpty() ? normalizedName : namespace + ":" + normalizedName;
	}

	/** @return effective namespace of the node (its own or inherited from the parent), lower-cased, or the empty string if it has none. */
	public String getNamespace() {
		return namespace;
	}

	/** @return children of the node in order of appearance, as a read-only view. */
	public List<Node> getChildren() {
		return Collections.unmodifiableList(children);
	}
	
	/** @param node already closed child to append at the end of this node's list of children. */
	public void addChild(Node node) {
		children.add(node);
	}

	/** @return inline value of the node (INLINE node), or the empty string if it is a BLOCK node. */
	public String getValue() {
		return value;
	}

	/** @return text lines of a BLOCK node ({@link #isTextNode()}), in order of appearance. */
	public List<String> getTextLines() {
		return textLines;
	}

	/** @return line number of the document where this node was opened. */
	public int getLine() {
		return line;
	}

	/** @return indentation level of the node (0 for root nodes). */
	public int getLevel() {
		return level;
	}

	/** @return {@code true} if the node is a text block (BLOCK, {@code >>}); {@code false} if it is INLINE. */
	public boolean isTextNode() {
		return textNode;
	}

	/** @return textual content of the node: the text lines joined with '\n' if it is BLOCK, or the inline value otherwise. */
	public String getText() {
		if (isTextNode())
			return String.join("\n", textLines);
		else
			return value;
	}

	/**
	 * Looks up the single direct child with that name in this node's own namespace.
	 *
	 * @param cname name of the child to look for.
	 * @return the child found, or {@code null} if there is none.
	 * @throws STXTException with code {@code AMBIGUOUS_CHILD} if more than one child matches;
	 *         use {@link #getChildren(String)} in that case.
	 */
	public Node getChild(String cname) {
		return getChild(cname, this.namespace);
	}

	/**
	 * Looks up the single direct child with that name in the given namespace.
	 *
	 * @param cname name of the child to look for.
	 * @param namespace namespace to search in.
	 * @return the child found, or {@code null} if there is none.
	 * @throws STXTException with code {@code AMBIGUOUS_CHILD} if more than one child matches;
	 *         use {@link #getChildren(String, String)} in that case.
	 */
	public Node getChild(String cname, String namespace) {
		List<Node> result = getChildren(cname, namespace);
		if (result.size() > 1)
			throw new STXTException("AMBIGUOUS_CHILD", "More than 1 child. Use getChildren");
		if (result.size() == 0)
			return null;
		return result.get(0);
	}

	// Fast access methods to children
	/**
	 * @param cname name of the child to look for.
	 * @return every direct child with that name in this node's own namespace.
	 */
	public List<Node> getChildren(String cname) {
		return getChildren(cname, this.namespace);
	}

	/**
	 * @param cname name of the child to look for.
	 * @param namespace namespace to search in.
	 * @return every direct child with that name in the given namespace.
	 */
	public List<Node> getChildren(String cname, String namespace) {
		String key = StringUtils.normalize(cname);
		List<Node> result = new ArrayList<Node>();

		for (Node child : children) {
			if (child.getNormalizedName().equals(key) && Objects.equals(child.getNamespace(), namespace))
				result.add(child);
		}

		return result;
	}
	
	@Override
	public String toString() {
	    StringBuilder sb = new StringBuilder();
	    sb.append("Node{");
	    sb.append("line=").append(line);
	    sb.append(", level=").append(level);
	    sb.append(", name='").append(name).append('\'');
	    if (!namespace.isEmpty()) sb.append(", ns='").append(namespace).append('\'');
	    sb.append(", text=").append(textNode);
	    if (!textNode && !value.isEmpty()) sb.append(", value='").append(value).append('\'');
	    if (textNode) sb.append(", lines=").append(textLines.size());
	    sb.append(", children=").append(children.size());
	    sb.append('}');
	    return sb.toString();
	}
	
}