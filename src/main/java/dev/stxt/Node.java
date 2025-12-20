package dev.stxt;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dev.stxt.utils.JSON;

public class Node {
	private final String name;
	private final String namespace;
	private final boolean multiline;

	private final String inlineText;
	private final List<String> multilineText = new ArrayList<>();
	private final int line;
	private final int level;
	private final List<Node> children = new ArrayList<>();

	public Node(int line, int level, String name, String namespace, boolean multiline, String value) {
		this.level = level;
		this.line = line;
		this.name = name.toLowerCase(Locale.ENGLISH);
		this.namespace = namespace;
		this.inlineText = value;
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

	public String getQualifiedName() {
		return namespace + ':' + name;
	}

	public String getDisplayName() {
		return namespace + '(' + name + ')';
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

	public String toJson() {
		return JSON.toJson(this);
	}

	public String toJsonPretty() {
		return JSON.toJsonPretty(this);
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

	public Node getChild(String cname) {
		List<Node> result = getChilds(cname);
		if (result.size() > 1)
			throw new IllegalArgumentException("More than 1 child. Use getChilds");
		if (result.size() == 0)
			return null;
		return result.get(0);
	}

	// Fast access methods to children
	public List<Node> getChilds(String cname) {
		List<Node> result = new ArrayList<Node>();

		for (Node child : children) {
			if (child.getName().equalsIgnoreCase(cname))
				result.add(child);
		}

		return result;
	}
}