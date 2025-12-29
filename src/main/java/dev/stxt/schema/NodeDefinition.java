package dev.stxt.schema;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import dev.stxt.exceptions.SchemaException;
import dev.stxt.utils.StringUtils;

public class NodeDefinition {
	private String name;
	private String normalizedName;
	private String type;
	private Map<String, ChildDefinition> children = new HashMap<>();
	
	public String getName() {
		return name;
	}
	public String getNormalizedName() {
		return normalizedName;
	}
	public void setName(String name) {
		this.name = StringUtils.normalizeSimple(name);
		this.normalizedName = StringUtils.normalizeFull(name);
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
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
