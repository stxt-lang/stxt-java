package dev.stxt.schema;

import java.util.LinkedHashMap;
import java.util.Map;

import dev.stxt.utils.JSON;

class Schema {
	public static final String SCHEMA_NAMESPACE = "@stxt.schema";

	public Map<String, SchemaNode> nodes = new LinkedHashMap<String, SchemaNode>();
	public String namespace;

	public String toJson() {
		return JSON.toJson(this);
	}

	public String toJsonPretty() {
		return JSON.toJsonPretty(this);
	}

}
