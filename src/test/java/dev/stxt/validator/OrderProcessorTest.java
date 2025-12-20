package dev.stxt.validator;

import java.util.ArrayList;
import java.util.List;

import dev.stxt.Node;
import dev.stxt.Processor;

class OrderProcessorTest implements Processor {
	public List<String> elements = new ArrayList<String>();

	@Override
	public void process(Node n) {
		elements.add(n.getQualifiedName());
	}
}
