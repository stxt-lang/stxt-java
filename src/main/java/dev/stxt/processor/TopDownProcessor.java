package dev.stxt.processor;

import java.util.List;

import dev.stxt.Node;

public class TopDownProcessor implements Processor {
	private final List<Processor> processors;

	public TopDownProcessor(List<Processor> processors) {
		this.processors = processors;
	}

	@Override
	public void process(Node n) {
		for (Processor p : processors) {
			p.process(n);
		}
		for (Node node : n.getChildren()) {
			process(node);
		}
	}
}
