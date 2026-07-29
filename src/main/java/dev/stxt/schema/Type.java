package dev.stxt.schema;

import dev.stxt.Node;

/** Tipo de valor de un nodo (TEXT, INTEGER, URL...). Cada tipo vive en {@code dev.stxt.schema.type} como singleton {@code INSTANCE}. */
public interface Type {
	/**
	 * @param nsNode definición del nodo en el schema.
	 * @param node nodo a validar.
	 * @throws dev.stxt.exceptions.ValidationException si el valor del nodo no cumple el tipo.
	 */
	void validate(NodeDefinition nsNode, Node node);
	/** @return nombre del tipo, tal como se usa en los schemas (p. ej. {@code "TEXT"}). */
	String getName();
}
