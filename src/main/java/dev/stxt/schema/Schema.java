package dev.stxt.schema;

import java.util.LinkedHashMap;
import java.util.Map;

public class Schema {
	public static final String SCHEMA_NAMESPACE = "@stxt.schema";

	private Map<String, NodeDefinition> nodes = new LinkedHashMap<String, NodeDefinition>();
	private String namespace;
	
	public Map<String, NodeDefinition> getNodes() {
		return nodes;
	}
	public String getNamespace() {
		return namespace;
	}
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
}
