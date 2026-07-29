package dev.stxt.schema;

import dev.stxt.NamespaceValidator;
import dev.stxt.exceptions.ParseException;
import dev.stxt.utils.StringUtils;

/** Definición de un hijo esperado dentro de un {@link NodeDefinition}: nombre, namespace y cardinalidad min/max. */
public class ChildDefinition {
	private final String normalizedName;
	private final String name;
	private final String namespace;
	private final Integer min;
	private final Integer max;

	/**
	 * @param name nombre del hijo esperado.
	 * @param namespace namespace del hijo esperado (puede ser {@code null}).
	 * @param min cardinalidad mínima, o {@code null} si no hay mínimo.
	 * @param max cardinalidad máxima, o {@code null} si no hay máximo.
	 * @param numLine número de línea, para los mensajes de error.
	 */
	public ChildDefinition(String name, String namespace, Integer min, Integer max, int numLine) {
		this.name = StringUtils.compactSpaces(name);
		this.normalizedName = StringUtils.normalize(name);
		this.namespace = StringUtils.lowerCase(namespace);
		this.min = min;
		this.max = max;
		NamespaceValidator.validateNamespaceFormat(this.namespace, numLine);
		if (this.normalizedName.isEmpty()) {
		    throw new ParseException(numLine, "INVALID_NODE_NAME", "Node name not valid: " + name);
		}
	}

	/** @return nombre del hijo esperado, tal como aparece en el schema. */
	public String getName() {
		return name;
	}

	/** @return nombre canónico del hijo esperado. */
	public String getNormalizedName() {
		return normalizedName;
	}

	/** @return namespace del hijo esperado, o cadena vacía si no tiene. */
	public String getNamespace() {
		return namespace;
	}

	/** @return cardinalidad mínima, o {@code null} si no hay mínimo. */
	public Integer getMin() {
		return min;
	}

	/** @return cardinalidad máxima, o {@code null} si no hay máximo. */
	public Integer getMax() {
		return max;
	}

	/** @return nombre canónico prefijado por namespace, usado como clave en {@link NodeDefinition#getChildren()}. */
	public String getQualifiedName() {
		return namespace.isEmpty() ? normalizedName : namespace + ":" + normalizedName;
	}
}
