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
	private String description;
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
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Map<String, ChildDefinition> getChildren() {
		return Collections.unmodifiableMap(children);
	}
	public void addChildDefinition(ChildDefinition childDefinition) {
		String qname = childDefinition.getQualifiedName();
		if (children.containsKey(qname)) throw new SchemaException("CHILD_DEF_ALREADY_DEFINED", "Exists a previous node definition with: " + qname);
		children.put(qname, childDefinition);
	}
	// STXT-SCHEMA-SPEC 13.9 / STXT-TEMPLATE-SPEC 14.14: no puede haber valores duplicados
	// tras la normalización por trim. Va aquí, y no en cada parser, porque es el punto por el
	// que pasan las dos vías (schema y template) y así el código de error es el mismo.
	public void addValue(String value, int line) {
	    String trimmed = value == null? "": value.trim();
	    if (!this.values.add(trimmed))
	        throw new ParseException(line, "VALUE_DUPLICATED", "The values " + trimmed + " is duplicated");
	}
    public boolean isAllowedValue(String value) {
        if (this.values.size()==0) return true;
        return this.values.contains(value);
    }
    public Set<String> getValues() {
        return Collections.unmodifiableSet(this.values);
    }
}
