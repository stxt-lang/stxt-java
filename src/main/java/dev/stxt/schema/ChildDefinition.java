package dev.stxt.schema;

import dev.stxt.utils.StringUtils;

public class ChildDefinition {
	private final String normalizedName;
	private final String name;
	private final String namespace;
	private final Integer min;
	private final Integer max;

	public ChildDefinition(String name, String namespace, Integer min, Integer max) {
		this.name = StringUtils.normalizeSimple(name);
		this.normalizedName = StringUtils.normalizeFull(name);
		this.namespace = namespace;
		this.min = min;
		this.max = max;
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
		return namespace == null || namespace.isEmpty() ? normalizedName : namespace + ":" + normalizedName;
	}
}
