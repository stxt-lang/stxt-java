package dev.stxt.core;

import java.util.Collections;
import java.util.List;

import dev.stxt.Node;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.processors.Validator;

class ValidatorCountTest implements Validator {
	public int total = 0;

	@Override
	public List<ValidationException> validate(Node n) {
		System.out.println("  => " + n.getQualifiedName());
		total++;
		return Collections.emptyList();
	}
}
