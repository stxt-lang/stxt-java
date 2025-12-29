package dev.stxt.schema;

import dev.stxt.utils.StringUtils;

public class ChildDefinition {
	private String normalizedName;
	private String namespace;
	private Integer min = null;
	private Integer max = null;

	public String getNormalizedName() {
		return normalizedName;
	}

	public void setNormalizedName(String name) {
		this.normalizedName = StringUtils.normalizeFull(name);
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public Integer getMin() {
		return min;
	}

	public void setMin(Integer min) {
		this.min = min;
	}

	public Integer getMax() {
		return max;
	}

	public void setMax(Integer max) {
		this.max = max;
	}

	public String getQualifiedName() {
		return namespace == null || namespace.isEmpty() ? normalizedName : namespace + ":" + normalizedName;
	}
}
