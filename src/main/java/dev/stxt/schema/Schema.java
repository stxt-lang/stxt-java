package dev.stxt.schema;

import java.util.LinkedHashMap;
import java.util.Map;

class Schema {
	public static final String SCHEMA_NAMESPACE = "@stxt.schema";

	public Map<String, SchemaNode> nodes = new LinkedHashMap<String, SchemaNode>();
	public String namespace;
}
