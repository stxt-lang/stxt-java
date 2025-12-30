package dev.stxt.schema;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import dev.stxt.exceptions.SchemaException;
import dev.stxt.utils.StringUtils;

public class NodeDefinition {
	private final String name;
	private final String normalizedName;
	private final String type;
	private final Map<String, ChildDefinition> children = new HashMap<>();
	
	public NodeDefinition(String name, String type) {
		this.name = StringUtils.compactSpaces(name);
		this.normalizedName = StringUtils.normalize(name);
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public String getNormalizedName() {
		return normalizedName;
	}
	public String getType() {
		return type;
	}
	public Map<String, ChildDefinition> getChildren() {
		return Collections.unmodifiableMap(children);
	}
	public void addChildDefinition(ChildDefinition childDefinition) {
		String qname = childDefinition.getQualifiedName();
		if (children.containsKey(qname)) throw new SchemaException("CHILD_DEF_ALREADY_DEFINED", "Exists a previous node definition with: " + qname);
		children.put(qname, childDefinition);
	}
}
