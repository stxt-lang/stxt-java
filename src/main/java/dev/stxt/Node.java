package dev.stxt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.stxt.utils.StringUtils;

public class Node {
	private final String name;
	private final String normalizedName;
	private final String namespace;
	private final boolean multiline;

	private final String inlineText;
	private List<String> multilineText = new ArrayList<>();
	private final int line;
	private final int level;
	private List<Node> children = new ArrayList<>();
	private boolean isFrozen = false;

	public Node(int line, int level, String name, String namespace, boolean multiline, String inlineText) {
		this.level = level;
		this.line = line;
		this.name = StringUtils.normalizeName(name);
		this.normalizedName = StringUtils.normalizeNodeName(name);
		this.namespace = namespace;
		this.inlineText = (inlineText == null ? "" : inlineText);
		this.multiline = multiline;

		if (!this.inlineText.isEmpty() && this.isMultiline())
			throw new IllegalArgumentException("Not empty value with multiline");
	}

	public void addTextLine(String line) {
		this.multilineText.add(line);
	}

	public String getName() {
		return name;
	}

	public String getNormalizedName() {
		return normalizedName;
	}

	public String getQualifiedName() {
		return namespace == null || namespace.isEmpty() ? name : namespace + ":" + name;
	}

	public String getDisplayName() {
	    return (namespace == null || namespace.isEmpty()) ? name : name + "(" + namespace + ")";
	}

	public String getNamespace() {
		return namespace;
	}

	public List<Node> getChildren() {
		return children;
	}

	public String getInlineText() {
		return inlineText;
	}

	public List<String> getMultilineText() {
		return multilineText;
	}

	public int getLine() {
		return line;
	}

	public int getLevel() {
		return level;
	}

	public boolean isMultiline() {
		return multiline;
	}

	public String getText() {
		if (isMultiline())
			return String.join("\n", multilineText);
		else
			return inlineText;
	}

	public void freeze()
	{
		if (isFrozen) return;
		for (Node n: children) n.freeze();
		this.children = Collections.unmodifiableList(this.children);
		this.multilineText = Collections.unmodifiableList(this.multilineText);		
		
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
		String key = StringUtils.normalizeName(cname);
		List<Node> result = new ArrayList<Node>();

		for (Node child : children) {
			if (child.getName().equals(key))
				result.add(child);
		}

		return result;
	}
}