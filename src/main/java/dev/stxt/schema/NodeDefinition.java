package dev.stxt.schema;

import java.util.HashMap;
import java.util.Map;

import dev.stxt.utils.StringUtils;

public class NodeDefinition {
	private String normalizedName;
	private String type;
	private Map<String, ChildDefinition> children = new HashMap<>();
	
	public String getNormalizedName() {
		return normalizedName;
	}
	public void setNormalizedName(String name) {
		this.normalizedName = StringUtils.normalizeFull(name);
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Map<String, ChildDefinition> getChildren() {
		return children;
	}
	public void setChildren(Map<String, ChildDefinition> children) {
		this.children = children;
	}
}
