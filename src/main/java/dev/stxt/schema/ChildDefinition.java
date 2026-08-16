package dev.stxt.schema;

import dev.stxt.NamespaceValidator;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.utils.StringUtils;

/** Definition of an expected child inside a {@link NodeDefinition}: name, namespace and min/max cardinality. */
public class ChildDefinition {
	private final String normalizedName;
	private final String name;
	private final String namespace;
	private final Integer min;
	private final Integer max;

	/**
	 * Creates the definition of an expected child.
	 *
	 * @param name name of the expected child.
	 * @param namespace namespace of the expected child (may be {@code null}).
	 * @param min minimum cardinality, or {@code null} if there is no minimum.
	 * @param max maximum cardinality, or {@code null} if there is no maximum.
	 * @param numLine line number, for the error messages.
	 */
	public ChildDefinition(String name, String namespace, Integer min, Integer max, int numLine) {
		this.name = StringUtils.compactSpaces(name);
		this.normalizedName = StringUtils.normalize(name);
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
		return normalizedName;
	}
	/**
	 * @return the canonical name of the expected child.
	 * @deprecated since 0.7.0, use {@link #getCanonicalName()}.
	 */
	@Deprecated
	public String getNormalizedName() {
		return normalizedName;
	}

	/** {@return the namespace of the expected child, or the empty string if it has none} */
	public String getNamespace() {
		return namespace;
	}

	/** {@return the minimum cardinality, or {@code null} if there is no minimum} */
	public Integer getMin() {
		return min;
	}

	/** {@return the maximum cardinality, or {@code null} if there is no maximum} */
	public Integer getMax() {
		return max;
	}

	/** {@return the canonical name prefixed by its namespace, used as the key in {@link NodeDefinition#getChildren()}} */
	public String getQualifiedName() {
		return namespace.isEmpty() ? normalizedName : namespace + ":" + normalizedName;
	}
}
