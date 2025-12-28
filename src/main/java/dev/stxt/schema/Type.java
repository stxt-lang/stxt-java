package dev.stxt.schema;

import dev.stxt.Node;

public interface Type {
	void validate(Node node);
	String getName();
}
