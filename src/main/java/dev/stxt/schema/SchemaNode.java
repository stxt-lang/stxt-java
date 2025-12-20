package dev.stxt.schema;

import java.util.HashMap;
import java.util.Map;

class SchemaNode {
	public String name;
	public String type;
	public Map<String, SchemaChild> children = new HashMap<>();
}
