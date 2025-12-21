package dev.stxt.processors;

import dev.stxt.Node;

public interface Validator extends Processor {
	void validate(Node n);
}
