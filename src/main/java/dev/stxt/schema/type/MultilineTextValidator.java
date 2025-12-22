package dev.stxt.schema.type;

import dev.stxt.Node;
import dev.stxt.processors.ValidationException;
import dev.stxt.schema.TypeValidator;

public final class MultilineTextValidator implements TypeValidator {
	public static final MultilineTextValidator INSTANCE = new MultilineTextValidator();

	private MultilineTextValidator() {
	}

	@Override
	public void validate(Node n) {
		if (n.getInlineText() != null && !n.getInlineText().isEmpty()) {
			throw new ValidationException(n.getLine(), "NOT_ALLOWED_INLINE_TEXT",
					"Not allowed inline text in node " + n.getQualifiedName());
		}
	}
}
