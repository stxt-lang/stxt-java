package dev.stxt.schema.type;

import dev.stxt.Node;
import dev.stxt.ParseException;
import dev.stxt.schema.TypeValidator;

public final class MultilineTextValidator implements TypeValidator {
	public static final MultilineTextValidator INSTANCE = new MultilineTextValidator();

	private MultilineTextValidator() {
	}

	@Override
	public void validate(Node n) throws ParseException {
		if (n.getInlineText() != null && !n.getInlineText().isEmpty()) {
			throw new ParseException(n.getLine(), "NOT_ALLOWED_INLINE_TEXT",
					"Not allowed inline text in node " + n.getQualifiedName());
		}
	}
}
