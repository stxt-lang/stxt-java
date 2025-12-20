package dev.stxt.schema.type;

import dev.stxt.Node;
import dev.stxt.ParseException;
import dev.stxt.schema.TypeValidator;

public final class InlineTextValidator implements TypeValidator {
	public static final InlineTextValidator INSTANCE = new InlineTextValidator();

	private InlineTextValidator() {
	}

	@Override
	public void validate(Node n) throws ParseException {
		if (n.getMultilineText() != null && n.getMultilineText().size() > 0) {
			throw new ParseException(n.getLine(), "NOT_ALLOWED_MULTILINE_TEXT",
					"Not allowed multiline text in node " + n.getQualifiedName());
		}
	}
}
