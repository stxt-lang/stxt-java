package dev.stxt.schema;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dev.stxt.exceptions.ParseException;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.utils.StringUtils;

/** Definition of a node inside a {@link Schema}: type, expected children, allowed values and description. */
public class NodeDefinition {
	private final String name;
	private final String canonicalName;
	private final String type;
	private String description;
	private final Map<String, ChildDefinition> children = new HashMap<>();
	private final Set<String> values = new HashSet<String>();
	
	/**
	 * Creates the definition of a node.
	 *
	 * @param name name of the node.
	 * @param type name of the type (see {@link TypeRegistry}).
	 * @param line line number, for the error message.
	 */
	public NodeDefinition(String name, String type, int line) {
		this(name, type, line, null);
	}

	/**
	 * Creates the definition of a node, with its description.
	 *
	 * @param name name of the node.
	 * @param type name of the type (see {@link TypeRegistry}).
	 * @param line line number, for the error message.
	 * @param description optional description of the node (STXT-SCHEMA-SPEC 7.1), or {@code null}.
	 */
	public NodeDefinition(String name, String type, int line, String description) {
		this.name = StringUtils.compactSpaces(name);
		this.canonicalName = StringUtils.normalize(name);
		this.type = type;
		this.description = description;
		if (!StringUtils.isValidNodeName(this.name)) {
		    throw new ValidationException(line, "INVALID_NODE_NAME", "Node name not valid: " + name);
		}
	}
	/** {@return the name of the node, as it appears in the schema} */
	public String getName() {
		return name;
	}
	/** {@return the canonical name of the node} */
	public String getCanonicalName() {
		return canonicalName;
	}
	/** {@return the name of the value type of this node (see {@link TypeRegistry})} */
	public String getType() {
		return type;
	}
	/** {@return the optional description of the node, or {@code null} if it has none} */
	public String getDescription() {
		return description;
	}
	/**
	 * Sets the optional description of the node.
	 *
	 * @param description new optional description of the node.
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/** {@return the definitions of the expected children, indexed by their qualified canonical name} */
	public Map<String, ChildDefinition> getChildren() {
		return Collections.unmodifiableMap(children);
	}
	/**
	 * Adds the definition of an expected child.
	 *
	 * @param childDefinition definition of the child to add.
	 * @throws ValidationException with code {@code CHILD_DUPLICATED} if a definition for that child already existed.
	 */
	public void addChildDefinition(ChildDefinition childDefinition) {
		String qname = childDefinition.getQualifiedName();
		if (children.containsKey(qname))
			throw new ValidationException(ParseException.NO_LINE, "CHILD_DUPLICATED", "A child declaration with the same name already exists: " + qname);
		children.put(qname, childDefinition);
	}
	// STXT-SCHEMA-SPEC 13.9 / STXT-TEMPLATE-SPEC 14.14: there can be no duplicated values
	// after trim normalization. It lives here, and not in each parser, because this is the point
	// both routes (schema and template) go through, so the error code stays the same.
	/**
	 * Adds a value to the list of values allowed for this node.
	 *
	 * @param value value to add to the list of allowed values.
	 * @param line line number, for the error message.
	 * @throws ValidationException with code {@code VALUE_DUPLICATED} if the value (once trimmed) had already been added.
	 */
	public void addValue(String value, int line) {
	    // Language blanks only (U+0020/U+0009): any other whitespace (NBSP...) is part of the value
	    String trimmed = StringUtils.trim(value);
	    if (!this.values.add(trimmed))
	        throw new ValidationException(line, "VALUE_DUPLICATED", "The value " + trimmed + " is duplicated");
	}
    /**
     * Tells whether a value is allowed for this node.
     *
     * @param value value to check.
     * @return {@code true} if no restricted values are defined, or if the value is among the allowed ones.
     */
    public boolean isAllowedValue(String value) {
        if (this.values.size()==0) return true;
        return this.values.contains(value);
    }
    /** {@return the values allowed for this node (ENUM), or empty if there is no restriction} */
    public Set<String> getValues() {
        return Collections.unmodifiableSet(this.values);
    }
}
