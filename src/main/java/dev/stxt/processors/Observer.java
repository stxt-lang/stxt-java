package dev.stxt.processors;

import dev.stxt.Node;

public interface Observer extends Processor {
	void process(Node node);
}
