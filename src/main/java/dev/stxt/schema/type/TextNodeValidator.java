package dev.stxt.schema.type;

import dev.stxt.Node;
import dev.stxt.processors.ValidationException;
import dev.stxt.schema.TypeValidator;

public final class TextNodeValidator implements TypeValidator {
	public static final TextNodeValidator INSTANCE = new TextNodeValidator();

	private TextNodeValidator() {
	}

	@Override
	public void validate(Node n) {
		if (!n.getValue().isEmpty()) {
			throw new ValidationException(n.getLine(), "NOT_ALLOWED_VALUE",
					"Not allowed inline text in node " + n.getQualifiedName());
		}
	}
}
