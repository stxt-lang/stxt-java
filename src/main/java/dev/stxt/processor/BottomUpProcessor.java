package dev.stxt.processor;

import java.util.List;

import dev.stxt.Node;

public class BottomUpProcessor implements Processor {
	private final List<Processor> processors;

	public BottomUpProcessor(List<Processor> processors) {
		this.processors = processors;
	}

	@Override
	public void process(Node n) {
		for (Node node : n.getChildren()) {
			process(node);
		}
		for (Processor p : processors) {
			p.process(n);
		}
	}
}
