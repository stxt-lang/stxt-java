package dev.stxt.schema;

import dev.stxt.NamespaceValidator;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.utils.StringUtils;

/** Definition of an expected child inside a {@link NodeDefinition}: name, namespace and min/max cardinality. */
public class ChildDefinition {
	private final String canonicalName;
	private final String name;
	private final String namespace;
	// Long, not Integer: the bound of a cardinality is 2^32 - 1 (STXT-SCHEMA-SPEC 10),
	// which does not fit a signed int
	private final Long min;
	private final Long max;

	/**
	 * Creates the definition of an expected child.
	 *
	 * @param name name of the expected child.
	 * @param namespace namespace of the expected child (may be {@code null}).
	 * @param min minimum cardinality, or {@code null} if there is no minimum.
	 * @param max maximum cardinality, or {@code null} if there is no maximum.
	 * @param numLine line number, for the error messages.
	 */
	public ChildDefinition(String name, String namespace, Long min, Long max, int numLine) {
		this.name = StringUtils.compactSpaces(name);
		this.canonicalName = StringUtils.normalize(name);
		this.namespace = StringUtils.lowerCase(namespace);
		this.min = min;
		this.max = max;
		NamespaceValidator.validateNamespaceFormat(this.namespace, numLine);
		if (!StringUtils.isValidNodeName(this.name)) {
		    throw new ValidationException(numLine, "INVALID_NODE_NAME", "Node name not valid: " + name);
		}
	}

	/** {@return the name of the expected child, as it appears in the schema} */
	public String getName() {
		return name;
	}

	/** {@return the canonical name of the expected child} */
	public String getCanonicalName() {
		return canonicalName;
	}

	/** {@return the namespace of the expected child, or the empty string if it has none} */
	public String getNamespace() {
		return namespace;
	}

	/** {@return the minimum cardinality, or {@code null} if there is no minimum} */
	public Long getMin() {
		return min;
	}

	/** {@return the maximum cardinality, or {@code null} if there is no maximum} */
	public Long getMax() {
		return max;
	}

	/** {@return the canonical name prefixed by its namespace, used as the key in {@link NodeDefinition#getChildren()}} */
	public String getQualifiedName() {
		return namespace.isEmpty() ? canonicalName : namespace + ":" + canonicalName;
	}
}
