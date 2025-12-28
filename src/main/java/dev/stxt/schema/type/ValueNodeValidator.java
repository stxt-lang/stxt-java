package dev.stxt.schema.type;

import dev.stxt.Node;
import dev.stxt.processors.ValidationException;
import dev.stxt.schema.Type;

public final class ValueNodeValidator implements Type {
	public static final ValueNodeValidator INSTANCE = new ValueNodeValidator();

	private ValueNodeValidator() {
	}

	@Override
	public void validate(Node n) {
		if (n.getTextLines().size() > 0) {
			throw new ValidationException(n.getLine(), "NOT_ALLOWED_TEXT",
					"Not allowed text in node " + n.getQualifiedName());
		}
	}

	@Override
	public String getName() {
		return "VALUE_NODE";
	}
}
