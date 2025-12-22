package dev.stxt.schema.type;

import dev.stxt.Node;
import dev.stxt.processors.ValidationException;
import dev.stxt.schema.TypeValidator;

public final class InlineTextValidator implements TypeValidator {
	public static final InlineTextValidator INSTANCE = new InlineTextValidator();

	private InlineTextValidator() {
	}

	@Override
	public void validate(Node n) {
		if (n.getMultilineText() != null && n.getMultilineText().size() > 0) {
			throw new ValidationException(n.getLine(), "NOT_ALLOWED_MULTILINE_TEXT",
					"Not allowed multiline text in node " + n.getQualifiedName());
		}
	}
}
