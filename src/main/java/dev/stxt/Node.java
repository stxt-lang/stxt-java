package dev.stxt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.stxt.exceptions.ParseException;
import dev.stxt.utils.StringUtils;

public class Node {
	private final String name;
	private final String normalizedName;
	private final String namespace;
	private final boolean textNode;

	private final String value;
	private List<String> textLines = new ArrayList<>();
	private final int line;
	private final int level;
	private List<Node> children = new ArrayList<>();
	private boolean isFrozen = false;

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
		
		if (this.normalizedName.isEmpty()) {
		    throw new ParseException(line, "INVALID_NODE_NAME", "Node name not valid: " + name);
		}
	}

	public void addTextLine(String line) {
		this.textLines.add(line);
	}

	public String getName() {
		return name;
	}

	public String getNormalizedName() {
		return normalizedName;
	}

	public String getQualifiedName() {
		return namespace.isEmpty() ? normalizedName : namespace + ":" + normalizedName;
	}

	public String getNamespace() {
		return namespace;
	}

	public List<Node> getChildren() {
		return Collections.unmodifiableList(children);
	}
	
	void addChild(Node node) {
		children.add(node);
	}

	public String getValue() {
		return value;
	}

	public List<String> getTextLines() {
		return textLines;
	}

	public int getLine() {
		return line;
	}

	public int getLevel() {
		return level;
	}

	public boolean isTextNode() {
		return textNode;
	}

	public String getText() {
		if (isTextNode())
			return String.join("\n", textLines);
		else
			return value;
	}

	public void freeze()
	{
		if (isFrozen) return;
		for (Node n: children) n.freeze();
		this.children = Collections.unmodifiableList(this.children);
		this.textLines = Collections.unmodifiableList(this.textLines);		
		
		isFrozen = true;
	}
	
	public Node getChild(String cname) {
		List<Node> result = getChildren(cname);
		if (result.size() > 1)
			throw new IllegalArgumentException("More than 1 child. Use getChildren");
		if (result.size() == 0)
			return null;
		return result.get(0);
	}

	// Fast access methods to children
	public List<Node> getChildren(String cname) {
		String key = StringUtils.normalize(cname);
		List<Node> result = new ArrayList<Node>();

		for (Node child : children) {
			if (child.getNormalizedName().equals(key))
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