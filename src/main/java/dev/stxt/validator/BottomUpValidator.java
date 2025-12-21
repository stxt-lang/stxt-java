package dev.stxt.validator;

import java.util.List;

import dev.stxt.Node;
import dev.stxt.Validator;

public class BottomUpValidator implements Validator {
	private final List<Validator> validators;

	public BottomUpValidator(List<Validator> processors) {
		this.validators = processors;
	}

	@Override
	public void validate(Node node) {
		for (Node n : node.getChildren()) {
			validate(n);
		}
		for (Validator  validator: validators) {
			validator.validate(node);
		}
	}
}
