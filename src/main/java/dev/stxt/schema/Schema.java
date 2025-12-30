package dev.stxt.schema;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import dev.stxt.NamespaceValidator;
import dev.stxt.exceptions.SchemaException;
import dev.stxt.utils.StringUtils;

public class Schema {
	public static final String SCHEMA_NAMESPACE = "@stxt.schema";

	private Map<String, NodeDefinition> nodes = new LinkedHashMap<String, NodeDefinition>();
	private final String namespace;
	
	public Schema(String namespace, int line) {
		this.namespace = StringUtils.lowerCase(namespace);
		NamespaceValidator.validateNamespaceFormat(this.namespace, line);
	}
	
	public Map<String, NodeDefinition> getNodes() {
		return Collections.unmodifiableMap(nodes);
	}
	
	public NodeDefinition getNodeDefinition(String name) {
		return nodes.get(StringUtils.normalize(name));
	}
	
	public void addNodeDefinition(NodeDefinition nodeDefinition) {
		String qname = nodeDefinition.getNormalizedName();
		if (nodes.containsKey(qname)) throw new SchemaException("NODE_DEF_ALREADY_DEFINED", "Exists a previous node definition with: " + qname);
		nodes.put(qname, nodeDefinition);
	}
	
	public String getNamespace() {
		return namespace;
	}
}
