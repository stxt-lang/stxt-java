package dev.stxt.schema;

import java.util.HashMap;
import java.util.Map;

public class NodeDefinition {
	private String name;
	private String type;
	private Map<String, ChildDefinition> children = new HashMap<>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
