package dev.stxt.schema.type;

import dev.stxt.Node;
import dev.stxt.processors.ValidationException;
import dev.stxt.schema.TypeValidator;

public final class EmptyValidator implements TypeValidator {
	public static final EmptyValidator INSTANCE = new EmptyValidator();

	private EmptyValidator() {
	}

	@Override
	public void validate(Node n) {
		if (!n.getValue().isEmpty() || n.getTextLines().size() > 0) {
			throw new ValidationException(n.getLine(), "INVALID_VALUE", "Node '" + n.getName() + "' has to be empty");
		}
	}
}
