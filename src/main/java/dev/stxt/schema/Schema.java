package dev.stxt.schema;

import java.util.LinkedHashMap;
import java.util.Map;

public class Schema {
	public static final String SCHEMA_NAMESPACE = "@stxt.schema";

	private Map<String, SchemaNode> nodes = new LinkedHashMap<String, SchemaNode>();
	private String namespace;
	
	public Map<String, SchemaNode> getNodes() {
		return nodes;
	}
	public void setNodes(Map<String, SchemaNode> nodes) {
		this.nodes = nodes;
	}
	public String getNamespace() {
		return namespace;
	}
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
}
