package dev.stxt.schema;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dev.stxt.exceptions.ParseException;
import dev.stxt.exceptions.SchemaException;
import dev.stxt.utils.StringUtils;

/** Definición de un nodo dentro de un {@link Schema}: tipo, hijos esperados, valores permitidos y descripción. */
public class NodeDefinition {
	private final String name;
	private final String normalizedName;
	private final String type;
	private String description;
	private final Map<String, ChildDefinition> children = new HashMap<>();
	private final Set<String> values = new HashSet<String>();
	
	/**
	 * @param name nombre del nodo.
	 * @param type nombre del tipo (ver {@link TypeRegistry}).
	 * @param line número de línea, para el mensaje de error.
	 */
	public NodeDefinition(String name, String type, int line) {
		this.name = StringUtils.compactSpaces(name);
		this.normalizedName = StringUtils.normalize(name);
		this.type = type;
		if (this.normalizedName.isEmpty()) {
		    throw new ParseException(line, "INVALID_NODE_NAME", "Node name not valid: " + name);
		}
	}
	/** @return nombre del nodo, tal como aparece en el schema. */
	public String getName() {
		return name;
	}
	/** @return nombre canónico del nodo. */
	public String getNormalizedName() {
		return normalizedName;
	}
	/** @return nombre del tipo de valor de este nodo (ver {@link TypeRegistry}). */
	public String getType() {
		return type;
	}
	/** @return descripción opcional del nodo, o {@code null} si no tiene. */
	public String getDescription() {
		return description;
	}
	/** @param description nueva descripción opcional del nodo. */
	public void setDescription(String description) {
		this.description = description;
	}
	/** @return definiciones de los hijos esperados, indexadas por su nombre canónico cualificado. */
	public Map<String, ChildDefinition> getChildren() {
		return Collections.unmodifiableMap(children);
	}
	/**
	 * @param childDefinition definición del hijo a añadir.
	 * @throws dev.stxt.exceptions.SchemaException con código {@code CHILD_DEF_ALREADY_DEFINED} si ya existía una definición para ese hijo.
	 */
	public void addChildDefinition(ChildDefinition childDefinition) {
		String qname = childDefinition.getQualifiedName();
		if (children.containsKey(qname)) throw new SchemaException("CHILD_DEF_ALREADY_DEFINED", "Exists a previous node definition with: " + qname);
		children.put(qname, childDefinition);
	}
	// STXT-SCHEMA-SPEC 13.9 / STXT-TEMPLATE-SPEC 14.14: no puede haber valores duplicados
	// tras la normalización por trim. Va aquí, y no en cada parser, porque es el punto por el
	// que pasan las dos vías (schema y template) y así el código de error es el mismo.
	/**
	 * @param value valor a añadir a la lista de valores permitidos.
	 * @param line número de línea, para el mensaje de error.
	 * @throws ParseException con código {@code VALUE_DUPLICATED} si el valor (tras trim) ya se había añadido.
	 */
	public void addValue(String value, int line) {
	    String trimmed = value == null? "": value.trim();
	    if (!this.values.add(trimmed))
	        throw new ParseException(line, "VALUE_DUPLICATED", "The values " + trimmed + " is duplicated");
	}
    /**
     * @param value valor a comprobar.
     * @return {@code true} si no hay valores restringidos definidos, o si el valor está entre los permitidos.
     */
    public boolean isAllowedValue(String value) {
        if (this.values.size()==0) return true;
        return this.values.contains(value);
    }
    /** @return valores permitidos para este nodo (ENUM), o vacío si no hay restricción. */
    public Set<String> getValues() {
        return Collections.unmodifiableSet(this.values);
    }
}
