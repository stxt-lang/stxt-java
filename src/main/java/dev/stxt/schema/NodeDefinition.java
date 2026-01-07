package dev.stxt.schema;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dev.stxt.exceptions.ParseException;
import dev.stxt.exceptions.SchemaException;
import dev.stxt.utils.StringUtils;

public class NodeDefinition {
	private final String name;
	private final String normalizedName;
	private final String type;
	private final Map<String, ChildDefinition> children = new HashMap<>();
	private final Set<String> values = new HashSet<String>();
	
	public NodeDefinition(String name, String type, int line) {
		this.name = StringUtils.compactSpaces(name);
		this.normalizedName = StringUtils.normalize(name);
		this.type = type;
		if (this.normalizedName.isEmpty()) {
		    throw new ParseException(line, "INVALID_NODE_NAME", "Node name not valid: " + name);
		}
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
	public void addValue(String value) {
	    this.values.add(value);
	}
    public boolean isAllowedValue(String value) {
        if (this.values.size()==0) return true;
        return this.values.contains(value);
    }
    public Set<String> getValues() {
        return Collections.unmodifiableSet(this.values);
    }
}
