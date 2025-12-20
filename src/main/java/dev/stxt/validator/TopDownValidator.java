package dev.stxt.validator;

import java.util.List;

import dev.stxt.Node;
import dev.stxt.ParseException;
import dev.stxt.Validator;

public class TopDownValidator implements Validator {
	private final List<Validator> validators;

	public TopDownValidator(List<Validator> processors) {
		this.validators = processors;
	}

	@Override
	public void validate(Node node) throws ParseException {
		for (Validator  validator: validators) {
			validator.validate(node);
		}
		for (Node n : node.getChildren()) {
			validate(n);
		}
	}
}
