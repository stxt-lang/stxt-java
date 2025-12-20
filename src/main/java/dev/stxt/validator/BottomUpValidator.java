package dev.stxt.validator;

import java.util.List;

import dev.stxt.Node;
import dev.stxt.Processor;

public class BottomUpValidator implements Processor {
	private final List<Processor> processors;

	public BottomUpValidator(List<Processor> processors) {
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
