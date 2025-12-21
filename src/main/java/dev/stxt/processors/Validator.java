package dev.stxt.processors;

import dev.stxt.Node;

public interface Validator extends Processor {
	public void validate(Node n);
}
