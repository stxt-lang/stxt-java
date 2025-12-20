package dev.stxt.validator;

import dev.stxt.Node;
import dev.stxt.Processor;

class CountProcessorTest implements Processor {
	public int total = 0;

	@Override
	public void process(Node n) {
		total++;
	}
}
