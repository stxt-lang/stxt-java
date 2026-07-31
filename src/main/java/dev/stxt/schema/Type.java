package dev.stxt.schema;

import dev.stxt.Node;

/** Value type of a node (TEXT, INTEGER, URL...). Each type lives in {@code dev.stxt.schema.type} as an {@code INSTANCE} singleton. */
public interface Type {
	/**
	 * @param nsNode definition of the node in the schema.
	 * @param node node to validate.
	 * @throws dev.stxt.exceptions.ValidationException if the value of the node does not match the type.
	 */
	void validate(NodeDefinition nsNode, Node node);
	/** @return name of the type, as used in the schemas (e.g. {@code "TEXT"}). */
	String getName();
}
