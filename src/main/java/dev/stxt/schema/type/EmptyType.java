package dev.stxt.schema.type;

import dev.stxt.Node;
import dev.stxt.processors.ValidationException;
import dev.stxt.schema.Type;

public final class EmptyType implements Type {
	public static final EmptyType INSTANCE = new EmptyType();

	private EmptyType() {
	}

	@Override
	public void validate(Node n) {
		if (!n.getValue().isEmpty() || n.getTextLines().size() > 0) {
			throw new ValidationException(n.getLine(), "INVALID_VALUE", "Node '" + n.getName() + "' has to be empty");
		}
	}

	@Override
	public String getName() {
		return "EMPTY";
	}
}
