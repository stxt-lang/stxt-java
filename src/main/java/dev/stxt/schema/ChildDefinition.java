package dev.stxt.schema;

import dev.stxt.NamespaceValidator;
import dev.stxt.utils.StringUtils;

public class ChildDefinition {
	private final String normalizedName;
	private final String name;
	private final String namespace;
	private final Integer min;
	private final Integer max;

	public ChildDefinition(String name, String namespace, Integer min, Integer max, int numLine) {
		this.name = StringUtils.compactSpaces(name);
		this.normalizedName = StringUtils.normalize(name);
		this.namespace = StringUtils.lowerCase(namespace);
		this.min = min;
		this.max = max;
		NamespaceValidator.validateNamespaceFormat(namespace, numLine);
	}

	public String getName() {
		return name;
	}

	public String getNormalizedName() {
		return normalizedName;
	}

	public String getNamespace() {
		return namespace;
	}

	public Integer getMin() {
		return min;
	}

	public Integer getMax() {
		return max;
	}

	public String getQualifiedName() {
		return namespace.isEmpty() ? normalizedName : namespace + ":" + normalizedName;
	}
}
