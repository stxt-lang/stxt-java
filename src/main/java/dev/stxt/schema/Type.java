package dev.stxt.schema;

import dev.stxt.Node;

public interface Type {
	void validate(NodeDefinition nsNode, Node node);
	String getName();
}
