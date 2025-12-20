package dev.stxt.schema;

import dev.stxt.Node;
import dev.stxt.ParseException;

public interface TypeValidator {
	void validate(Node node) throws ParseException;
}
